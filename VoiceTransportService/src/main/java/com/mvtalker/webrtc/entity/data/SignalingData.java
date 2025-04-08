package com.mvtalker.webrtc.entity.data;

import lombok.Data;

import java.util.List;

@Data
public class SignalingData
{
    private Long sourceUserId;
    private Long targetUserId;
    private String sdp;
    private List<String> iceCandidates;
}
