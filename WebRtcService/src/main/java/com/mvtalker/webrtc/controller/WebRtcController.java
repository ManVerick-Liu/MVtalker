package com.mvtalker.webrtc.controller;

import com.mvtalker.webrtc.server.WebRtcSignalingServer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/webrtc")
@RequiredArgsConstructor
public class WebRtcController
{
    private final WebRtcSignalingServer webRtcSignalingServer;

}
