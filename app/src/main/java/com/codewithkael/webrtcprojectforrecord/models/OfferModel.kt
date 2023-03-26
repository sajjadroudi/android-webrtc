package com.codewithkael.webrtcprojectforrecord.models

import org.webrtc.SessionDescription

data class OfferModel(
    val type: SessionDescription.Type?,
    val sdp: String?
)
