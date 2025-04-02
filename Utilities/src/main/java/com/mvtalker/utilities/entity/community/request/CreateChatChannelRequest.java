package com.mvtalker.utilities.entity.community.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CreateChatChannelRequest
{
    @NotNull(message = "字段 communityId 不能为空")
    private Long communityId;
    private String name;
    private Integer capacity;
}
