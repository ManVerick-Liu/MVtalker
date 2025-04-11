package com.mvtalker.webrtc.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.mvtalker.webrtc.entity.data.SignalingData;
import com.mvtalker.webrtc.entity.enums.MessageType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebRtcSignalingMessage
{
    private MessageType type;
    private LocalDateTime timestamp;
    private SignalingData data;
}
