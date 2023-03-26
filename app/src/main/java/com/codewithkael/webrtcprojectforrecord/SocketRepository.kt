package com.codewithkael.webrtcprojectforrecord

import android.util.Log
import com.codewithkael.webrtcprojectforrecord.Config.SERVER_IP
import com.codewithkael.webrtcprojectforrecord.Config.SERVER_PORT
import com.codewithkael.webrtcprojectforrecord.models.MessageModel
import com.codewithkael.webrtcprojectforrecord.models.SentWebRtcEvent
import com.codewithkael.webrtcprojectforrecord.models.SentWebRtcEventType.REGISTER_USER
import com.codewithkael.webrtcprojectforrecord.models.SentWebRtcEventType.UNREGISTER_USER
import com.codewithkael.webrtcprojectforrecord.utils.NewMessageInterface
import com.google.gson.Gson
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class SocketRepository (private val messageInterface: NewMessageInterface) {
    private var webSocket: WebSocketClient? = null
    private var userName: String? = null
    private val TAG = "SocketRepository"
    private val gson = Gson()

    fun initSocket(username: String, serverIp: String?, serverPort: String?) {
        userName = username
        //if you are using android emulator your local websocket address is going to be "ws://10.0.2.2:3000"
        //if you are using your phone as emulator your local address, use cmd and then write ipconfig
        // and get your ethernet ipv4 , mine is : "ws://192.168.1.3:3000"
        //but if your websocket is deployed you add your websocket address here

        webSocket = object : WebSocketClient(URI("ws://${serverIp ?: SERVER_IP}:${serverPort ?: SERVER_PORT}")) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                val sentWebRtcEvent = SentWebRtcEvent<Nothing>(REGISTER_USER, userName!!)
                sendMessageToSocket(sentWebRtcEvent)
            }

            override fun onMessage(message: String?) {
                try {
                    messageInterface.onNewMessage(gson.fromJson(message,MessageModel::class.java))

                }catch (e:Exception){
                    e.printStackTrace()
                }

            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d(TAG, "onClose: $reason")
                val sentWebRtcEvent = SentWebRtcEvent<Nothing>(UNREGISTER_USER, userName!!)
                sendMessageToSocket(sentWebRtcEvent)
            }

            override fun onError(ex: Exception?) {
                Log.d(TAG, "onError: $ex")
            }

        }
        webSocket?.connect()

    }

    fun <T> sendMessageToSocket(message: SentWebRtcEvent<T>) {
        try {
            Log.d(TAG, "sendMessageToSocket: $message")
            webSocket?.send(gson.toJson(message))
        } catch (e: Exception) {
            Log.d(TAG, "sendMessageToSocket: $e")
        }
    }
}