package com.mvtalker.webrtc.entity.data;

import lombok.Data;

import java.util.List;

@Data
public class OfferData
{
    private String sdp;
    private List<IceCandidate> iceCandidates;
}
