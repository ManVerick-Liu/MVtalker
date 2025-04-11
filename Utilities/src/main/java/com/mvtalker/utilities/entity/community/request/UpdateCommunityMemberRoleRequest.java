package com.mvtalker.utilities.entity.community.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mvtalker.utilities.common.LongToStringSerializer;
import com.mvtalker.utilities.common.StringToLongDeserializer;
import com.mvtalker.utilities.entity.community.enums.MemberRole;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UpdateCommunityMemberRoleRequest
{
    @NotNull(message = "字段 communityId 不能为空")
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long communityId;
    @NotNull(message = "字段 userId 不能为空")
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long userId;
    private MemberRole role;
}
