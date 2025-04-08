package com.mvtalker.webrtc.entity.data;

import lombok.Data;

@Data
public class IceCandidate
{
    private String candidate;
    private String sdpMid;
    private int sdpMLineIndex;
}
