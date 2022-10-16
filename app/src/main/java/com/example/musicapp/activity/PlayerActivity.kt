package com.example.musicapp.activity

import MusicPojo
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import com.example.musicapp.R
import com.example.musicapp.service.MyMusicService
import com.example.musicapp.utils.MusicUtils
import com.example.musicapp.utils.ServiceUtils
import org.litepal.LitePal


class PlayerActivity : BaseActivity() {
    private var currentMusicName: TextView? = null
    private var currentTime: TextView? = null
    private var totalTime: TextView? = null
    private var playOrPause: Button? = null
    private var prev: Button? = null
    private var next: Button? = null
    private var loop: Button? = null
    private var seekBar: SeekBar? = null
    private val START_HANDLER = 1
    private val myMusicConnection = MyMusicConnection()
    private val handler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                START_HANDLER -> {
                    try {
                        if (myMusicBinder != null &&
                            myMusicBinder!!.currentMusic != null
                        ) {
                            updateSeekBar() // 先更新进度条
                            updateMusicName()
                            updateTotalMusicTime()
                            updatePlayButtonBackground()
                            updateLoopBackground()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    sendEmptyMessageDelayed(START_HANDLER, 1000) // 若已触发则定时1000毫秒执行
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        registBroadcastReceiver() // 注册广播接收器
        val bindIntent = Intent(this@PlayerActivity, MyMusicService::class.java)
        bindService(bindIntent, myMusicConnection, BIND_AUTO_CREATE)
        initComp()
    }

    override fun onStart() {
        super.onStart()
        handler.sendEmptyMessage(START_HANDLER) // 开启计时器
        Log.d("PlayActivity", "onStart() end")
    }

    override fun onResume() {
        super.onResume()
        Log.d("PlayActivity", "onResume() end")
    }

    override fun onStop() {
        super.onStop()
        Log.d("PlayActivity", "onStop()")
        handler.removeCallbacksAndMessages(null) // 停止计时
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("PlayActivity", "onDestroy()")
        unregisterReceiver(exitBroadcastReceiver) // 注销广播接收器
        try {
            if (myMusicBinder != null && myMusicBinder!!.isPlaying) {
                myMusicBinder!!.pause()
            }
            if (myMusicBinder != null) {
                // 记录播放状态
                val tempMpj = myMusicBinder!!.currentMusic
                if (tempMpj != null) {
                    tempMpj.musicDuration = myMusicBinder!!.currentPosition
                    setPlayState(tempMpj) // 写入到文件
                }
            }
            if (myMusicBinder != null) {
                unbindService(myMusicConnection) // 关闭与服务的连接
            }
            // 关闭服务
            if (ServiceUtils.isMusicServiceRunning(this)) {
                this.stopService(this)
            } else {
                Log.d("PlayActivity", "音乐服务没有运行")
            }
        } catch (e: Exception) {
        }
    }

    private fun initComp() {
        currentMusicName = findViewById<View>(R.id.currentMusicName) as TextView
        currentTime = findViewById<View>(R.id.currentMusicTime) as TextView
        totalTime = findViewById<View>(R.id.totalMusicTime) as TextView
        seekBar = findViewById<View>(R.id.seekBar) as SeekBar
        seekBar!!.setOnSeekBarChangeListener(MySeekBarChangeListener())
        playOrPause = findViewById<View>(R.id.play) as Button
        playOrPause!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                playOrPauseMusic()
            }
        })
        prev = findViewById<View>(R.id.prevMusic) as Button
        prev?.setOnClickListener { playPrevMusic() }
        next = findViewById<View>(R.id.nextMusic) as Button
        next?.setOnClickListener { playNextMusic() }
        loop = findViewById<View>(R.id.repeatThis) as Button
        loop?.setOnClickListener {
            if (myMusicBinder?.setRepeating(true) == true) {
                loop?.setBackgroundResource(R.drawable.isloop)
                Toast.makeText(this@PlayerActivity, "【提示】单曲循环", Toast.LENGTH_LONG).show()
            } else {
                loop?.setBackgroundResource(R.drawable.loop)
                Toast.makeText(this@PlayerActivity, "【提示】取消循环", Toast.LENGTH_LONG).show()
            }
        }
        currentMusicName?.text = "没有正在播放的音乐"
        currentTime?.text = "0"
        totalTime?.text = "0"
    }

    // 播放或暂停按钮事件
    private fun playOrPauseMusic() {
        if (myMusicBinder != null &&
            myMusicBinder!!.currentMusic != null
        ) {
            if (myMusicBinder!!.isPlaying) { // 若正在播放则暂停
                playOrPause?.setBackgroundResource(R.drawable.play) // 背景设为播放
                myMusicBinder!!.pause()
            } else if (!myMusicBinder!!.isPlaying) { // 若已暂停则继续播放
                playOrPause?.setBackgroundResource(R.drawable.pause) // 背景设为暂停
                myMusicBinder!!.play()
            }
        } else {
            Log.d("PlayActivity", "【错误】Binder或MusicPojo不存在")
        }
    }

    private fun playPrevMusic() {
        val currentId = myMusicBinder!!.currentMusic!!.id
        val prevMusic: MusicPojo
        if (currentId < 2) { // 如果当前ID为第一项
            prevMusic = LitePal.findLast(MusicPojo::class.java) // 播放最后一项
            myMusicBinder!!.initMediaPlayer(prevMusic)
            myMusicBinder!!.play()
        } else {
            prevMusic = LitePal.where("id = ?", Integer.toString(currentId - 1))
                .find(MusicPojo::class.java).get(0)
            myMusicBinder!!.initMediaPlayer(prevMusic)
            myMusicBinder!!.play()
        }
        updateTotalMusicTime()
        Toast.makeText(this@PlayerActivity, "播放上一首：" + prevMusic.musicName, Toast.LENGTH_SHORT)
            .show()
    }

    private fun playNextMusic() {
        val totalMusic: Int = LitePal.findAll(MusicPojo::class.java).size // 音乐项总数
        val currentId = myMusicBinder?.currentMusic!!.id
        val nextMusic: MusicPojo
        if (currentId == totalMusic) { // 如果当前ID为最后一项
            nextMusic = LitePal.findFirst(MusicPojo::class.java) // 播放第一项
            myMusicBinder!!.initMediaPlayer(nextMusic)
            myMusicBinder!!.play()
        } else {
            nextMusic = LitePal.where("id = ?", Integer.toString(currentId + 1))
                .find(MusicPojo::class.java)[0]
            myMusicBinder!!.initMediaPlayer(nextMusic)
            myMusicBinder!!.play()
        }
        updateTotalMusicTime()
        Toast.makeText(this@PlayerActivity, "播放下一首：" + nextMusic.musicName, Toast.LENGTH_SHORT)
            .show()
    }

    // 更新当前音乐名
    private fun updateMusicName() {
        if (myMusicBinder != null) {
            val mName = myMusicBinder!!.currentMusic!!.musicName
            currentMusicName!!.text = mName
        }
    }

    // 更新当前音乐总时
    private fun updateTotalMusicTime() {
        if (myMusicBinder != null) {
            val tTime = myMusicBinder!!.duration
            totalTime!!.text = MusicUtils.formatMusicTime(tTime)
        }
    }

    // 更新当前播放时间
    private fun updateCurrentMusicTime() {
        if (myMusicBinder != null) {
            val cTime: Int = myMusicBinder!!.currentPosition
            currentTime!!.text = MusicUtils.formatMusicTime(cTime)
        }
    }

    // 更新播放按钮的背景
    private fun updatePlayButtonBackground() {
        if (myMusicBinder != null) {
            if (myMusicBinder!!.isPlaying) { // 若正在播放则设为暂停
                playOrPause?.setBackgroundResource(R.drawable.pause) // 背景设为暂停
            } else if (!myMusicBinder!!.isPlaying) { // 若已暂停则继续播放
                playOrPause?.setBackgroundResource(R.drawable.play) // 背景设为播放
            }
        }
    }

    // 更新进度条
    private fun updateSeekBar() {
        if (myMusicBinder != null) {
            seekBar!!.max = myMusicBinder!!.duration // 设置最大值
            seekBar!!.progress = myMusicBinder!!.currentPosition // 设置当前值
        }
    }

    // 更新循环图
    private fun updateLoopBackground() {
        if (myMusicBinder != null) {
            if (myMusicBinder!!.isLooping) {
                loop?.setBackgroundResource(R.drawable.isloop)
            } else {
                loop?.setBackgroundResource(R.drawable.loop)
            }
        }
    }

    /**
     * 实现按返回键不销毁活动
     * @param keyCode
     * @param event
     * @return
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true) // 挂后台
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * 内部类 进度条改变监听器
     */
    private inner class MySeekBarChangeListener : OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            /**
             * 进度条改变时，应做如下操作：
             * 1、代替handler修改当前播放时间
             * 2、
             */
            if (fromUser) {
                currentTime!!.text = MusicUtils.formatMusicTime(seekBar.progress)
                if (myMusicBinder != null) {
                    myMusicBinder!!.seekTo(seekBar.progress)
                }
            } else {
                updateCurrentMusicTime()
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            /**
             * 开始拖动进度条
             * 1、
             * 2、需要提前停止handle更新进度条，否则无法更新播放时间
             */
            handler.removeCallbacksAndMessages(null) // 停止计时
            currentTime!!.textSize = 36f
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            /**
             * 停止拖动进度条
             * 1、
             * 2、继续让handler更新进度条
             */
            handler.sendEmptyMessage(START_HANDLER) // 开始计时
            currentTime!!.textSize = 15f // TextView默认字体大小为15
        }
    }
}
