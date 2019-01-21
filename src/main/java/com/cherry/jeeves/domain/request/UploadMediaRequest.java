package com.cherry.jeeves.domain.request;

import com.cherry.jeeves.domain.request.component.BaseRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class UploadMediaRequest {
	@JsonProperty
	private BaseRequest BaseRequest;
	@JsonProperty
	private String FromUserName;
	@JsonProperty
	private String ToUserName;
	@JsonProperty
	private String FileMd5;
	@JsonProperty
	private long ClientMediaId;
	@JsonProperty
	private long DataLen;
	@JsonProperty
	private long TotalLen;
	@JsonProperty
    private int StartPos;
	@JsonProperty
    private int MediaType;
	@JsonProperty
	private int UploadType;
	
	public BaseRequest getBaseRequest() {
		return BaseRequest;
	}

	public void setBaseRequest(BaseRequest baseRequest) {
		BaseRequest = baseRequest;
	}

	public long getClientMediaId() {
		return ClientMediaId;
	}

	public void setClientMediaId(long clientMediaId) {
		ClientMediaId = clientMediaId;
	}

	public long getDataLen() {
		return DataLen;
	}

	public void setDataLen(long dataLen) {
		DataLen = dataLen;
	}

	public long getTotalLen() {
		return TotalLen;
	}

	public void setTotalLen(long totalLen) {
		TotalLen = totalLen;
	}

	public int getStartPos() {
		return StartPos;
	}

	public void setStartPos(int startPos) {
		StartPos = startPos;
	}

	public int getMediaType() {
		return MediaType;
	}

	public void setMediaType(int mediaType) {
		MediaType = mediaType;
	}

	public String getFromUserName() {
		return FromUserName;
	}

	public void setFromUserName(String fromUserName) {
		FromUserName = fromUserName;
	}

	public String getToUserName() {
		return ToUserName;
	}

	public void setToUserName(String toUserName) {
		ToUserName = toUserName;
	}

	public String getFileMd5() {
		return FileMd5;
	}

	public void setFileMd5(String fileMd5) {
		FileMd5 = fileMd5;
	}

	public int getUploadType() {
		return UploadType;
	}

	public void setUploadType(int uploadType) {
		UploadType = uploadType;
	}
	
}