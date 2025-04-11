package com.mvtalker.webrtc.entity.data;

import com.mvtalker.webrtc.entity.BaseMessage;
import com.mvtalker.webrtc.entity.enums.MessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class SignalingMessage extends BaseMessage
{
    private SignalingData data;
    public SignalingMessage(MessageType type)
    {
        this.setType(type);
    }
}
