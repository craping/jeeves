package com.cherry.jeeves.service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.stereotype.Component;

import com.cherry.jeeves.domain.request.component.BaseRequest;
import com.cherry.jeeves.domain.shared.Contact;
import com.cherry.jeeves.domain.shared.Owner;
import com.cherry.jeeves.domain.shared.SyncCheckKey;
import com.cherry.jeeves.domain.shared.SyncKey;
import com.cherry.jeeves.utils.DeviceIdGenerator;

@Component
public class CacheService {

    public void reset() {
        this.hostUrl = null;
        this.syncUrl = null;
        this.fileUrl = null;
        this.passTicket = null;
        this.baseRequest = null;
        this.owner = null;
        this.syncCheckKey = null;
        this.syncKey = null;
        this.sKey = null;
        this.uuid = null;
        this.uin = null;
        this.sid = null;
        this.individuals.clear();
        this.mediaPlatforms.clear();
        this.chatRooms.clear();
    }

    private boolean alive = false;
    private String uuid;
    private String hostUrl;
    private String syncUrl;
    private String fileUrl;
    private String passTicket;
    private BaseRequest baseRequest;
    private Owner owner;
    private SyncKey syncKey;
    private SyncCheckKey syncCheckKey;
    private String sKey;
    private String uin;
    private String sid;

    private ConcurrentLinkedQueue<Contact> individuals = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<Contact> mediaPlatforms = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<Contact> chatRooms = new ConcurrentLinkedQueue<>();

    private Set<String> contactNamesWithUnreadMessage = new HashSet<>();

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public String getHostUrl() {
        return hostUrl;
    }

    public void setHostUrl(String hostUrl) {
        this.hostUrl = hostUrl;
    }

    public String getPassTicket() {
        return passTicket;
    }

    public void setPassTicket(String passTicket) {
        this.passTicket = passTicket;
    }

    public BaseRequest getBaseRequest() {
        baseRequest.setDeviceID(DeviceIdGenerator.generate());
        return baseRequest;
    }

    public void setBaseRequest(BaseRequest baseRequest) {
        this.baseRequest = baseRequest;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public SyncKey getSyncKey() {
        return syncKey;
    }

    public void setSyncKey(SyncKey syncKey) {
        this.syncKey = syncKey;
    }

    public SyncCheckKey getSyncCheckKey() {
        return syncCheckKey;
    }

    public void setSyncCheckKey(SyncCheckKey syncCheckKey) {
        this.syncCheckKey = syncCheckKey;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getsKey() {
        return sKey;
    }

    public void setsKey(String sKey) {
        this.sKey = sKey;
    }

    public String getUin() {
        return uin;
    }

    public void setUin(String uin) {
        this.uin = uin;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public ConcurrentLinkedQueue<Contact> getIndividuals() {
        return individuals;
    }

    public ConcurrentLinkedQueue<Contact> getMediaPlatforms() {
        return mediaPlatforms;
    }

    public ConcurrentLinkedQueue<Contact> getChatRooms() {
        return chatRooms;
    }

    public String getSyncUrl() {
        return syncUrl;
    }

    public void setSyncUrl(String syncUrl) {
        this.syncUrl = syncUrl;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public Set<String> getContactNamesWithUnreadMessage() {
        return contactNamesWithUnreadMessage;
    }

	public void setIndividuals(ConcurrentLinkedQueue<Contact> individuals) {
		this.individuals = individuals;
	}

	public void setMediaPlatforms(ConcurrentLinkedQueue<Contact> mediaPlatforms) {
		this.mediaPlatforms = mediaPlatforms;
	}

	public void setChatRooms(ConcurrentLinkedQueue<Contact> chatRooms) {
		this.chatRooms = chatRooms;
	}
    
	public Contact searchContact(ConcurrentLinkedQueue<Contact> contacts, String userNameOrSeq) {
		if(contacts == null)
			return null;
		return contacts.stream().parallel().filter(x -> userNameOrSeq.equals(x.getUserName()) || userNameOrSeq.equals(x.getSeq())).findFirst().orElse(null);
	}
	
	public Contact getContact(String userNameOrSeq) {
		return searchContact(individuals, userNameOrSeq);
	}
	
	public Contact getChatRoom(String userNameOrSeq) {
		return searchContact(chatRooms, userNameOrSeq);
	}
	
	public Contact getChatRoomMember(String chatRoomuserNameOrSeq, String memberuserNameOrSeq) {
		Contact chatRoom = searchContact(chatRooms, chatRoomuserNameOrSeq);
		if(chatRoom != null && chatRoom.getMemberList() != null)
			return searchContact(chatRoom.getMemberList(), memberuserNameOrSeq);
		return null;
	}
	
}