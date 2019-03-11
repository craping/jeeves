package com.cherry.jeeves.service;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cherry.jeeves.domain.shared.Contact;
import com.cherry.jeeves.domain.shared.Member;
import com.cherry.jeeves.domain.shared.Message;
import com.cherry.jeeves.domain.shared.RecommendInfo;

public class DefaultMessageHandler implements MessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(DefaultMessageHandler.class);

    @Override
    public void onReceivingChatRoomTextMessage(Message message) {
        logger.info("onReceivingChatRoomTextMessage");
    }

    @Override
    public void onReceivingChatRoomImageMessage(Message message, String thumbImageUrl, String fullImageUrl) {
        logger.info("onReceivingChatRoomImageMessage");
    }

    @Override
    public void onReceivingPrivateTextMessage(Message message) {
        logger.info("onReceivingPrivateTextMessage");
    }

    @Override
    public void onReceivingPrivateImageMessage(Message message, String thumbImageUrl, String fullImageUrl) {
        logger.info("onReceivingPrivateImageMessage");
    }

    @Override
    public boolean onReceivingFriendInvitation(RecommendInfo info) {
        logger.info("onReceivingFriendInvitation");
        return false;
    }

    @Override
    public void postAcceptFriendInvitation(Message message) throws IOException {
        logger.info("postAcceptFriendInvitation");
    }

    @Override
    public void onChatRoomMembersChanged(Contact chatRoom, Set<Contact> membersJoined, Set<Contact> membersLeft) {
        logger.info("onChatRoomMembersChanged");
    }

    @Override
    public void onNewChatRoomsFound(Set<Contact> chatRooms) {
        logger.info("onNewChatRoomsFound");
    }

    @Override
    public void onChatRoomsDeleted(Set<Contact> chatRooms) {
        logger.info("onChatRoomsDeleted");
    }

    @Override
    public void onNewFriendsFound(Set<Contact> contacts) {
        logger.info("onNewFriendsFound");
    }

    @Override
    public void onFriendsDeleted(Set<Contact> contacts) {
        logger.info("onFriendsDeleted");
    }

    @Override
    public void onNewMediaPlatformsFound(Set<Contact> mps) {
        logger.info("onNewMediaPlatformsFound");
    }

    @Override
    public void onMediaPlatformsDeleted(Set<Contact> mps) {
        logger.info("onMediaPlatformsDeleted");
    }

    @Override
    public void onRedPacketReceived(Contact contact) {
        logger.info("onRedPacketReceived");
    }

	@Override
	public void onReceivingChatRoomEmoticonMessage(Message message, String emoticonUrl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReceivingChatRoomVoiceMessage(Message message, String voiceUrl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReceivingChatRoomVideoMessage(Message message, String thumbImageUrl, String videoUrl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReceivingChatRoomMediaMessage(Message message, String mediaUrl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReceivingPrivateEmoticonMessage(Message message, String emoticonUrl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReceivingPrivateVoiceMessage(Message message, String voiceUrl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReceivingPrivateVideoMessage(Message message, String thumbImageUrl, String videoUrl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReceivingPrivateMediaMessage(Message message, String mediaUrl) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLogin(Member member) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLogout(Member member) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onScanning(String headImgBase64) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onExpired() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConfirmation() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onQR(byte[] qrData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusNotifyReaded(Message message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusNotifyEnterSession(Message message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusNotifyInited(Message message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusNotifyQuitSession(Message message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusNotifySyncConv(Message message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMembersSeqChanged(Map<String, String> seqMap) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFriendVerify(Contact contact) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFriendBlacklist(Contact contact) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onContactCompleted() {
		// TODO Auto-generated method stub
		
	}
}
