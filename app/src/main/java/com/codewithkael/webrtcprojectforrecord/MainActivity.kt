package com.codewithkael.webrtcprojectforrecord

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.codewithkael.webrtcprojectforrecord.databinding.ActivityMainBinding
import com.permissionx.guolindev.PermissionX

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.enterBtn.setOnClickListener {
            PermissionX.init(this)
                .permissions(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA
                ).request{ allGranted, _ ,_ ->
                    if (allGranted){
                        startActivity(
                            Intent(this, CallActivity::class.java)
                                .putExtras(extractBundle())
                        )
                    } else {
                        Toast.makeText(this,"you should accept all permissions",Toast.LENGTH_LONG).show()
                    }
                }

        }

    }

    private fun extractBundle(): Bundle {
        val userName = binding.username.text.toString()
        val serverIp = binding.edtServerIp.text.toString()
        val serverPort = binding.edtServerPort.text.toString()

        return Bundle().apply {
            putString("username", userName)
            putString("server_ip", serverIp)
            putString("server_port", serverPort)
        }
    }

}