package com.codewithkael.webrtcprojectforrecord

object Config {

    val SERVER_IP = if(isRunningOnEmulator()) "10.0.2.2" else "192.168.225.4"
    const val SERVER_PORT = "3000"

}