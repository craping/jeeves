package com.cherry.jeeves.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.cherry.jeeves.domain.request.AddChatRoomMemberRequest;
import com.cherry.jeeves.domain.request.BatchGetContactRequest;
import com.cherry.jeeves.domain.request.CreateChatRoomRequest;
import com.cherry.jeeves.domain.request.DeleteChatRoomMemberRequest;
import com.cherry.jeeves.domain.request.InitRequest;
import com.cherry.jeeves.domain.request.OpLogRequest;
import com.cherry.jeeves.domain.request.SendMsgRequest;
import com.cherry.jeeves.domain.request.StatReportRequest;
import com.cherry.jeeves.domain.request.StatusNotifyRequest;
import com.cherry.jeeves.domain.request.SyncRequest;
import com.cherry.jeeves.domain.request.UploadMediaRequest;
import com.cherry.jeeves.domain.request.VerifyUserRequest;
import com.cherry.jeeves.domain.request.component.BaseRequest;
import com.cherry.jeeves.domain.response.AddChatRoomMemberResponse;
import com.cherry.jeeves.domain.response.BatchGetContactResponse;
import com.cherry.jeeves.domain.response.CreateChatRoomResponse;
import com.cherry.jeeves.domain.response.DeleteChatRoomMemberResponse;
import com.cherry.jeeves.domain.response.GetContactResponse;
import com.cherry.jeeves.domain.response.InitResponse;
import com.cherry.jeeves.domain.response.LoginResult;
import com.cherry.jeeves.domain.response.OpLogResponse;
import com.cherry.jeeves.domain.response.SendMsgResponse;
import com.cherry.jeeves.domain.response.StatusNotifyResponse;
import com.cherry.jeeves.domain.response.SyncCheckResponse;
import com.cherry.jeeves.domain.response.SyncResponse;
import com.cherry.jeeves.domain.response.UploadMediaResponse;
import com.cherry.jeeves.domain.response.VerifyUserResponse;
import com.cherry.jeeves.domain.shared.BaseMsg;
import com.cherry.jeeves.domain.shared.ChatRoomDescription;
import com.cherry.jeeves.domain.shared.ChatRoomMember;
import com.cherry.jeeves.domain.shared.Message;
import com.cherry.jeeves.domain.shared.StatReport;
import com.cherry.jeeves.domain.shared.Token;
import com.cherry.jeeves.domain.shared.VerifyUser;
import com.cherry.jeeves.enums.AddScene;
import com.cherry.jeeves.enums.MessageType;
import com.cherry.jeeves.enums.OpLogCmdId;
import com.cherry.jeeves.enums.StatusNotifyCode;
import com.cherry.jeeves.enums.VerifyUserOPCode;
import com.cherry.jeeves.exception.WechatException;
import com.cherry.jeeves.utils.DeviceIdGenerator;
import com.cherry.jeeves.utils.HeaderUtils;
import com.cherry.jeeves.utils.RandomUtils;
import com.cherry.jeeves.utils.WechatUtils;
import com.cherry.jeeves.utils.rest.StatefullRestTemplate;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

@Component
class WechatHttpServiceInternal {

    @Value("${wechat.url.entry}")
    private String WECHAT_URL_ENTRY;
    @Value("${wechat.url.uuid}")
    private String WECHAT_URL_UUID;
    @Value("${wechat.url.qrcode}")
    private String WECHAT_URL_QRCODE;
    @Value("${wechat.url.status_notify}")
    private String WECHAT_URL_STATUS_NOTIFY;
    @Value("${wechat.url.statreport}")
    private String WECHAT_URL_STATREPORT;
    @Value("${wechat.url.login}")
    private String WECHAT_URL_LOGIN;
    @Value("${wechat.url.init}")
    private String WECHAT_URL_INIT;
    @Value("${wechat.url.sync_check}")
    private String WECHAT_URL_SYNC_CHECK;
    @Value("${wechat.url.sync}")
    private String WECHAT_URL_SYNC;
    @Value("${wechat.url.get_contact}")
    private String WECHAT_URL_GET_CONTACT;
    
    @Value("${wechat.url.upload_media}")
    private String WECHAT_URL_UPLOAD_MEDIA;
    @Value("${wechat.url.send_msg}")
    private String WECHAT_URL_SEND_MSG;
    @Value("${wechat.url.send_msg_img}")
    private String WECHAT_URL_SEND_MSG_IMG;
    @Value("${wechat.url.send_msg_emoticon}")
    private String WECHAT_URL_SEND_MSG_EMOTICON;
    @Value("${wechat.url.send_msg_video}")
    private String WECHAT_URL_SEND_MSG_VIDOE;
    @Value("${wechat.url.send_msg_app}")
    private String WECHAT_URL_SEND_MSG_APP;
    
    @Value("${wechat.url.get_msg_img}")
    private String WECHAT_URL_GET_MSG_IMG;
    @Value("${wechat.url.get_voice}")
    private String WECHAT_URL_GET_VOICE;
    @Value("${wechat.url.get_video}")
    private String WECHAT_URL_GET_VIDEO;
    @Value("${wechat.url.get_media}")
    private String WECHAT_URL_GET_MEDIA;
    
    @Value("${wechat.url.push_login}")
    private String WECHAT_URL_PUSH_LOGIN;
    @Value("${wechat.url.logout}")
    private String WECHAT_URL_LOGOUT;
    @Value("${wechat.url.batch_get_contact}")
    private String WECHAT_URL_BATCH_GET_CONTACT;
    @Value("${wechat.url.op_log}")
    private String WECHAT_URL_OP_LOG;
    @Value("${wechat.url.verify_user}")
    private String WECHAT_URL_VERIFY_USER;
    @Value("${wechat.url.create_chatroom}")
    private String WECHAT_URL_CREATE_CHATROOM;
    @Value("${wechat.url.delete_chatroom_member}")
    private String WECHAT_URL_DELETE_CHATROOM_MEMBER;
    @Value("${wechat.url.add_chatroom_member}")
    private String WECHAT_URL_ADD_CHATROOM_MEMBER;
    @Autowired
    private CacheService cacheService;
    
