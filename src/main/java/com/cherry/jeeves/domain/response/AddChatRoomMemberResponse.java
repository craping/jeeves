package com.cherry.jeeves.domain.response;

import java.util.Set;

import com.cherry.jeeves.domain.response.component.WechatHttpResponseBase;
import com.cherry.jeeves.domain.shared.Contact;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AddChatRoomMemberResponse extends WechatHttpResponseBase {
    @JsonProperty
    private int MemberCount;
    @JsonProperty
    private Set<Contact> MemberList;

    public int getMemberCount() {
        return MemberCount;
    }

    public void setMemberCount(int memberCount) {
        MemberCount = memberCount;
    }

    public Set<Contact> getMemberList() {
        return MemberList;
    }

    public void setMemberList(Set<Contact> memberList) {
        MemberList = memberList;
    }
}
