package com.codewithkael.webrtcprojectforrecord.models

import org.webrtc.SessionDescription

data class AnswerModel(
    val type: SessionDescription.Type?,
    val sdp: String?
)