    private long synccheckTimeMillis;
    private long fileId = 0;
    private final RestTemplate restTemplate;
    private final HttpHeaders postHeader;
    private final HttpHeaders getHeader;
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final ObjectMapper xmlMapper = new XmlMapper();
    private String originValue = null;
    private String refererValue = null;
    private String BROWSER_DEFAULT_ACCEPT_LANGUAGE = "en,zh-CN;q=0.8,zh;q=0.6,ja;q=0.4,zh-TW;q=0.2";
    private String BROWSER_DEFAULT_ACCEPT_ENCODING = "gzip, deflate, br";
    private Map<String, String> cookies = new HashMap<>();

    @Autowired
    WechatHttpServiceInternal(RestTemplate restTemplate, @Value("${wechat.ua}") String BROWSER_DEFAULT_USER_AGENT) {
        this.restTemplate = restTemplate;
        this.postHeader = new HttpHeaders();
        postHeader.set(HttpHeaders.USER_AGENT, BROWSER_DEFAULT_USER_AGENT);
        postHeader.set(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        postHeader.setAccept(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, MediaType.ALL));
        postHeader.set(HttpHeaders.ACCEPT_LANGUAGE, BROWSER_DEFAULT_ACCEPT_LANGUAGE);
        postHeader.set(HttpHeaders.ACCEPT_ENCODING, BROWSER_DEFAULT_ACCEPT_ENCODING);
        this.getHeader = new HttpHeaders();
        getHeader.set(HttpHeaders.USER_AGENT, BROWSER_DEFAULT_USER_AGENT);
        getHeader.set(HttpHeaders.ACCEPT_LANGUAGE, BROWSER_DEFAULT_ACCEPT_LANGUAGE);
        getHeader.set(HttpHeaders.ACCEPT_ENCODING, BROWSER_DEFAULT_ACCEPT_ENCODING);
    }

