package com.mvtalker.webrtc.entity.data;

import com.mvtalker.webrtc.entity.BaseMessage;
import com.mvtalker.webrtc.entity.enums.MessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class HeartbeatMessage extends BaseMessage
{
    private HeartbeatData data;
    public HeartbeatMessage()
    {
        this.setType(MessageType.heartbeat);
    }
}
