package com.cherry.jeeves.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cherry.jeeves.domain.request.component.BaseRequest;
import com.cherry.jeeves.domain.response.BatchGetContactResponse;
import com.cherry.jeeves.domain.response.GetContactResponse;
import com.cherry.jeeves.domain.response.InitResponse;
import com.cherry.jeeves.domain.response.LoginResult;
import com.cherry.jeeves.domain.response.StatusNotifyResponse;
import com.cherry.jeeves.domain.shared.ChatRoomDescription;
import com.cherry.jeeves.domain.shared.Contact;
import com.cherry.jeeves.domain.shared.Token;
import com.cherry.jeeves.enums.LoginCode;
import com.cherry.jeeves.enums.StatusNotifyCode;
import com.cherry.jeeves.exception.WechatException;
import com.cherry.jeeves.exception.WechatQRExpiredException;
import com.cherry.jeeves.utils.WechatUtils;

@Component
public class LoginService {
    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);

    @Autowired
    private CacheService cacheService;
    @Autowired
    private SyncServie syncServie;
    @Autowired
    private WechatHttpServiceInternal wechatHttpServiceInternal;

    @Value("${jeeves.auto-relogin-when-qrcode-expired}")
    private boolean AUTO_RELOGIN_WHEN_QRCODE_EXPIRED;

    @Value("${jeeves.max-qr-refresh-times}")
    private int MAX_QR_REFRESH_TIMES;

    private int qrRefreshTimes = 0;

    public void login() {
		try {
			//0 entry
			wechatHttpServiceInternal.open(qrRefreshTimes);
			logger.info("[0] entry completed");
			//1 uuid
			String uuid = wechatHttpServiceInternal.getUUID();
			cacheService.setUuid(uuid);
			logger.info("[1] uuid completed");
			//2 qr
			byte[] qrData = wechatHttpServiceInternal.getQR(uuid);
			syncServie.getMessageHandler().onQR(qrData);
			logger.info("[2] qrcode completed");
			//3 statreport
			wechatHttpServiceInternal.statReport();
			logger.info("[3] statReport completed");
			//4 login
			LoginResult loginResponse;
			while (true) {
				if(Thread.currentThread().isInterrupted()) {
					logger.info("[Stop] exit bye");
					return;
				}
				loginResponse = wechatHttpServiceInternal.login(uuid);
				if (LoginCode.SUCCESS.getCode().equals(loginResponse.getCode())) {
					if (loginResponse.getHostUrl() == null) {
						throw new WechatException("hostUrl can't be found");
					}
					if (loginResponse.getRedirectUrl() == null) {
						throw new WechatException("redirectUrl can't be found");
					}
					cacheService.setHostUrl(loginResponse.getHostUrl());
					if (loginResponse.getHostUrl().equals("https://wechat.com")) {
						cacheService.setSyncUrl("https://webpush.web.wechat.com");
						cacheService.setFileUrl("https://file.web.wechat.com");
					} else {
						cacheService.setSyncUrl(loginResponse.getHostUrl().replaceFirst("^https://", "https://webpush."));
						cacheService.setFileUrl(loginResponse.getHostUrl().replaceFirst("^https://", "https://file."));
					}
					break;
				} else if (LoginCode.AWAIT_CONFIRMATION.getCode().equals(loginResponse.getCode())) {
					logger.info("[*] login status = AWAIT_CONFIRMATION");
					syncServie.getMessageHandler().onScanning(loginResponse.getUserAvatar());
				} else if (LoginCode.AWAIT_SCANNING.getCode().equals(loginResponse.getCode())) {
					logger.info("[*] login status = AWAIT_SCANNING");
				} else if (LoginCode.EXPIRED.getCode().equals(loginResponse.getCode())) {
					logger.info("[*] login status = EXPIRED");
					throw new WechatQRExpiredException();
				} else {
					logger.info("[*] login status = " + loginResponse.getCode());
				}
			}
			logger.info("[4] login completed");
			syncServie.getMessageHandler().onConfirmation();
			//5 redirect login
			Token token = wechatHttpServiceInternal.openNewloginpage(loginResponse.getRedirectUrl());
			if (token.getRet() == 0) {
				cacheService.setPassTicket(token.getPass_ticket());
				cacheService.setsKey(token.getSkey());
				cacheService.setSid(token.getWxsid());
				cacheService.setUin(token.getWxuin());
				BaseRequest baseRequest = new BaseRequest();
				baseRequest.setUin(cacheService.getUin());
				baseRequest.setSid(cacheService.getSid());
				baseRequest.setSkey(cacheService.getsKey());
				cacheService.setBaseRequest(baseRequest);
			} else {
				throw new WechatException("token ret = " + token.getRet());
			}
			logger.info("[5] redirect login completed");
			//6 redirect
			wechatHttpServiceInternal.redirect();
			logger.info("[6] redirect completed");
			//7 init
			InitResponse initResponse = wechatHttpServiceInternal.init();
			WechatUtils.checkBaseResponse(initResponse);
			cacheService.setSyncKey(initResponse.getSyncKey());
			cacheService.setOwner(initResponse.getUser());
			logger.info("[7] init completed");
			new Thread(() -> {
				syncServie.getMessageHandler().onLogin(initResponse.getUser());
			}).start();
			//8 status notify
			StatusNotifyResponse statusNotifyResponse =
					wechatHttpServiceInternal.statusNotify(cacheService.getOwner().getUserName(), StatusNotifyCode.INITED.getCode());
			WechatUtils.checkBaseResponse(statusNotifyResponse);
			
			logger.info("[8] status notify completed");
			//9 get contact
			Set<Contact> initChatRooms =  Arrays.stream(initResponse.getChatSet().split(","))
			.filter(x -> x != null && x.startsWith("@@")).map(x -> {
				Contact contact = new Contact();
				contact.setUserName(x);
				return contact;
			}).collect(Collectors.toSet());
			cacheService.getChatRooms().addAll(initChatRooms);
			long seq = 0;
			do {
				GetContactResponse getContactResponse = wechatHttpServiceInternal.getContact(seq);
				WechatUtils.checkBaseResponse(getContactResponse);
				logger.info("[*] getContactResponse seq = " + getContactResponse.getSeq());
				logger.info("[*] getContactResponse memberCount = " + getContactResponse.getMemberCount());
				seq = getContactResponse.getSeq();
				cacheService.getIndividuals().addAll(getContactResponse.getMemberList().stream().filter(WechatUtils::isIndividual).collect(Collectors.toSet()));
				cacheService.getMediaPlatforms().addAll(getContactResponse.getMemberList().stream().filter(WechatUtils::isMediaPlatform).collect(Collectors.toSet()));
				cacheService.getChatRooms().addAll(getContactResponse.getMemberList().stream().filter(WechatUtils::isChatRoom).collect(Collectors.toSet()));
			} while (seq > 0);
			logger.info("[9] get contact completed");
			//10 batch get contact
			
			while(true){
				ChatRoomDescription[] chatRoomDescriptions = cacheService.getChatRooms().stream()
					.filter(c -> c.getEncryChatRoomId() == null || c.getEncryChatRoomId().isEmpty())
					.limit(50)
					.map(x -> {
						ChatRoomDescription description = new ChatRoomDescription();
						description.setUserName(x.getUserName());
						return description;
					})
					.toArray(ChatRoomDescription[]::new);
				if (chatRoomDescriptions.length > 0) {
					BatchGetContactResponse batchGetContactResponse = wechatHttpServiceInternal.batchGetContact(chatRoomDescriptions);
					WechatUtils.checkBaseResponse(batchGetContactResponse);
					logger.info("[*] batchGetContactResponse count = " + batchGetContactResponse.getCount());
					batchGetContactResponse.getContactList().forEach(x -> {
						cacheService.getChatRooms().remove(x);
						cacheService.getChatRooms().add(x);
		            });
//					cacheService.getChatRooms().addAll(batchGetContactResponse.getContactList());
				} else {
					break;
				}
			}
			logger.info("[10] batch get contact completed");
			syncServie.getMessageHandler().onContactCompleted();
			
			cacheService.setAlive(true);
			logger.info("[*] login process completed");
			logger.info("[*] start listening");
			wechatHttpServiceInternal.setSynccheckTimeMillis(System.currentTimeMillis());
			while (cacheService.isAlive()) {
				if(Thread.currentThread().isInterrupted()) {
					logger.info("[Logout] exit bye");
					return;
				}
				syncServie.listen();
			}
			logger.info("[*] exit bye");
		} catch (IOException | URISyntaxException ex) {
			ex.printStackTrace();
			logger.info("[Exception] exit bye");
		} catch (WechatQRExpiredException ex) {
			syncServie.getMessageHandler().onExpired();
			logger.info("[Expired] exit bye");
		}
    }
    
    public void logout() {
    	wechatHttpServiceInternal.logout();
    	if(cacheService.getOwner() != null)
    		syncServie.getMessageHandler().onLogout(cacheService.getOwner());
    }
    
}
