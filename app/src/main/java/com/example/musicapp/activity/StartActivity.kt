package com.example.musicapp.activity

import android.annotation.SuppressLint
import android.content.*
import android.os.*
import android.util.*
import com.example.musicapp.R
import com.example.musicapp.service.MyMusicService


class StartActivity : BaseActivity() {

    private val START_DURATION = 1
    private val FINISH_ACTIVITY = 2

    private val handler: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                START_DURATION -> if (!isDestroyed) {
                    sendEmptyMessageDelayed(FINISH_ACTIVITY, 2000)
                }
                FINISH_ACTIVITY -> {
                    val intent = Intent(this@StartActivity, MusicListActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        getReadStoragePermission(this)
        Log.d("初始界面", "onCreate() end")
    }

    override fun onStart() {
        super.onStart()
        val startServiceIntent = Intent(this, MyMusicService::class.java)
        startService(startServiceIntent)
        handler.sendEmptyMessage(START_DURATION) // 开启计时器
        Log.d("初始界面", "onStart() end")
    }

    override fun onResume() {
        super.onResume()
        Log.d("初始界面", "onResume() end")
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacksAndMessages(null) // 停止计时
        Log.d("初始界面", "onStop() end")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("初始界面", "onDestroy() end")
    }
}