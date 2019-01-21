package com.cherry.jeeves.exception;

public class WechatQRExpiredException extends RuntimeException {
      
	/**  
	* @Fields field:field:{todo}(用一句话描述这个变量表示什么)  
	*/  
	    
	private static final long serialVersionUID = -6118440850308261728L;

	public WechatQRExpiredException() {
    }

    public WechatQRExpiredException(String message) {
        super(message);
    }

    public WechatQRExpiredException(String message, Throwable cause) {
        super(message, cause);
    }

    public WechatQRExpiredException(Throwable cause) {
        super(cause);
    }

    public WechatQRExpiredException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
