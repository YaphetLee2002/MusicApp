package com.example.musicapp.activity

import MusicPojo
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.MusicAdapter
import com.example.musicapp.OnRecyclerItemsClickListener
import com.example.musicapp.R
import com.example.musicapp.service.MyMusicService
import com.example.musicapp.utils.MusicUtils
import com.example.musicapp.utils.ServiceUtils


class MusicListActivity : BaseActivity(){
    private var recyclerView: RecyclerView? = null
    private var musicAdapter: MusicAdapter? = null
    private var musicPojoList: List<MusicPojo>? = null
    private val myMusicConnection = MyMusicConnection()

    override fun onStart() {
        super.onStart()
        Log.d("MusicListActivity", "onStart()")
        refreshMusicList()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MusicListActivity", "onDestroy()")
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
            // 关闭与服务的连接
            unbindService(myMusicConnection)
            // 关闭服务
            if (ServiceUtils.isMusicServiceRunning(this)) {
                this.stopService(this)
            } else {
                Log.d("MusicListActivity", "音乐服务没有运行")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_musiclist)
        registBroadcastReceiver() // 注册广播接收器

        // 绑定音乐服务
        val bindIntent = Intent(this, MyMusicService::class.java)
        bindService(bindIntent, myMusicConnection, BIND_AUTO_CREATE)

        // 清除列表按钮
        val btn_clearList: Button = findViewById<View>(R.id.button_clearMusicList) as Button
        btn_clearList.setOnClickListener(clearList)
        // 音乐列表
        recyclerView = findViewById<View>(R.id.recyclerView_musicList) as RecyclerView
        val manager = LinearLayoutManager(this)
        recyclerView!!.layoutManager = manager
        // 音乐列表数据初始化
        musicPojoList = MusicUtils.loadMusicList() // 从数据库读取音乐列表
        try {
            Log.d("MusicListActivity", "从数据库读取的音乐数：" + musicPojoList!!.size)
            if (musicPojoList!!.isEmpty() || musicPojoList!!.isEmpty()) {
                Toast.makeText(
                    this@MusicListActivity,
                    "【提示】音乐列表为空，请先扫描音乐", Toast.LENGTH_LONG
                ).show()
            }
            initComp()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initComp() {
        musicAdapter = MusicAdapter(musicPojoList!!)
        // 列表项点击事件回调
        musicAdapter!!.setOnRecyclerItemsClickListener(object :
            OnRecyclerItemsClickListener<MusicPojo> {
            override fun onRecyclerItemsClick(view: View?, info: MusicPojo) {
                Toast.makeText(
                    this@MusicListActivity,
                    "播放：" + info.musicName,
                    Toast.LENGTH_SHORT
                ).show()
                playMusic(info)
                val intent = Intent(this@MusicListActivity, PlayerActivity::class.java)
                startActivity(intent)
            }
        })
        recyclerView!!.adapter = musicAdapter
    }

    private fun playMusic(musicPojo: MusicPojo) {
        if (myMusicBinder != null) {
            myMusicBinder!!.initMediaPlayer(musicPojo)
            myMusicBinder!!.play()
        } else {
            Log.d("MusicListActivity", "Binder不存在，播放失败")
        }
    }

    /**
     * 清除列表（删除数据库）
     */
    private val clearList: View.OnClickListener = View.OnClickListener {
        if (MusicUtils.loadMusicList().isNotEmpty()) {
            if (deleteDatabase("Music")) { // 删除数据库
                Log.d("数据库操作日志", "数据库Music已删除")
                Toast.makeText(this@MusicListActivity, "已清空，请重新扫描", Toast.LENGTH_SHORT).show()
                refreshMusicList()
            } else {
                Log.d("数据库操作日志", "【错误】数据库Music删除失败")
            }
        }
    }

    /**
     * 刷新适配器数据。如果适配器为空则初始化控件。
     */
    private fun refreshMusicList() {
        if (musicAdapter != null) {
            musicAdapter!!.refreshMusicList()
            Log.d("MusicListActivity", "刷新Adapter数据")
        } else {
            initComp()
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
}