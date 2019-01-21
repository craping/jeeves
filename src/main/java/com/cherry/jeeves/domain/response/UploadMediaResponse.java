package com.cherry.jeeves.domain.response;

import com.cherry.jeeves.domain.response.component.WechatHttpResponseBase;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadMediaResponse extends WechatHttpResponseBase {
    @JsonProperty
    private int CDNThumbImgHeight;
    @JsonProperty
    private int CDNThumbImgWidth;
    @JsonProperty
    private String EncryFileName;
    @JsonProperty
    private String MediaId;
    @JsonProperty
    private int StartPos;
    @JsonProperty
    private String fileext;
    @JsonProperty
    private String title;
    
	public int getCDNThumbImgHeight() {
		return CDNThumbImgHeight;
	}
	public void setCDNThumbImgHeight(int cDNThumbImgHeight) {
		CDNThumbImgHeight = cDNThumbImgHeight;
	}
	public int getCDNThumbImgWidth() {
		return CDNThumbImgWidth;
	}
	public void setCDNThumbImgWidth(int cDNThumbImgWidth) {
		CDNThumbImgWidth = cDNThumbImgWidth;
	}
	public String getEncryFileName() {
		return EncryFileName;
	}
	public void setEncryFileName(String encryFileName) {
		EncryFileName = encryFileName;
	}
	public String getMediaId() {
		return MediaId;
	}
	public void setMediaId(String mediaId) {
		MediaId = mediaId;
	}
	public int getStartPos() {
		return StartPos;
	}
	public void setStartPos(int startPos) {
		StartPos = startPos;
	}
	public String getFileext() {
		return fileext;
	}
	public void setFileext(String fileext) {
		this.fileext = fileext;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
}