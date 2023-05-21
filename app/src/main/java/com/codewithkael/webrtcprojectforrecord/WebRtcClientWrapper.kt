package com.codewithkael.webrtcprojectforrecord

import android.app.Application
import com.codewithkael.webrtcprojectforrecord.models.AnswerModel
import com.codewithkael.webrtcprojectforrecord.models.OfferModel
import com.codewithkael.webrtcprojectforrecord.models.SentWebRtcEvent
import org.webrtc.IceCandidate
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer

class WebRtcClientWrapper(
    private val application: Application,
    private val username: String,
    private val localView: SurfaceViewRenderer,
    private val remoteView: SurfaceViewRenderer,
    private val observer: PeerConnection.Observer
) {

    private var webRtcClient : WebRtcClient? = null

    val isInCall : Boolean
        get() = (webRtcClient != null)

    fun initialize() {
        webRtcClient = WebRtcClient(application, username, localView, remoteView, observer)
    }

    fun setupLocalVideoView() {
        webRtcClient?.setupLocalVideoView()
    }

    fun setupRemoteVideoView() {
        webRtcClient?.setupRemoteVideoView()
    }

    fun startLocalVideo() {
        webRtcClient?.startLocalVideo()
    }

    fun call(target: String, sendingOfferBlock: (SentWebRtcEvent<OfferModel>) -> Unit) {
        webRtcClient?.call(target, sendingOfferBlock)
    }

    fun onRemoteSessionReceived(session: SessionDescription) {
        webRtcClient?.onRemoteSessionReceived(session)
    }

    fun answer(target: String, sendingAnswerBlock: (SentWebRtcEvent<AnswerModel>) -> Unit) {
        webRtcClient?.answer(target, sendingAnswerBlock)
    }

    fun addIceCandidate(p0: IceCandidate?) {
        webRtcClient?.addIceCandidate(p0)
    }

    fun switchCamera() {
        webRtcClient?.switchCamera()
    }

    fun toggleAudio(mute: Boolean) {
        webRtcClient?.toggleAudio(mute)
    }

    fun toggleCamera(cameraPause: Boolean) {
        webRtcClient?.toggleAudio(cameraPause)
    }

    fun endCall() {
        webRtcClient?.endCall()
        webRtcClient = null
    }

}