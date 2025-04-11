package com.mvtalker.webrtc.entity.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mvtalker.utilities.common.LongToStringSerializer;
import com.mvtalker.utilities.common.StringToLongDeserializer;
import lombok.Data;

import java.util.List;
import java.util.Objects;

@Data
public class SignalingData
{
    @JsonProperty("sourceUserId")
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @JsonSerialize(using = LongToStringSerializer.class)
    private Long sourceUserId;

    @JsonProperty("targetUserId")
    @JsonDeserialize(using = StringToLongDeserializer.class)
    @JsonSerialize(using = LongToStringSerializer.class)
    private Long targetUserId;

    @JsonProperty("sdp")
    private String sdp;

    @JsonProperty("iceCandidates")
    private List<Object> iceCandidates;
}
