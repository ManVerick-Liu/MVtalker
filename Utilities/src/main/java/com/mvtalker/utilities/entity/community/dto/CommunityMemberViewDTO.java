package com.mvtalker.utilities.entity.community.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mvtalker.utilities.common.LongToStringSerializer;
import com.mvtalker.utilities.common.StringToLongDeserializer;
import com.mvtalker.utilities.entity.community.enums.MemberRole;
import com.mvtalker.utilities.entity.user.enums.AccountStatus;
import com.mvtalker.utilities.entity.user.enums.OnlineStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommunityMemberViewDTO
{
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @JsonSerialize(using = LongToStringSerializer.class)
    private Long userId;
    private String nickname;
    private String avatarUrl;
    private OnlineStatus onlineStatus;
    private AccountStatus accountStatus;
    private LocalDateTime lastOnline;
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @JsonSerialize(using = LongToStringSerializer.class)
    private Long communityId;
    private MemberRole role;

    public CommunityMemberViewDTO(Long userId, String nickname, String avatarUrl, OnlineStatus onlineStatus, AccountStatus accountStatus, LocalDateTime lastOnline, Long communityId, MemberRole role)
    {
        this.userId = userId;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
        this.onlineStatus = onlineStatus;
        this.accountStatus = accountStatus;
        this.lastOnline = lastOnline;
        this.communityId = communityId;
        this.role = role;
    }
    public CommunityMemberViewDTO()
    {
        // 在Feign远程调用中，如果响应数据没有无参构造函数，则无法正常序列化，会报空指针异常
    }
}