    void logout() {
    	if(cacheService.isAlive()) {
	    	try {
		        final String url = String.format(WECHAT_URL_LOGOUT, cacheService.getHostUrl(), escape(cacheService.getsKey()));
		        restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(postHeader), Object.class);
	    	} catch (Exception e) {
	    		e.printStackTrace();
			}
	    	cacheService.setAlive(false);
    	}
    }

    /**
     * Open the entry page.
     *
     * @param retryTimes retry times of qr scan
     */
    void open(int retryTimes) {
        final String url = WECHAT_URL_ENTRY;
        HttpHeaders customHeader = new HttpHeaders();
        customHeader.setPragma("no-cache");
        customHeader.setCacheControl("no-cache");
        customHeader.set("Upgrade-Insecure-Requests", "1");
        customHeader.set(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        HeaderUtils.assign(customHeader, getHeader);
        restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(customHeader), String.class);
        //manually insert two cookies into cookiestore, as they're supposed to be stored in browsers by javascript.
        CookieStore store = (CookieStore) ((StatefullRestTemplate) restTemplate).getHttpContext().getAttribute(HttpClientContext.COOKIE_STORE);
        Date maxDate = new Date(Long.MAX_VALUE);
        String domain = WECHAT_URL_ENTRY.replaceAll("https://", "").replaceAll("/", "");
        cookies.put("MM_WX_NOTIFY_STATE", "1");
        cookies.put("MM_WX_SOUND_STATE", "1");
        if (retryTimes > 0) {
            cookies.put("refreshTimes", String.valueOf(retryTimes));
        }
        appendAdditionalCookies(store, cookies, domain, "/", maxDate);
        cookies = store.getCookies().stream().collect(Collectors.toMap(Cookie::getName, Cookie::getValue, (k1, k2)-> k2));
        //It's now at entry page.
        this.originValue = WECHAT_URL_ENTRY;
        this.refererValue = WECHAT_URL_ENTRY.replaceAll("/$", "");
    }

    /**
     * Get UUID for this session
     *
     * @return UUID
     */
    String getUUID() {
        final String regEx = "window.QRLogin.code = (\\d+); window.QRLogin.uuid = \"(\\S+?)\";";
        final String url = String.format(WECHAT_URL_UUID, System.currentTimeMillis());
        final String successCode = "200";
        HttpHeaders customHeader = new HttpHeaders();
        customHeader.setPragma("no-cache");
        customHeader.setCacheControl("no-cache");
        customHeader.setAccept(Arrays.asList(MediaType.ALL));
        customHeader.set(HttpHeaders.REFERER, WECHAT_URL_ENTRY);
        HeaderUtils.assign(customHeader, getHeader);
        ResponseEntity<String> responseEntity
                = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(customHeader), String.class);
        String body = responseEntity.getBody();
        Matcher matcher = Pattern.compile(regEx).matcher(body);
        if (matcher.find()) {
            if (successCode.equals(matcher.group(1))) {
                return matcher.group(2);
            }
        }
        throw new WechatException("uuid can't be found");
    }

    /**
     * Get QR code for scanning
     *
     * @param uuid UUID
     * @return QR code in binary
     */
    byte[] getQR(String uuid) {
        final String url = WECHAT_URL_QRCODE + "/" + uuid;
        HttpHeaders customHeader = new HttpHeaders();
        customHeader.set(HttpHeaders.ACCEPT, "image/webp,image/apng,image/*,*/*;q=0.8");
        customHeader.set(HttpHeaders.REFERER, WECHAT_URL_ENTRY);
        HeaderUtils.assign(customHeader, getHeader);
        ResponseEntity<byte[]> responseEntity
                = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(customHeader), new ParameterizedTypeReference<byte[]>() {
        });
        return responseEntity.getBody();
    }

    /**
     * Get hostUrl and redirectUrl
     *
     * @param uuid
     * @return hostUrl and redirectUrl
     * @throws WechatException if the response doesn't contain code
     */
    LoginResult login(String uuid) {
        Pattern pattern = Pattern.compile("window.code\\s?=\\s?(\\d+)");
        Pattern hostUrlPattern = Pattern.compile("window.redirect_uri\\s?=\\s?\\\"(.*)\\/cgi-bin");
        Pattern redirectUrlPattern = Pattern.compile("window.redirect_uri\\s?=\\s?\\\"(.*)\\\";");
        Pattern userAvatarPattern = Pattern.compile("window.userAvatar\\s?=\\s?'data:img/jpg;base64,(.*)';");
        long time = System.currentTimeMillis();
        final String url = String.format(WECHAT_URL_LOGIN, uuid, RandomUtils.generateDateWithBitwiseNot(time), time);
        HttpHeaders customHeader = new HttpHeaders();
        customHeader.setAccept(Arrays.asList(MediaType.ALL));
        customHeader.set(HttpHeaders.REFERER, WECHAT_URL_ENTRY);
        HeaderUtils.assign(customHeader, getHeader);
        ResponseEntity<String> responseEntity
                = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(customHeader), String.class);
        String body = responseEntity.getBody();
        Matcher matcher = pattern.matcher(body);
        LoginResult response = new LoginResult();
        if (matcher.find()) {
            response.setCode(matcher.group(1));
        } else {
//            throw new WechatException("code can't be found");
        }
        Matcher hostUrlMatcher = hostUrlPattern.matcher(body);
        if (hostUrlMatcher.find()) {
            response.setHostUrl(hostUrlMatcher.group(1));
        }
        Matcher redirectUrlMatcher = redirectUrlPattern.matcher(body);
        if (redirectUrlMatcher.find()) {
            response.setRedirectUrl(redirectUrlMatcher.group(1));
        }
        Matcher userAvatarMatcher = userAvatarPattern.matcher(body);
        if (userAvatarMatcher.find()) {
            response.setUserAvatar(userAvatarMatcher.group(1));
        }
        return response;
    }

    /**
     * Get basic parameters for this session
     *
     * @param redirectUrl
     * @return session token
     * @throws IOException if the http response body can't be convert to {@link Token}
     */
    Token openNewloginpage(String redirectUrl) throws IOException {
        HttpHeaders customHeader = new HttpHeaders();
        customHeader.set(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        customHeader.set(HttpHeaders.REFERER, WECHAT_URL_ENTRY);
        customHeader.set("Upgrade-Insecure-Requests", "1");
        HeaderUtils.assign(customHeader, getHeader);
        ResponseEntity<String> responseEntity
                = restTemplate.exchange(redirectUrl, HttpMethod.GET, new HttpEntity<>(customHeader), String.class);
        String xmlString = responseEntity.getBody();
        return xmlMapper.readValue(xmlString, Token.class);
    }

    /**
     * Redirect to main page of wechat
     *
     * @param hostUrl hostUrl
     */
    void redirect() {
        final String url = cacheService.getHostUrl() + "/";
        HttpHeaders customHeader = new HttpHeaders();
        customHeader.set(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        customHeader.set(HttpHeaders.REFERER, WECHAT_URL_ENTRY);
        customHeader.set("Upgrade-Insecure-Requests", "1");
        HeaderUtils.assign(customHeader, getHeader);
        restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(customHeader), String.class);
        //It's now at main page.
        this.originValue = cacheService.getHostUrl();
        this.refererValue = cacheService.getHostUrl() + "/";
    }

    /**
     * Initialization
     *
     * @param hostUrl     hostUrl
     * @param baseRequest baseRequest
     * @return current user's information and contact information
     * @throws IOException if the http response body can't be convert to {@link InitResponse}
     */
    InitResponse init() throws IOException {
        String url = String.format(WECHAT_URL_INIT, cacheService.getHostUrl(), RandomUtils.generateDateWithBitwiseNot(), cacheService.getPassTicket());

        CookieStore store = (CookieStore) ((StatefullRestTemplate) restTemplate).getHttpContext().getAttribute(HttpClientContext.COOKIE_STORE);
        Date maxDate = new Date(Long.MAX_VALUE);
        String domain = cacheService.getHostUrl().replaceAll("https://", "").replaceAll("/", "");
        cookies.put("MM_WX_NOTIFY_STATE", "1");
        cookies.put("MM_WX_SOUND_STATE", "1");
        appendAdditionalCookies(store, cookies, domain, "/", maxDate);
        InitRequest request = new InitRequest();
        request.setBaseRequest(cacheService.getBaseRequest());
        HttpHeaders customHeader = new HttpHeaders();
        customHeader.set(HttpHeaders.REFERER, cacheService.getHostUrl() + "/");
        customHeader.setOrigin(cacheService.getHostUrl());
        HeaderUtils.assign(customHeader, postHeader);
        ResponseEntity<String> responseEntity
                = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(request, customHeader), String.class);
        cookies = store.getCookies().stream().collect(Collectors.toMap(Cookie::getName, Cookie::getValue, (k1, k2)-> k2));
        return jsonMapper.readValue(WechatUtils.textDecode(responseEntity.getBody()), InitResponse.class);
    }

    /**
     * Notify mobile side once certain actions have been taken on web side.
     *
     * @param hostUrl     hostUrl
     * @param baseRequest baseRequest
     * @param userName    the userName of the user
     * @param code        {@link StatusNotifyCode}
     * @return the http response body
     * @throws IOException if the http response body can't be convert to {@link StatusNotifyResponse}
     */
    StatusNotifyResponse statusNotify(String toUserName, int code) throws IOException {
        String rnd = String.valueOf(System.currentTimeMillis());
        final String url = String.format(WECHAT_URL_STATUS_NOTIFY, cacheService.getHostUrl(), cacheService.getPassTicket());
        StatusNotifyRequest request = new StatusNotifyRequest();
        request.setBaseRequest(cacheService.getBaseRequest());
        request.setFromUserName(cacheService.getOwner().getUserName());
        request.setToUserName(toUserName);
        request.setCode(code);
        request.setClientMsgId(rnd);
        HttpHeaders customHeader = new HttpHeaders();
        customHeader.set(HttpHeaders.REFERER, cacheService.getHostUrl() + "/");
        customHeader.setOrigin(cacheService.getHostUrl());
        HeaderUtils.assign(customHeader, postHeader);
        ResponseEntity<String> responseEntity
                = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(request, customHeader), String.class);
        return jsonMapper.readValue(WechatUtils.textDecode(responseEntity.getBody()), StatusNotifyResponse.class);
    }

    /**
     * report stats to server
     */
    void statReport() {
        final String url = WECHAT_URL_STATREPORT;
        StatReportRequest request = new StatReportRequest();
        BaseRequest baseRequest = new BaseRequest();
        baseRequest.setUin("");
        baseRequest.setSid("");
        request.setBaseRequest(baseRequest);
        request.setCount(0);
        request.setList(new StatReport[0]);
        HttpHeaders customHeader = new HttpHeaders();
        customHeader.set(HttpHeaders.REFERER, WECHAT_URL_ENTRY);
        customHeader.setOrigin(WECHAT_URL_ENTRY.replaceAll("/$", ""));
        HeaderUtils.assign(customHeader, postHeader);
        restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(request, customHeader), String.class);
    }

    /**
     * Get all the contacts. If the Seq it returns is greater than zero, it means at least one more request is required to fetch all contacts.
     *
     * @param hostUrl hostUrl
     * @param skey    skey
     * @param seq     seq
     * @return contact information
     * @throws IOException if the http response body can't be convert to {@link GetContactResponse}
     */
    GetContactResponse getContact(long seq) throws IOException {
        long rnd = System.currentTimeMillis();
        final String url = String.format(WECHAT_URL_GET_CONTACT, cacheService.getHostUrl(), rnd, seq, escape(cacheService.getBaseRequest().getSkey()), cacheService.getPassTicket());
        HttpHeaders customHeader = new HttpHeaders();
        customHeader.setAccept(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, MediaType.ALL));
        customHeader.set(HttpHeaders.REFERER, cacheService.getHostUrl() + "/");
        HeaderUtils.assign(customHeader, getHeader);
        ResponseEntity<String> responseEntity
                = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(customHeader), String.class);
        return jsonMapper.readValue(WechatUtils.textDecode(responseEntity.getBody()), GetContactResponse.class);
    }

    /**
     * Get all the members in the given chatrooms
     *
     * @param hostUrl     hostUrl
     * @param baseRequest baseRequest
     * @param list        chatroom information
     * @return chatroom members information
     * @throws IOException if the http response body can't be convert to {@link BatchGetContactResponse}
     */
    BatchGetContactResponse batchGetContact(ChatRoomDescription[] list) {
        long rnd = System.currentTimeMillis();
        String url = String.format(WECHAT_URL_BATCH_GET_CONTACT, cacheService.getHostUrl(), rnd, cacheService.getPassTicket());
        BatchGetContactRequest request = new BatchGetContactRequest();
        request.setBaseRequest(cacheService.getBaseRequest());
        request.setCount(list.length);
        request.setList(list);
        HttpHeaders customHeader = createPostCustomHeader();
        HeaderUtils.assign(customHeader, postHeader);
        ResponseEntity<String> responseEntity
                = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(request, customHeader), String.class);
        try {
			return jsonMapper.readValue(WechatUtils.textDecode(responseEntity.getBody()), BatchGetContactResponse.class);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
    }

    /**
     * Periodically request to server
     *
     * @param hostUrl hostUrl
     * @param uin     uin
     * @param sid     sid
     * @param skey    skey
     * @param syncKey syncKey
     * @return synccheck response
     * @throws IOException        if the http response body can't be convert to {@link SyncCheckResponse}
     * @throws URISyntaxException if url is invalid
     */
    SyncCheckResponse syncCheck() throws IOException, URISyntaxException {
        final Pattern pattern = Pattern.compile("window.synccheck=\\{retcode:\"(\\d+)\",selector:\"(\\d+)\"\\}");
        final String path = String.format(WECHAT_URL_SYNC_CHECK, cacheService.getSyncUrl());
//        if(cacheService.getSyncKey() != null && cacheService.getSyncCheckKey() != null) {
//	        System.out.println("syncCheck 请求[SyncKey]:"+cacheService.getSyncKey().toString());
//	        System.out.println("syncCheck 请求[SyncCheckKey]:"+cacheService.getSyncCheckKey().toString());
//        }
        URIBuilder builder = new URIBuilder(path);
        BaseRequest baseRequest = cacheService.getBaseRequest();
        builder.addParameter("uin", baseRequest.getUin());
        builder.addParameter("sid", baseRequest.getSid());
        builder.addParameter("skey", baseRequest.getSkey());
        builder.addParameter("deviceid", DeviceIdGenerator.generate());
        builder.addParameter("synckey", cacheService.getSyncCheckKey() == null?cacheService.getSyncKey().toString():cacheService.getSyncCheckKey().toString());
        builder.addParameter("r", String.valueOf(System.currentTimeMillis()));
        builder.addParameter("_", String.valueOf(synccheckTimeMillis++));
        final URI uri = builder.build().toURL().toURI();
        HttpHeaders customHeader = new HttpHeaders();
        customHeader.setAccept(Arrays.asList(MediaType.ALL));
        customHeader.set(HttpHeaders.REFERER, cacheService.getSyncUrl() + "/");
        HeaderUtils.assign(customHeader, getHeader);
        try {
	        ResponseEntity<String> responseEntity
	                = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(customHeader), String.class);
	        String body = responseEntity.getBody();
	        Matcher matcher = pattern.matcher(body);
	        if (!matcher.find()) {
	            return null;
	        } else {
	            SyncCheckResponse result = new SyncCheckResponse();
	            result.setRetcode(Integer.valueOf(matcher.group(1)));
	            result.setSelector(Integer.valueOf(matcher.group(2)));
	            return result;
	        }
        } catch (Exception e) {
        	e.printStackTrace();
        	return null;
		}
    }

    /**
     * Sync with server to get new messages and contacts
     *
     * @param hostUrl     hostUrl
     * @param syncKey     syncKey
     * @param baseRequest baseRequest
     * @return new messages and contacts
     * @throws IOException if the http response body can't be convert to {@link SyncResponse}
     */
    SyncResponse sync() throws IOException {
    	BaseRequest baseRequest = cacheService.getBaseRequest();
//    	if(cacheService.getSyncKey() != null && cacheService.getSyncCheckKey() != null) {
//	    	System.out.println("sync 请求[SyncKey]:"+cacheService.getSyncKey().toString());
//	        System.out.println("sync 请求[SyncCheckKey]:"+cacheService.getSyncCheckKey().toString());
//    	}
        final String url = String.format(WECHAT_URL_SYNC, cacheService.getHostUrl(), baseRequest.getSid(), escape(baseRequest.getSkey()), cacheService.getPassTicket());
        SyncRequest request = new SyncRequest();
        request.setBaseRequest(baseRequest);
        request.setRr(-System.currentTimeMillis() / 1000);
        request.setSyncKey(cacheService.getSyncKey());
        HttpHeaders customHeader = createPostCustomHeader();
        HeaderUtils.assign(customHeader, postHeader);
        CookieStore store = (CookieStore) ((StatefullRestTemplate) restTemplate).getHttpContext().getAttribute(HttpClientContext.COOKIE_STORE);
        cookies = store.getCookies().stream().collect(Collectors.toMap(Cookie::getName, Cookie::getValue, (k1, k2)-> k2));
        ResponseEntity<String> responseEntity
                = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(request, customHeader), String.class);
        return jsonMapper.readValue(WechatUtils.textDecode(responseEntity.getBody()), SyncResponse.class);
    }

    VerifyUserResponse acceptFriend(VerifyUser[] verifyUsers) throws IOException, URISyntaxException {
    	BaseRequest baseRequest = cacheService.getBaseRequest();
        final int opCode = VerifyUserOPCode.VERIFYOK.getCode();
        final int[] sceneList = new int[]{AddScene.WEB.getCode()};
        final String path = String.format(WECHAT_URL_VERIFY_USER, cacheService.getHostUrl());
        VerifyUserRequest request = new VerifyUserRequest();
        request.setBaseRequest(baseRequest);
        request.setOpcode(opCode);
        request.setSceneList(sceneList);
        request.setSceneListCount(sceneList.length);
        request.setSkey(baseRequest.getSkey());
        request.setVerifyContent("");
        request.setVerifyUserList(verifyUsers);
        request.setVerifyUserListSize(verifyUsers.length);

        URIBuilder builder = new URIBuilder(path);
        builder.addParameter("r", String.valueOf(System.currentTimeMillis()));
        builder.addParameter("pass_ticket", cacheService.getPassTicket());
        final URI uri = builder.build().toURL().toURI();

        ResponseEntity<String> responseEntity
                = restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(request, this.postHeader), String.class);
        return jsonMapper.readValue(WechatUtils.textDecode(responseEntity.getBody()), VerifyUserResponse.class);
    }
    
    UploadMediaResponse uploadMedia(String toUserName, String filePath) throws IOException {
    	File f = new File(filePath);
		if (!f.exists() && f.isFile()) {
			return null;
		}
		String mimeType = Files.probeContentType(f.toPath());
		String mediaType = "";
		if (mimeType == null) {
			mimeType = "application/octet-stream";
			mediaType = "doc";
		} else if (mimeType.split("/")[0].equals("image")) {
			mediaType = "pic";
		} else if (mimeType.split("/")[0].equals("video")) {
			mediaType = "video";
		} else {
			mediaType = "doc";
		}
		FileSystemResource resource = new FileSystemResource(f);
		
		final long rnd = System.currentTimeMillis() * 10;
        final String url = String.format(WECHAT_URL_UPLOAD_MEDIA, cacheService.getFileUrl());
        UploadMediaRequest uploadmediarequest = new UploadMediaRequest();
        uploadmediarequest.setBaseRequest(cacheService.getBaseRequest());
        uploadmediarequest.setFromUserName(cacheService.getOwner().getUserName());
        uploadmediarequest.setToUserName(toUserName);
        uploadmediarequest.setClientMediaId(rnd);
        uploadmediarequest.setFileMd5(DigestUtils.md5Hex(new FileInputStream(f)));
        uploadmediarequest.setDataLen(f.length());
        uploadmediarequest.setTotalLen(f.length());
        uploadmediarequest.setMediaType(4);
        uploadmediarequest.setUploadType(2);
        
        
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("id", "WU_FILE_"+(fileId++));
        form.add("name", f.getName());
        form.add("type", mimeType);
        form.add("lastModifieDate", new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT 0800 (中国标准时间)'", Locale.US).format(new Timestamp(f.lastModified())));
        form.add("size", String.valueOf(f.length()));
        form.add("mediatype", mediaType);
        form.add("uploadmediarequest", jsonMapper.writeValueAsString(uploadmediarequest) );
        form.add("webwx_data_ticket", cookies.get("webwx_data_ticket"));
        form.add("pass_ticket", cacheService.getPassTicket());
        form.add("filename", resource);
        
        HttpHeaders customHeader = createPostCustomHeader();
        HeaderUtils.assign(customHeader, postHeader);
        customHeader.setContentType(MediaType.MULTIPART_FORM_DATA);
        ResponseEntity<String> responseEntity
                = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(form, customHeader), String.class);
        UploadMediaResponse response = jsonMapper.readValue(WechatUtils.textDecode(responseEntity.getBody()), UploadMediaResponse.class);
        response.setTitle(f.getName());
    	response.setFileext(f.getName().contains(".")?f.getName().split("[.]")[1]:"");
        return response;
    }
    
    SendMsgResponse sendText(String toUserName, String content) throws IOException {
        final int scene = 0;
        final String rnd = System.currentTimeMillis() + String.valueOf((int)(Math.random() * 1e4));
        final String url = String.format(WECHAT_URL_SEND_MSG, cacheService.getHostUrl(), cacheService.getPassTicket());
        SendMsgRequest request = new SendMsgRequest();
        request.setBaseRequest(cacheService.getBaseRequest());
        request.setScene(scene);
        BaseMsg msg = new BaseMsg();
        msg.setType(MessageType.TEXT.getCode());
        msg.setClientMsgId(rnd);
        msg.setContent(content);
        msg.setFromUserName(cacheService.getOwner().getUserName());
        msg.setToUserName(toUserName);
        msg.setLocalID(rnd);
        request.setMsg(msg);
        HttpHeaders customHeader = createPostCustomHeader();
        HeaderUtils.assign(customHeader, postHeader);
        ResponseEntity<String> responseEntity
                = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(request, customHeader), String.class);
        return jsonMapper.readValue(WechatUtils.textDecode(responseEntity.getBody()), SendMsgResponse.class);
    }
    
    SendMsgResponse sendImage(String toUserName, String imgUrl) throws IOException {
    	UploadMediaResponse media = uploadMedia(toUserName, imgUrl);
        final int scene = 0;
        final String rnd = System.currentTimeMillis() + String.valueOf((int)(Math.random() * 1e4));
        final String url = String.format(WECHAT_URL_SEND_MSG_IMG, cacheService.getHostUrl(), cacheService.getPassTicket());
        SendMsgRequest request = new SendMsgRequest();
        request.setBaseRequest(cacheService.getBaseRequest());
        request.setScene(scene);
        BaseMsg msg = new BaseMsg();
        msg.setType(MessageType.IMAGE.getCode());
        msg.setClientMsgId(rnd);
        msg.setContent("");
        msg.setFromUserName(cacheService.getOwner().getUserName());
        msg.setToUserName(toUserName);
        msg.setLocalID(rnd);
        msg.setMediaId(media.getMediaId());
        request.setMsg(msg);
        HttpHeaders customHeader = createPostCustomHeader();
        HeaderUtils.assign(customHeader, postHeader);
        ResponseEntity<String> responseEntity
                = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(request, customHeader), String.class);
        return jsonMapper.readValue(WechatUtils.textDecode(responseEntity.getBody()), SendMsgResponse.class);
    }
    
    SendMsgResponse sendEmoticon(String toUserName, String emoticonUrl) throws IOException {
    	UploadMediaResponse media = uploadMedia(toUserName, emoticonUrl);
        final int scene = 0;
        final String rnd = System.currentTimeMillis() + String.valueOf((int)(Math.random() * 1e4));
        final String url = String.format(WECHAT_URL_SEND_MSG_EMOTICON, cacheService.getHostUrl(), cacheService.getPassTicket());
        SendMsgRequest request = new SendMsgRequest();
        request.setBaseRequest(cacheService.getBaseRequest());
        request.setScene(scene);
        BaseMsg msg = new BaseMsg();
        msg.setType(MessageType.EMOTICON.getCode());
        msg.setEmojiFlag(2);
        msg.setClientMsgId(rnd);
        msg.setFromUserName(cacheService.getOwner().getUserName());
        msg.setToUserName(toUserName);
        msg.setLocalID(rnd);
        msg.setMediaId(media.getMediaId());
        request.setMsg(msg);
        HttpHeaders customHeader = createPostCustomHeader();
        HeaderUtils.assign(customHeader, postHeader);
        ResponseEntity<String> responseEntity
                = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(request, customHeader), String.class);
        return jsonMapper.readValue(WechatUtils.textDecode(responseEntity.getBody()), SendMsgResponse.class);
    }
    
    SendMsgResponse sendVideo(String toUserName, String videoUrl) throws IOException {
    	UploadMediaResponse media = uploadMedia(toUserName, videoUrl);
        final int scene = 0;
        final String rnd = System.currentTimeMillis() + String.valueOf((int)(Math.random() * 1e4));
        final String url = String.format(WECHAT_URL_SEND_MSG_VIDOE, cacheService.getHostUrl(), cacheService.getPassTicket());
        SendMsgRequest request = new SendMsgRequest();
        request.setBaseRequest(cacheService.getBaseRequest());
        request.setScene(scene);
        BaseMsg msg = new BaseMsg();
        msg.setType(MessageType.VIDEO.getCode());
        msg.setClientMsgId(rnd);
        msg.setFromUserName(cacheService.getOwner().getUserName());
        msg.setContent("");
        msg.setToUserName(toUserName);
        msg.setLocalID(rnd);
        msg.setMediaId(media.getMediaId());
        request.setMsg(msg);
        HttpHeaders customHeader = createPostCustomHeader();
        HeaderUtils.assign(customHeader, postHeader);
        ResponseEntity<String> responseEntity
                = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(request, customHeader), String.class);
        return jsonMapper.readValue(WechatUtils.textDecode(responseEntity.getBody()), SendMsgResponse.class);
    }
    
    SendMsgResponse sendApp(String toUserName, String mediaUrl) throws IOException {
    	UploadMediaResponse media = uploadMedia(toUserName, mediaUrl);
        final int scene = 0;
        final String rnd = System.currentTimeMillis() + String.valueOf((int)(Math.random() * 1e4));
        final String url = String.format(WECHAT_URL_SEND_MSG_APP, cacheService.getHostUrl(), cacheService.getPassTicket());
        SendMsgRequest request = new SendMsgRequest();
        request.setBaseRequest(cacheService.getBaseRequest());
        request.setScene(scene);
        BaseMsg msg = new BaseMsg();
        msg.setType(6);
        msg.setClientMsgId(rnd);
        msg.setContent("<appmsg appid='wxeb7ec651dd0aefa9' sdkver=''><title>" + media.getTitle()
		+ "</title><des></des><action></action><type>6</type><content></content><url></url><lowurl></lowurl>"
		+ "<appattach><totallen>" + media.getStartPos() + "</totallen><attachid>" + media.getMediaId()
		+ "</attachid><fileext>" + media.getFileext() + "</fileext></appattach><extinfo></extinfo></appmsg>");
        msg.setFromUserName(cacheService.getOwner().getUserName());
        msg.setToUserName(toUserName);
        msg.setLocalID(rnd);
        request.setMsg(msg);
        HttpHeaders customHeader = createPostCustomHeader();
        HeaderUtils.assign(customHeader, postHeader);
        ResponseEntity<String> responseEntity
                = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(request, customHeader), String.class);
        return jsonMapper.readValue(WechatUtils.textDecode(responseEntity.getBody()), SendMsgResponse.class);
    }

    SendMsgResponse forwardMsg(String toUserName, Message message) throws IOException {
//    	statusNotify(message.getFromUserName(), StatusNotifyCode.READED.getCode());
//    	statusNotify(toUserName, StatusNotifyCode.READED.getCode());
        final int scene = 2;
        String url;
        if(message.getMsgType() == MessageType.TEXT.getCode()) {
        	url = String.format(WECHAT_URL_SEND_MSG, cacheService.getHostUrl(), cacheService.getPassTicket());
        } else if (message.getMsgType() == MessageType.IMAGE.getCode()) {
			url = String.format(WECHAT_URL_SEND_MSG_IMG, cacheService.getHostUrl(), cacheService.getPassTicket());
		} else if (message.getMsgType() == MessageType.EMOTICON.getCode()) {
			url = String.format(WECHAT_URL_SEND_MSG_EMOTICON, cacheService.getHostUrl(), cacheService.getPassTicket());
		} else if (message.getMsgType() == MessageType.VOICE.getCode() || message.getMsgType() == MessageType.APP.getCode()) {
			url = String.format(WECHAT_URL_SEND_MSG_APP, cacheService.getHostUrl(), cacheService.getPassTicket());
		} else if (message.getMsgType() == MessageType.VIDEO.getCode() || message.getMsgType() == MessageType.MICROVIDEO.getCode()) {
			url = String.format(WECHAT_URL_SEND_MSG_VIDOE, cacheService.getHostUrl(), cacheService.getPassTicket());
		} else {
			return null;
		}
		
		final String rnd = System.currentTimeMillis() + String.valueOf((int)(Math.random() * 1e4));
        SendMsgRequest request = new SendMsgRequest();
        request.setBaseRequest(cacheService.getBaseRequest());
        request.setScene(scene);
        BaseMsg msg = new BaseMsg();
        if (message.getMsgType() == MessageType.VOICE.getCode()) {
        	String voiceUrl = String.format(WECHAT_URL_GET_VOICE, cacheService.getHostUrl(), message.getMsgId(), cacheService.getsKey());
        	String path = download(voiceUrl, message.getMsgId()+".mp3", MessageType.VOICE);
        	return sendApp(toUserName, path);
        } else if (message.getMsgType() == MessageType.APP.getCode()) {
        	msg.setType(6);
        	msg.setContent("<appmsg appid='wxeb7ec651dd0aefa9' sdkver=''><title>" + message.getFileName()
    		+ "</title><des></des><action></action><type>6</type><content></content><url></url><lowurl></lowurl>"
    		+ "<appattach><totallen>" + message.getFileSize() + "</totallen><attachid>" + message.getMediaId()
    		+ "</attachid><fileext>" + (message.getFileName().contains(".")?message.getFileName().split("[.]")[1]:"") + "</fileext></appattach><extinfo></extinfo></appmsg>");
        } if(message.getMsgType() == MessageType.EMOTICON.getCode()) {
        	msg.setType(message.getMsgType());
        	String xml = message.getContent().replace("<br/>", "").replace("&lt;", "<").replace("&gt;", ">").replace("&#39;", "'").replace("&quot;", "\"").replace("&amp;", "&");
        	Map<String, Map<String, String>> appmsg = xmlMapper.readValue(xml, new TypeReference<Map<String, Map<String, String>>>() {});
        	msg.setEMoticonMd5(appmsg.get("emoji").get("md5"));
        } else {
        	msg.setType(message.getMsgType());
        	msg.setContent(message.getContent());
        }
        msg.setMediaId(message.getMediaId());
        msg.setClientMsgId(rnd);
        msg.setFromUserName(cacheService.getOwner().getUserName());
        msg.setToUserName(toUserName);
        msg.setLocalID(rnd);
        request.setMsg(msg);
        HttpHeaders customHeader = createPostCustomHeader();
        HeaderUtils.assign(customHeader, postHeader);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(request, customHeader), String.class);
        return jsonMapper.readValue(WechatUtils.textDecode(responseEntity.getBody()), SendMsgResponse.class);
    }
    
    OpLogResponse setAlias(String newAlias, String userName) throws IOException {
        final int cmdId = OpLogCmdId.MODREMARKNAME.getCode();
        final String url = String.format(WECHAT_URL_OP_LOG, cacheService.getHostUrl());
        OpLogRequest request = new OpLogRequest();
        request.setBaseRequest(cacheService.getBaseRequest());
        request.setCmdId(cmdId);
        request.setRemarkName(newAlias);
        request.setUserName(userName);
        HttpHeaders customHeader = createPostCustomHeader();
        HeaderUtils.assign(customHeader, postHeader);
        ResponseEntity<String> responseEntity
                = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(request, customHeader), String.class);
        return jsonMapper.readValue(WechatUtils.textDecode(responseEntity.getBody()), OpLogResponse.class);
    }

    CreateChatRoomResponse createChatRoom(String[] userNames, String topic) throws IOException {
        String rnd = String.valueOf(System.currentTimeMillis());
        final String url = String.format(WECHAT_URL_CREATE_CHATROOM, cacheService.getHostUrl(), rnd);
        CreateChatRoomRequest request = new CreateChatRoomRequest();
        request.setBaseRequest(cacheService.getBaseRequest());
        request.setMemberCount(userNames.length);
        ChatRoomMember[] members = new ChatRoomMember[userNames.length];
        for (int i = 0; i < userNames.length; i++) {
            members[i] = new ChatRoomMember();
            members[i].setUserName(userNames[i]);
        }
        request.setMemberList(members);
        request.setTopic(topic);
        HttpHeaders customHeader = createPostCustomHeader();
        HeaderUtils.assign(customHeader, postHeader);
        ResponseEntity<String> responseEntity
                = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(request, customHeader), String.class);
        return jsonMapper.readValue(WechatUtils.textDecode(responseEntity.getBody()), CreateChatRoomResponse.class);
    }

    DeleteChatRoomMemberResponse deleteChatRoomMember(String chatRoomUserName, String userName) throws IOException {
        final String url = String.format(WECHAT_URL_DELETE_CHATROOM_MEMBER, cacheService.getHostUrl());
        DeleteChatRoomMemberRequest request = new DeleteChatRoomMemberRequest();
        request.setBaseRequest(cacheService.getBaseRequest());
        request.setChatRoomName(chatRoomUserName);
        request.setDelMemberList(userName);
        HttpHeaders customHeader = createPostCustomHeader();
        HeaderUtils.assign(customHeader, postHeader);
        ResponseEntity<String> responseEntity
                = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(request, customHeader), String.class);
        return jsonMapper.readValue(WechatUtils.textDecode(responseEntity.getBody()), DeleteChatRoomMemberResponse.class);
    }

    AddChatRoomMemberResponse addChatRoomMember(String chatRoomUserName, String userName) throws IOException {
        final String url = String.format(WECHAT_URL_ADD_CHATROOM_MEMBER, cacheService.getHostUrl());
        AddChatRoomMemberRequest request = new AddChatRoomMemberRequest();
        request.setBaseRequest(cacheService.getBaseRequest());
        request.setChatRoomName(chatRoomUserName);
        request.setAddMemberList(userName);
        HttpHeaders customHeader = createPostCustomHeader();
        HeaderUtils.assign(customHeader, postHeader);
        ResponseEntity<String> responseEntity
                = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(request, customHeader), String.class);
        return jsonMapper.readValue(WechatUtils.textDecode(responseEntity.getBody()), AddChatRoomMemberResponse.class);
    }

    String download(String url, String fileName, MessageType type) {
        HttpHeaders customHeader = new HttpHeaders();
        String path = "resource";
        switch (type) {
		case IMAGE:
		case EMOTICON:
			path+="/img";
			customHeader.set("Accept", "image/webp,image/apng,image/*,*/*;q=0.8");
	        customHeader.set("Referer", this.refererValue);
			break;
		case VOICE:
			path+="/voice";
			customHeader.set("Accept", "*/*");
	        customHeader.set("Referer", this.refererValue);
			break;
		case VIDEO:
		case APP:
			path+="/file";
			customHeader.set("Range", "bytes=0-");
			customHeader.set("Accept", "*/*");
	        customHeader.set("Referer", this.refererValue);
			break;
		default:
			return null;
		}
        File qrFile = new File(path);
		if(!qrFile.exists())
			qrFile.mkdirs();
       
        HeaderUtils.assign(customHeader, getHeader);
        ResponseEntity<byte[]> responseEntity = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(customHeader), new ParameterizedTypeReference<byte[]>() {
        });
        path = path+"/"+fileName;
        try {
			OutputStream out = new FileOutputStream(path);
			out.write(responseEntity.getBody());
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
        return path;
    }

    private String escape(String str) throws IOException {
        return URLEncoder.encode(str, StandardCharsets.UTF_8.toString());
    }

    private void appendAdditionalCookies(CookieStore store, Map<String, String> cookies, String domain, String path, Date expiryDate) {
        cookies.forEach((key, value) -> {
            BasicClientCookie cookie = new BasicClientCookie(key, value);
            cookie.setDomain(domain);
            cookie.setPath(path);
            cookie.setExpiryDate(expiryDate);
            store.addCookie(cookie);
        });
    }

    private HttpHeaders createPostCustomHeader() {
        HttpHeaders customHeader = new HttpHeaders();
        customHeader.setOrigin(this.originValue);
        customHeader.set(HttpHeaders.REFERER, this.refererValue);
        return customHeader;
    }

	public long getSynccheckTimeMillis() {
		return synccheckTimeMillis;
	}

	public void setSynccheckTimeMillis(long synccheckTimeMillis) {
		this.synccheckTimeMillis = synccheckTimeMillis;
	}

	public Map<String, String> getCookies() {
		return cookies;
	}

	public void setCookies(Map<String, String> cookies) {
		this.cookies = cookies;
	}
	
}