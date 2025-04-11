package com.mvtalker.webrtc.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mvtalker.webrtc.entity.data.HeartbeatData;
import com.mvtalker.webrtc.entity.data.HeartbeatMessage;
import com.mvtalker.webrtc.entity.data.SignalingData;
import com.mvtalker.webrtc.entity.data.SignalingMessage;
import com.mvtalker.webrtc.entity.enums.MessageType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = HeartbeatMessage.class, name = "heartbeat"),
        @JsonSubTypes.Type(value = SignalingMessage.class, name = "offer"),
        @JsonSubTypes.Type(value = SignalingMessage.class, name = "answer"),
        @JsonSubTypes.Type(value = SignalingMessage.class, name = "iceCandidate")
})
public class BaseMessage
{
    private MessageType type;
    private LocalDateTime timestamp;
}
