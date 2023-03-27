package com.codewithkael.webrtcprojectforrecord.models

data class ReceivedWebRtcEvent(
    val type: ReceivedWebRtcEventType,
    val originUserName: String,
    val targetUserName: String? = null,
    val data: Any? = null,
    val isSuccessful: Boolean = true
)

enum class ReceivedWebRtcEventType(
    private val key: String
) {
    CALL_RESPONSE("call_response"),
    ANSWER_RECEIVED("answer_received"),
    OFFER_RECEIVED("offer_received"),
    ICE_CANDIDATE("ice_candidate"),
    CALL_REJECTED("call_rejected"),
    CALL_ENDED("call_ended")
}