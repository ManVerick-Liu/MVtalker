package com.mvtalker.webrtc.entity.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mvtalker.utilities.common.LongToStringSerializer;
import com.mvtalker.utilities.common.StringToLongDeserializer;
import com.mvtalker.webrtc.entity.BaseMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class HeartbeatData
{
    @JsonProperty("sourceUserId")
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @JsonSerialize(using = LongToStringSerializer.class)
    private Long sourceUserId;

    @JsonProperty("message")
    private String message;
}
