package com.cherry.jeeves.service.disruptor;

import com.cherry.jeeves.service.SyncServie;
import com.lmax.disruptor.EventHandler;

public class MsgHandler implements EventHandler<MsgEvent> {
	
	private final long ordinal;
	
    private final long numberOfConsumers;
    
    private SyncServie service;
	
	public MsgHandler(final long ordinal, final long numberOfConsumers, SyncServie service)  {
        this.ordinal = ordinal;
        this.numberOfConsumers = numberOfConsumers;
        this.service = service;
    }
	
	@Override
	public void onEvent(MsgEvent event, long sequence, boolean endOfBatch) throws Exception {
		if (Math.abs((event.getHash() % (numberOfConsumers+1))) == ordinal){
			try {
				// mod包含新增和修改
				if (event.getModContactList().size() > 0) {
					service.onContactsModified(event.getModContactList());
				}
				// del->联系人移除
				if (event.getDelContactList().size() > 0) {
					service.onContactsDeleted(event.getDelContactList());
				}
				service.onNewMessage(event.getAddMsgList());
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
			}
		}
	}
	
	public static void main(String[] args) {
		String userName = "asdfasdf";
		System.out.println(userName.hashCode());
//		for (int i = -10; i <= 10; i++) {
//			System.out.println(Math.abs(i%(3+1)));
//		}
		int hash = String.valueOf(56800004874L).hashCode() & Integer.MAX_VALUE;
		System.out.println(hash);
	}
}
