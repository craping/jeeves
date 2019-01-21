package com.cherry.jeeves.exception;

public class WechatException extends RuntimeException {
      
	/**  
	* @Fields field:field:{todo}(用一句话描述这个变量表示什么)  
	*/  
	    
	private static final long serialVersionUID = 1252245545947347177L;

	public WechatException() {
    }

    public WechatException(String message) {
        super(message);
    }

    public WechatException(String message, Throwable cause) {
        super(message, cause);
    }

    public WechatException(Throwable cause) {
        super(cause);
    }

    public WechatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
