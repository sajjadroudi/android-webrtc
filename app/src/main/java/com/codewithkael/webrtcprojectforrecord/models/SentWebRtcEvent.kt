package com.codewithkael.webrtcprojectforrecord.models

data class SentWebRtcEvent<T>(
    val type: SentWebRtcEventType,
    val originUserName: String,
    val targetUserName: String? = null,
    val data: T? = null
)

enum class SentWebRtcEventType(
    val key: String
) {
    REGISTER_USER("register_user"),
    START_CALL("start_call"),
    SEND_OFFER("send_offer"),
    SEND_ANSWER("send_answer"),
    ICE_CANDIDATE("ice_candidate"),
    REJECT("reject"),
    END_CALL("end_call"),
    UNREGISTER_USER("unregister_user");
}