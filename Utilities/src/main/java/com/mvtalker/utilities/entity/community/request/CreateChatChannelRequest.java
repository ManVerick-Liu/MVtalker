package com.mvtalker.utilities.entity.community.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mvtalker.utilities.common.LongToStringSerializer;
import com.mvtalker.utilities.common.StringToLongDeserializer;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CreateChatChannelRequest
{
    @NotNull(message = "字段 communityId 不能为空")
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long communityId;
    private String name;
    private Integer capacity;
}
