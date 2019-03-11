package com.cherry.jeeves.service;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.cherry.jeeves.domain.shared.Contact;
import com.cherry.jeeves.domain.shared.Member;
import com.cherry.jeeves.domain.shared.Message;
import com.cherry.jeeves.domain.shared.RecommendInfo;

public interface MessageHandler {
	
	/**
     * 事件：获取登录二维码消息
     *
     * @param qrData 登录二维码数据
     */
	void onQR(byte[] qrData);
	/**
     * 事件：用户扫码消息
     *
     * @param headImgBase64 扫码用户头像图片 Base64
     */
    void onScanning(String headImgBase64);
    
    /**
     * 事件：用户确认登录消息
     *
     */
    void onConfirmation();
    /**
     * 事件：二维码过期消息
     *
     */
    void onExpired();
    
	/**
     * 事件：登录消息
     *
     * @param message 消息体
     */
    void onLogin(Member member);
    
    /**
     * 事件：离线消息
     *
     * @param message 消息体
     */
    void onLogout(Member member);
    
    /**
     * 事件：联系人加载完毕
     *
     * @param message 消息体
     */
    void onContactCompleted();
    
    /**
     * 事件：收到群聊天文本消息
     *
     * @param message 消息体
     */
    void onReceivingChatRoomTextMessage(Message message);

    /**
     * 事件：收到群聊天图片消息
     *
     * @param message		消息体
     * @param thumbImageUrl	图片缩略图链接
     * @param fullImageUrl	图片完整图链接
     */
    void onReceivingChatRoomImageMessage(Message message, String thumbImageUrl, String fullImageUrl);
    
    /**
     * 事件：收到群聊天表情消息
     *
     * @param message		消息体
     * @param emoticonUrl	表情图片链接
     */
    void onReceivingChatRoomEmoticonMessage(Message message, String emoticonUrl);
    
    /**
     * 事件：收到群聊天语音消息
     *
     * @param message		消息体
     * @param thumbImageUrl	语音链接
     */
    void onReceivingChatRoomVoiceMessage(Message message, String voiceUrl);
    
    /**
     * 事件：收到群聊天视频消息
     *
     * @param message		消息体
     * @param thumbImageUrl	视频缩略图链接
     * @param videoUrl		视频链接
     */
    void onReceivingChatRoomVideoMessage(Message message, String thumbImageUrl, String videoUrl);
    
    
    /**
     * 事件：收到群聊天文件消息
     *
     * @param message		消息体
     * @param mediaUrl 		文件链接
     */
    void onReceivingChatRoomMediaMessage(Message message, String mediaUrl);
    
    
    
    
    
    
    
    
    
    /**
     * 事件：收到个人聊天文本消息
     *
     * @param message	消息体
     */
    void onReceivingPrivateTextMessage(Message message);

    /**
     * 事件：收到个人聊天图片消息
     *
     * @param message       消息体
     * @param thumbImageUrl 图片缩略图链接
     * @param fullImageUrl  图片完整图链接
     */
    void onReceivingPrivateImageMessage(Message message, String thumbImageUrl, String fullImageUrl);
    
    /**
     * 事件：收到个人聊天表情消息
     *
     * @param message       消息体
     * @param thumbImageUrl 图片缩略图链接
     * @param fullImageUrl  图片完整图链接
     */
    void onReceivingPrivateEmoticonMessage(Message message, String emoticonUrl);
    
    /**
     * 事件：收到个人聊天语音消息
     *
     * @param message		消息体
     * @param thumbImageUrl	语音链接
     */
    void onReceivingPrivateVoiceMessage(Message message, String voiceUrl);
    
    /**
     * 事件：收到个人聊天视频消息
     *
     * @param message		消息体
     * @param thumbImageUrl	视频缩略图链接
     * @param videoUrl		视频链接
     */
    void onReceivingPrivateVideoMessage(Message message, String thumbImageUrl, String videoUrl);
    
    
    /**
     * 事件：收到个人聊天文件消息
     *
     * @param message		消息体
     * @param mediaUrl 		文件链接
     */
    void onReceivingPrivateMediaMessage(Message message, String mediaUrl);
    
    
    
    
    
    
    
    
    
    /**
     * 事件：收到状态同步消息：消息已读
     *
     * @param message		消息体
     */
    void onStatusNotifyReaded(Message message);
    
    /**
     * 事件：收到状态同步消息：发起会话
     *
     * @param message		消息体
     */
    void onStatusNotifyEnterSession(Message message);
    
    /**
     * 事件：收到状态同步消息：正在输入
     *
     * @param message		消息体
     */
    void onStatusNotifyInited(Message message);
    
    /**
     * 事件：收到状态同步消息：通讯录同步
     *
     * @param message		消息体
     */
    void onStatusNotifySyncConv(Message message);
    
    /**
     * 事件：收到状态同步消息：关闭会话
     *
     * @param message		消息体
     */
    void onStatusNotifyQuitSession(Message message);
    /**
     * 事件：所有联系人seq发生变化
     *
     * @param chatRoom      群
     * @param membersJoined 新加入的群成员
     * @param membersLeft   离开的群成员
     */
    void onMembersSeqChanged(Map<String, String> seqMap);
    
    
    
    
    
    /**
     * 事件：收到加好友邀请
     *
     * @param info 邀请信息
     * @return {@code true} 如果接受请求, 否则 {@code false}
     */
    boolean onReceivingFriendInvitation(RecommendInfo info);

    /**
     * 事件：接受好友邀请成功
     *
     * @param message 消息体
     */
    void postAcceptFriendInvitation(Message message) throws IOException;

    /**
     * 事件：群成员发生变化
     *
     * @param chatRoom      群
     * @param membersJoined 新加入的群成员
     * @param membersLeft   离开的群成员
     */
    void onChatRoomMembersChanged(Contact chatRoom, Set<Contact> membersJoined, Set<Contact> membersLeft);

    /**
     * 事件：发现新增群（例如加入了新群）
     *
     * @param chatRooms 新增的群
     */
    void onNewChatRoomsFound(Set<Contact> chatRooms);

    /**
     * 事件：发现群减少（例如被踢出了群）
     *
     * @param chatRooms 减少的群
     */
    void onChatRoomsDeleted(Set<Contact> chatRooms);

    /**
     * 事件：发现新的好友
     *
     * @param contacts 新的好友
     */
    void onNewFriendsFound(Set<Contact> contacts);

    /**
     * 事件：发现好友减少
     *
     * @param contacts 减少的好友
     */
    void onFriendsDeleted(Set<Contact> contacts);

    /**
     * 事件：发现新的公众号
     *
     * @param mps 新的公众号
     */
    void onNewMediaPlatformsFound(Set<Contact> mps);

    /**
     * 事件：删除公众号
     *
     * @param mps 被删除的公众号
     */
    void onMediaPlatformsDeleted(Set<Contact> mps);

    /**
     * 事件：收到红包（个人的或者群里的）
     *
     * @param contact 发红包的个人或者群
     */
    void onRedPacketReceived(Contact contact);
    
    /**
     * 事件：被对方删除
     *
     * @param contact 僵尸用户
     */
    void onFriendVerify(Contact contact);
    
    /**
     * 事件：被对方拉黑
     *
     * @param contact 僵尸用户
     */
    void onFriendBlacklist(Contact contact);
}
