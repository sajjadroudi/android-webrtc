package com.codewithkael.webrtcprojectforrecord

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.codewithkael.webrtcprojectforrecord.databinding.ActivityCallBinding
import com.codewithkael.webrtcprojectforrecord.models.IceCandidateModel
import com.codewithkael.webrtcprojectforrecord.models.MessageModel
import com.codewithkael.webrtcprojectforrecord.utils.NewMessageInterface
import com.codewithkael.webrtcprojectforrecord.utils.PeerConnectionObserver
import com.codewithkael.webrtcprojectforrecord.utils.RTCAudioManager
import com.google.gson.Gson
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.SessionDescription

class CallActivity : AppCompatActivity(), NewMessageInterface {


    lateinit var binding : ActivityCallBinding
    private var userName:String?=null
    private var serverIp:String?=null
    private var serverPort:String?=null
    private var socketRepository:SocketRepository?=null
    private var rtcClient : RTCClient?=null
    private val TAG = "CallActivity"
    private var target:String = ""
    private val gson = Gson()
    private var isMute = false
    private var isCameraPause = false
    private val rtcAudioManager by lazy { RTCAudioManager.create(this) }
    private var isSpeakerMode = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()


    }

    private fun initWebRtcClient() {
        rtcClient = null

        rtcClient = RTCClient(application,userName!!,socketRepository!!, binding.localView, binding.remoteView, object : PeerConnectionObserver() {
            override fun onIceCandidate(p0: IceCandidate?) {
                super.onIceCandidate(p0)
                rtcClient?.addIceCandidate(p0)
                val candidate = hashMapOf(
                    "sdpMid" to p0?.sdpMid,
                    "sdpMLineIndex" to p0?.sdpMLineIndex,
                    "sdpCandidate" to p0?.sdp
                )

                socketRepository?.sendMessageToSocket(
                    MessageModel("ice_candidate",userName,target,candidate)
                )

            }

            override fun onAddStream(p0: MediaStream?) {
                super.onAddStream(p0)
                p0?.videoTracks?.get(0)?.addSink(binding.remoteView)
                Log.d(TAG, "onAddStream: $p0")

            }
        })
    }

    private fun init(){
        userName = intent.getStringExtra("username")
        serverIp = intent.getStringExtra("server_ip")
        serverPort = intent.getStringExtra("server_port")
        socketRepository = SocketRepository(this)
        userName?.let { socketRepository?.initSocket(it, serverIp, serverPort) }

        rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)

        binding.apply {
            callBtn.setOnClickListener {
                socketRepository?.sendMessageToSocket(MessageModel(
                    "start_call",userName,targetUserNameEt.text.toString(),null
                ))
                target = targetUserNameEt.text.toString()
            }

            switchCameraButton.setOnClickListener {
                rtcClient?.switchCamera()
            }

            micButton.setOnClickListener {
                if (isMute){
                    isMute = false
                    micButton.setImageResource(R.drawable.ic_baseline_mic_off_24)
                }else{
                    isMute = true
                    micButton.setImageResource(R.drawable.ic_baseline_mic_24)
                }
                rtcClient?.toggleAudio(isMute)
            }

            videoButton.setOnClickListener {
                if (isCameraPause){
                    isCameraPause = false
                    videoButton.setImageResource(R.drawable.ic_baseline_videocam_off_24)
                }else{
                    isCameraPause = true
                    videoButton.setImageResource(R.drawable.ic_baseline_videocam_24)
                }
                rtcClient?.toggleCamera(isCameraPause)
            }

            audioOutputButton.setOnClickListener {
                if (isSpeakerMode){
                    isSpeakerMode = false
                    audioOutputButton.setImageResource(R.drawable.ic_baseline_hearing_24)
                    rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.EARPIECE)
                }else{
                    isSpeakerMode = true
                    audioOutputButton.setImageResource(R.drawable.ic_baseline_speaker_up_24)
                    rtcAudioManager.setDefaultAudioDevice(RTCAudioManager.AudioDevice.SPEAKER_PHONE)

                }

            }
            endCallButton.setOnClickListener {
                setCallLayoutGone()
                setWhoToCallLayoutVisible()
                setIncomingCallLayoutGone()
                socketRepository?.sendMessageToSocket(MessageModel("end_call",userName,target,null))
                rtcClient?.endCall()
                rtcClient = null
            }
        }

    }

    override fun onNewMessage(message: MessageModel) {
        Log.d(TAG, "onNewMessage: $message")
        when(message.type){
            "call_response"->{
                if (message.data == "user is not online"){
                    //user is not reachable
                    runOnUiThread {
                        Toast.makeText(this,"user is not reachable",Toast.LENGTH_LONG).show()

                    }
                }else{
                    //we are ready for call, we started a call
                    runOnUiThread {
                        setWhoToCallLayoutGone()
                        setCallLayoutVisible()
                        binding.apply {
                            initWebRtcClient()
                            rtcClient?.setupLocalView()
                            rtcClient?.setupRemoteView()
                            rtcClient?.startLocalVideo()
                            rtcClient?.call(targetUserNameEt.text.toString())
                        }
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(binding.targetUserNameEt.windowToken, 0)
                    }

                }
            }
            "answer_received" ->{

                val session = SessionDescription(
                    SessionDescription.Type.ANSWER,
                    message.data.toString()
                )
                rtcClient?.onRemoteSessionReceived(session)
                runOnUiThread {
                    binding.remoteViewLoading.visibility = View.GONE
                }
            }
            "offer_received" ->{
                runOnUiThread {
                    setIncomingCallLayoutVisible()
                    binding.incomingNameTV.text = "${message.name.toString()} is calling you"
                    binding.acceptButton.setOnClickListener {
                        setIncomingCallLayoutGone()
                        setCallLayoutVisible()
                        setWhoToCallLayoutGone()

                        binding.apply {
                            initWebRtcClient()
                            rtcClient?.setupLocalView()
                            rtcClient?.setupRemoteView()
                            rtcClient?.startLocalVideo()
                        }
                        val session = SessionDescription(
                            SessionDescription.Type.OFFER,
                            message.data.toString()
                        )
                        rtcClient?.onRemoteSessionReceived(session)
                        rtcClient?.answer(message.name!!)
                        target = message.name!!
                        binding.remoteViewLoading.visibility = View.GONE

                    }
                    binding.rejectButton.setOnClickListener {
                        rtcClient?.endCall()
                        rtcClient = null
                        val rejectedUser = message.name.toString()
                        Log.i("rouditest", "onNewMessage: rejectedUser: $rejectedUser")
                        socketRepository?.sendMessageToSocket(MessageModel(
                            "reject", userName, rejectedUser,null
                        ))
                        setIncomingCallLayoutGone()
                    }

                }

            }


            "ice_candidate"->{
                try {
                    val receivingCandidate = gson.fromJson(gson.toJson(message.data),
                        IceCandidateModel::class.java)
                    rtcClient?.addIceCandidate(IceCandidate(receivingCandidate.sdpMid,
                        Math.toIntExact(receivingCandidate.sdpMLineIndex.toLong()),receivingCandidate.sdpCandidate))
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }

            "call_rejected" -> {
                runOnUiThread {
                    binding.remoteViewLoading.visibility = View.GONE
                    setCallLayoutGone()
                    setWhoToCallLayoutVisible()
                }

                rtcClient?.endCall()
                rtcClient = null
            }

            "call_ended" -> {
                runOnUiThread {
                    binding.remoteViewLoading.visibility = View.GONE
                    setCallLayoutGone()
                    setIncomingCallLayoutGone()
                    setWhoToCallLayoutVisible()
                }

                rtcClient?.endCall()
                rtcClient = null
            }
        }
    }

    private fun setIncomingCallLayoutGone(){
        binding.incomingCallLayout.visibility = View.GONE
    }
    private fun setIncomingCallLayoutVisible() {
        binding.incomingCallLayout.visibility = View.VISIBLE
    }

    private fun setCallLayoutGone() {
        binding.callLayout.visibility = View.GONE
    }

    private fun setCallLayoutVisible() {
        binding.callLayout.visibility = View.VISIBLE
    }

    private fun setWhoToCallLayoutGone() {
        binding.whoToCallLayout.visibility = View.GONE
    }

    private fun setWhoToCallLayoutVisible() {
        binding.whoToCallLayout.visibility = View.VISIBLE
    }
}