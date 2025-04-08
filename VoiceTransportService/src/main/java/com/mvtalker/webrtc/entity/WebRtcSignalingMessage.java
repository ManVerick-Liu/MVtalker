package com.mvtalker.webrtc.entity;

import com.mvtalker.webrtc.entity.enums.MessageType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WebRtcSignalingMessage<T>
{
    private MessageType type;
    private LocalDateTime timestamp;
    private T data;
}
