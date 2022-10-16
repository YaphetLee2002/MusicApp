package com.example.musicapp.service

import MusicPojo
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import java.io.IOException


class MyMusicService : Service(){

    private lateinit var myMediaPlayer : MediaPlayer
    private val myMusicBinder = MyMusicBinder()

    override fun onBind(p0: Intent?): IBinder? {
        Log.d("MyMusicService", "服务onBind()")
        return myMusicBinder
    }

    override fun onCreate() {
        super.onCreate()
        myMediaPlayer = MediaPlayer()
        Log.e("MyMusicService", "服务onCreate(), 调用者：$applicationContext")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("MyMusicService", "服务onStartCommand()")
        if (myMediaPlayer == null) {
            myMediaPlayer = MediaPlayer()
            Log.d("MyMusicService", "MediaPlayer不存在，初始化MediaPlayer")
        }

        return super.onStartCommand(intent, flags, startId)
    }

    inner class MyMusicBinder : Binder() {
        @get:Throws(NullPointerException::class)
        var currentMusic: MusicPojo? = null
            private set

        // 判断是否处于播放状态
        val isPlaying: Boolean
            get() = myMediaPlayer.isPlaying()

        // 重置音乐状态，设置音乐文件的路径，并进入准备状态
        fun initMediaPlayer(music: MusicPojo) {
            try {
                currentMusic = music
                myMediaPlayer.reset() // 重置音乐状态
//                myMediaPlayer.setDataSource(music.getMusicPath()) // 设置音乐源
                myMediaPlayer.prepare() // 准备状态
            } catch (e: IOException) {
                Log.d("音乐服务日志", "设置音乐路径错误")
                e.printStackTrace()
            }
        }

        //播放音乐
        fun play() {
            /**
             * BUG：MediaPlayer若没有指定过数据源并进入准备状态，不能播放音乐（没有记录播放状态）
             */
            if (!myMediaPlayer.isPlaying) {
                myMediaPlayer.start()
                Log.d("音乐服务日志", "播放")
            }
        }

        // 暂停音乐
        fun pause() {
            if (myMediaPlayer.isPlaying) {
                myMediaPlayer.pause()
                Log.d("音乐服务日志", "暂停")
            }
        }

        // 停止播放音乐
        fun stop() {
            if (myMediaPlayer.isPlaying) {
                myMediaPlayer.stop()
            }
            Log.d("音乐服务日志", "停止播放")
        }

        //返回歌曲的长度，单位为毫秒
        val duration: Int
            get() = myMediaPlayer.duration

        //返回歌曲目前的进度，单位为毫秒
        val currentPosition: Int
            get() = myMediaPlayer.currentPosition

        //设置歌曲播放的进度，单位为毫秒
        fun seekTo(mes: Int) {
            myMediaPlayer.seekTo(mes)
        }

        // 设置单曲循环
        fun setRepeating(repeat: Boolean): Boolean {
            if (repeat) {
                return if (myMediaPlayer.isLooping) {
                    Log.d("音乐服务日志", "已经是循环播放状态，取消循环")
                    myMediaPlayer.isLooping = false
                    false
                } else {
                    myMediaPlayer.isLooping = repeat
                    Log.d("音乐服务日志", "设置重复播放")
                    true
                }
            }
            return false
        }

        // 判断是否处于循环播放状态
        val isLooping: Boolean
            get() = myMediaPlayer.isLooping
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("音乐服务日志", "服务onUnbind()，解除绑定")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        // 关闭服务时结束播放并释放资源
        if (myMediaPlayer != null) {
            if (myMediaPlayer.isPlaying) {
                myMediaPlayer.stop()
            }
            myMediaPlayer.reset()
            myMediaPlayer.release() // 释放资源
            Log.d("音乐服务日志", "MediaPlayer不为空，释放资源")
        }
        Log.d("音乐服务日志", "服务onDestroy()")
    }
}