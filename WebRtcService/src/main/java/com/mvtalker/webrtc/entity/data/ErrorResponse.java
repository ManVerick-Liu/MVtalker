package com.mvtalker.webrtc.entity.data;

import com.mvtalker.webrtc.entity.enums.MessageType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErrorResponse
{
    private MessageType type;
    private LocalDateTime timestamp;
    private String message;
}
