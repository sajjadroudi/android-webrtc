package com.codewithkael.webrtcprojectforrecord.utils

import com.codewithkael.webrtcprojectforrecord.models.ReceivedWebRtcEvent

interface OnNewMessageListener {
    fun onNewMessage(event: ReceivedWebRtcEvent)
}