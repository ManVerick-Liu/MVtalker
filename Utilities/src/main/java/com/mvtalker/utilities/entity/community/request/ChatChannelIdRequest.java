package com.mvtalker.utilities.entity.community.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ChatChannelIdRequest
{
    @NotNull(message = "字段 chatChannelId 不能为空")
    private Long chatChannelId;
    @NotNull(message = "字段 communityId 不能为空")
    private Long communityId;
}
