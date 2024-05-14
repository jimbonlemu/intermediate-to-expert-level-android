package com.jimbonlemu.myserviceapp

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

import com.jimbonlemu.mycustomview.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var boundStatus = false
    private lateinit var boundService: MyBoundService

    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            boundStatus = false
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val myBinder = service as MyBoundService.MyBinder
            boundService = myBinder.getService
            boundStatus = true
            getNumberFromService()
        }
    }
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean? ->
        if (!isGranted!!) {
            Toast.makeText(
                this,
                "Unable to display Foreground service notification due to permission decline",
                Toast.LENGTH_LONG
            )
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater).apply {
            setContentView(root)
            val serviceIntent = Intent(this@MainActivity, MyBackgroundService::class.java)

            btnStartBackgroundService.setOnClickListener {
                startService(serviceIntent)
            }

            btnStopBackgroundService.setOnClickListener {
                stopService(serviceIntent)
            }


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                )
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            val foregroundServiceIntent = Intent(this@MainActivity, MyForegroundService::class.java)
            btnStartForegroundService.setOnClickListener {
                if (Build.VERSION.SDK_INT >= 26) {
                    startForegroundService(foregroundServiceIntent)
                } else {
                    startService(foregroundServiceIntent)
                }
            }

            btnStopForegroundService.setOnClickListener {
                stopService(foregroundServiceIntent)
            }

            val boundServiceIntent = Intent(this@MainActivity, MyBoundService::class.java)
            btnStartBoundService.setOnClickListener {
                bindService(boundServiceIntent, connection, BIND_AUTO_CREATE)
            }

            btnStopBoundService.setOnClickListener {
                unbindService(connection)
            }
        }


    }

    override fun onStop() {
        super.onStop()
        if (boundStatus) {
            unbindService(connection)
            boundStatus = false
        }
    }

    private fun getNumberFromService() {
        boundService.numberLiveData.observe(this) { number ->
            binding.tvBoundServiceNumber.text = number.toString()
        }
    }
}