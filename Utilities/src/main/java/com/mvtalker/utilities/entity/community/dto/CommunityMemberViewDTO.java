package com.mvtalker.utilities.entity.community.dto;

import com.mvtalker.utilities.entity.community.enums.MemberRole;
import com.mvtalker.utilities.entity.user.enums.AccountStatus;
import com.mvtalker.utilities.entity.user.enums.OnlineStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommunityMemberViewDTO
{
    private Long userId;
    private String nickname;
    private String avatarUrl;
    private OnlineStatus onlineStatus;
    private AccountStatus accountStatus;
    private LocalDateTime lastOnline;
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
}
