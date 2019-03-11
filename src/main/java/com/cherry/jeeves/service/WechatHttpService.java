package com.cherry.jeeves.service;

import com.cherry.jeeves.domain.response.*;
import com.cherry.jeeves.domain.shared.ChatRoomDescription;
import com.cherry.jeeves.domain.shared.Contact;
import com.cherry.jeeves.domain.shared.Message;
import com.cherry.jeeves.enums.MessageType;
import com.cherry.jeeves.enums.StatusNotifyCode;
import com.cherry.jeeves.exception.WechatException;
import com.cherry.jeeves.utils.WechatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class WechatHttpService {

    @Autowired
    private WechatHttpServiceInternal wechatHttpServiceInternal;
    @Autowired
    private CacheService cacheService;
    @Autowired(required = false)
    private MessageHandler messageHandler;
    /**
     * Log out
     *
     * @throws IOException if logout fails
     */
    public void logout() throws IOException {
        wechatHttpServiceInternal.logout();
    }

    /**
     * Get all the contacts
     *
     * @return contacts
     * @throws IOException if getContact fails
     */
    public Set<Contact> getContact() throws IOException {
        Set<Contact> contacts = new HashSet<>();
        long seq = 0;
        do {
            GetContactResponse response = wechatHttpServiceInternal.getContact(seq);
            WechatUtils.checkBaseResponse(response);
            seq = response.getSeq();
            contacts.addAll(response.getMemberList());
        }
        while (seq > 0);
        return contacts;
    }
    
    public UploadMediaResponse uploadMedia(String toUserName, String filePath) throws IOException {
    	return wechatHttpServiceInternal.uploadMedia(toUserName, filePath);
    }
    /**
     * Send plain text to a contact (not chatroom)
     *
     * @param userName the username of the contact
     * @param content  the content of text
     * @throws IOException if sendText fails
     */
    public SendMsgResponse sendText(String userName, String content) throws IOException {
        notifyNecessary(userName);
        return wechatHttpServiceInternal.sendText(userName, content);
    }
    
    
    public SendMsgResponse sendImage(String userName, String imgUrl) throws IOException {
        notifyNecessary(userName);
        return wechatHttpServiceInternal.sendImage(userName, imgUrl);
    }
    public SendMsgResponse sendImage(String userName, UploadMediaResponse media, int scene) throws IOException {
        notifyNecessary(userName);
        return wechatHttpServiceInternal.sendImage(userName, media, scene);
    }
    
    
    
    public SendMsgResponse sendEmoticon(String userName, String emoticonUrl) throws IOException {
        notifyNecessary(userName);
        return wechatHttpServiceInternal.sendEmoticon(userName, emoticonUrl);
    }
    public SendMsgResponse sendEmoticon(String userName, UploadMediaResponse media, int scene) throws IOException {
        notifyNecessary(userName);
        return wechatHttpServiceInternal.sendEmoticon(userName, media, scene);
    }
    
    
    
    public SendMsgResponse sendVideo(String userName, String videoUrl) throws IOException {
        notifyNecessary(userName);
        return wechatHttpServiceInternal.sendVideo(userName, videoUrl);
    }
    public SendMsgResponse sendVideo(String userName, UploadMediaResponse media, int scene) throws IOException {
        notifyNecessary(userName);
        return wechatHttpServiceInternal.sendVideo(userName, media, scene);
    }
    
    
    
    public SendMsgResponse sendApp(String userName, String mediaUrl) throws IOException {
        notifyNecessary(userName);
        return wechatHttpServiceInternal.sendApp(userName, mediaUrl);
    }
    public SendMsgResponse sendApp(String userName, UploadMediaResponse media, int scene) throws IOException {
        notifyNecessary(userName);
        return wechatHttpServiceInternal.sendApp(userName, media, scene);
    }
    
    
    
    public SendMsgResponse forwardMsg(String userName, Message message) throws IOException {
        notifyNecessary(userName);
        return wechatHttpServiceInternal.forwardMsg(userName, message);
    }
    public SendMsgResponse forwardAttachMsg(String userName, UploadMediaResponse media, MessageType msgType) throws IOException {
        notifyNecessary(userName);
        return wechatHttpServiceInternal.forwardAttachMsg(userName, media, msgType);
    }
    
    
    /**
     * Set the alias of a contact
     *
     * @param userName the username of the contact
     * @param newAlias alias
     * @throws IOException if setAlias fails
     */
    public void setAlias(String userName, String newAlias) throws IOException {
        OpLogResponse response = wechatHttpServiceInternal.setAlias(newAlias, userName);
        WechatUtils.checkBaseResponse(response);
    }

    /**
     * Get contacts in chatrooms
     *
     * @param list chatroom usernames
     * @return chatroom list
     * @throws IOException if batchGetContact fails
     */
    public Contact getChatRoomInfo(String chatRoomId) throws IOException {
    	Contact chatRoom = null;
    	for (Contact c : cacheService.getChatRooms()) {
			if(c.getUserName().equals(chatRoomId)){
				chatRoom = c;
				break;
			}
		}
    	if(chatRoom == null)
    		return null;
    	
    	String encryChatRoomId = chatRoom.getEncryChatRoomId();
    	
    	if(encryChatRoomId == null || encryChatRoomId.isEmpty()){
    		ChatRoomDescription description = new ChatRoomDescription();
			description.setUserName(chatRoom.getUserName());
    		ChatRoomDescription[] descriptions = new ChatRoomDescription[]{description};
    		BatchGetContactResponse response = wechatHttpServiceInternal.batchGetContact(descriptions);
    		for (Contact c : response.getContactList()) {
    			if(!chatRoom.getSeq().equals(c.getSeq())){
    				Map<String, String> seqMap = new HashMap<>();
					seqMap.put(chatRoom.getSeq(), c.getSeq());
					if (messageHandler != null) {
			            messageHandler.onMembersSeqChanged(seqMap);
			        }
				}
    			cacheService.getChatRooms().remove(c);
    			cacheService.getChatRooms().add(c);
    			chatRoom = c;
			}
    	}
    	
		while (true) {
			ChatRoomDescription[] descriptions = chatRoom.getMemberList().stream().filter(m -> m.getHeadImgUrl() == null || m.getHeadImgUrl().isEmpty())
				.limit(50)
				.map(x -> {
					ChatRoomDescription description = new ChatRoomDescription();
					description.setEncryChatRoomId(encryChatRoomId);
					description.setUserName(x.getUserName());
					return description;
				}).toArray(ChatRoomDescription[]::new);
			if (descriptions.length > 0) {
				BatchGetContactResponse response = wechatHttpServiceInternal.batchGetContact(descriptions);
				for (Contact c : response.getContactList()) {
					chatRoom.getMemberList().remove(c);
					chatRoom.getMemberList().add(c);
				}
			} else {
				break;
			}
		}
        return chatRoom;
    }

    /**
     * Create a chatroom with a topic.
     * In fact, a topic is usually not provided when creating the chatroom.
     *
     * @param userNames the usernames of the contacts who are invited to the chatroom.
     * @param topic     the topic(or nickname)
     * @throws IOException
     */
    public void createChatRoom(String[] userNames, String topic) throws IOException {
        CreateChatRoomResponse response = wechatHttpServiceInternal.createChatRoom(userNames, topic);
        WechatUtils.checkBaseResponse(response);
        //invoke BatchGetContact after CreateChatRoom
        ChatRoomDescription description = new ChatRoomDescription();
        description.setUserName(response.getChatRoomName());
        ChatRoomDescription[] descriptions = new ChatRoomDescription[]{description};
        BatchGetContactResponse batchGetContactResponse = wechatHttpServiceInternal.batchGetContact(descriptions);
        WechatUtils.checkBaseResponse(batchGetContactResponse);
        cacheService.getChatRooms().addAll(batchGetContactResponse.getContactList());
    }

    /**
     * Delete a contact from a certain chatroom (if you're the owner!)
     *
     * @param chatRoomUserName chatroom username
     * @param userName         contact username
     * @throws IOException if remove chatroom member fails
     */
    public void deleteChatRoomMember(String chatRoomUserName, String userName) throws IOException {
        DeleteChatRoomMemberResponse response = wechatHttpServiceInternal.deleteChatRoomMember(chatRoomUserName, userName);
        WechatUtils.checkBaseResponse(response);
    }

    /**
     * Invite a contact to a certain chatroom
     *
     * @param chatRoomUserName chatroom username
     * @param userName         contact username
     * @throws IOException if add chatroom member fails
     */
    public void addChatRoomMember(String chatRoomUserName, String userName) throws IOException {
        AddChatRoomMemberResponse response = wechatHttpServiceInternal.addChatRoomMember(chatRoomUserName, userName);
        WechatUtils.checkBaseResponse(response);
        Contact chatRoom = cacheService.getChatRooms().stream().filter(x -> chatRoomUserName.equals(x.getUserName())).findFirst().orElse(null);
        if (chatRoom == null) {
            throw new WechatException("can't find " + chatRoomUserName);
        }
        chatRoom.getMemberList().addAll(response.getMemberList());
    }

    /**
     * download images in the conversation. Note that it's better not to download image directly. This method has included cookies in the request.
     *
     * @param url image url
     * @return image data
     */
    public String download(String url, String fileName, MessageType type) {
        return wechatHttpServiceInternal.download(url, fileName, type);
    }

    /**
     * notify the server that all the messages in the conversation between {@code userName} and me have been read.
     *
     * @param userName the contact with whom I need to set the messages read.
     * @throws IOException if statusNotify fails.
     */
    private void notifyNecessary(String toUserName) throws IOException {
        if (toUserName == null) {
            throw new IllegalArgumentException("userName");
        }
        Set<String> unreadContacts = cacheService.getContactNamesWithUnreadMessage();
        if (unreadContacts.contains(toUserName)) {
            wechatHttpServiceInternal.statusNotify(toUserName, StatusNotifyCode.READED.getCode());
            unreadContacts.remove(toUserName);
        }
    }

    public Map<String, String> getCookies() {
		return this.wechatHttpServiceInternal.getCookies();
	}
}
