package com.cherry.jeeves.service.disruptor;

import java.util.LinkedList;
import java.util.List;

import com.cherry.jeeves.domain.shared.Contact;
import com.cherry.jeeves.domain.shared.Message;

public class MsgEvent {
	private int hash;
	private List<Message> AddMsgList = new LinkedList<>();
	private List<Contact> ModContactList = new LinkedList<>();
	private List<Contact> DelContactList = new LinkedList<>();
    
	public MsgEvent() {
	}
	
	public MsgEvent(int hash) {
		this.hash = hash;
	}
	public List<Message> getAddMsgList() {
		return AddMsgList;
	}
	public void setAddMsgList(List<Message> addMsgList) {
		AddMsgList = addMsgList;
	}
	public List<Contact> getModContactList() {
		return ModContactList;
	}
	public void setModContactList(List<Contact> modContactList) {
		ModContactList = modContactList;
	}
	public List<Contact> getDelContactList() {
		return DelContactList;
	}
	public void setDelContactList(List<Contact> delContactList) {
		DelContactList = delContactList;
	}
	
	public int getHash() {
		return hash;
	}
	public void setHash(int hash) {
		this.hash = hash;
	}
	public void clear(){
		this.AddMsgList = null;
		this.ModContactList = null;
		this.DelContactList = null;
	}
}
