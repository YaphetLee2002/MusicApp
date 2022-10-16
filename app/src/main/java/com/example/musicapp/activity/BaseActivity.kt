package com.example.musicapp.activity

import MusicPojo
import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.musicapp.R
import com.example.musicapp.service.MyMusicService
import com.example.musicapp.service.MyMusicService.MyMusicBinder
import java.lang.reflect.Method


open class BaseActivity : AppCompatActivity() {
    private var intentFilter: IntentFilter? = null
    var exitBroadcastReceiver: ExitBroadcastReceiver? = null

    /**
     * 内部类：广播接收器。用于调用finish()
     */
    inner class ExitBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            Log.d("广播接收器日志", "活动：" + context::class.java + "调用finish()")
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        setMenuIconVisible(menu, true)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        val intent: Intent
        when (id) {
            R.id.menu_exit -> {
                intent = Intent("com.example.musicapp.EXIT_BROADCAST")
                sendBroadcast(intent)
            }
            R.id.menu_scanMusic -> {
                intent = Intent(applicationContext, ChooseDirectoryActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_musicList -> {
                intent = Intent(applicationContext, MusicListActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_musicPlayer -> {
                intent = Intent(applicationContext, PlayerActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_setting -> {}
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("BaseActivity", "onDestroy()")
    }

    // 注册广播接收器
    open fun registBroadcastReceiver() {
        intentFilter = IntentFilter()
        intentFilter!!.addAction("com.xiaoyao.xiaoyaomusic.EXIT_BROADCAST")
        exitBroadcastReceiver = ExitBroadcastReceiver()
        registerReceiver(exitBroadcastReceiver, intentFilter)
    }

    /**
     * 结束音乐服务
     */
    open fun stopService(context: Context?) {
        val stopIntent = Intent(context, MyMusicService::class.java)
        stopService(stopIntent)
    }

    /**
     * 以下为调用音乐服务所需，要使用音乐服务必须先启动和绑定服务。
     */
    var myMusicBinder: MyMusicBinder? = null

    inner class MyMusicConnection : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d("服务连接日志", "服务连接，调用者：" + getApplicationContext())
            myMusicBinder = service as MyMusicBinder
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Log.d("服务连接日志", "服务断开连接")
        }
    }

    open fun getPlayState(): MusicPojo? {
        val musicPojo = MusicPojo()
        try {
            val spRead = getSharedPreferences("playState", MODE_PRIVATE)
            musicPojo.musicName = spRead.getString("musicName", null)
            musicPojo.musicPath = spRead.getString("musicPath", null)
            musicPojo.musicDuration = spRead.getInt("currentDuration", 0)
            Log.d("SharedPreferences", "读取音乐状态")
            Log.d("SharedPreferences", "读取到的音乐名：" + musicPojo.musicName)
            Log.d("SharedPreferences", "播放到：" + musicPojo.musicDuration)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return musicPojo
    }

    open fun setPlayState(musicPojo: MusicPojo) {
        val spEdit = getSharedPreferences("playState", MODE_PRIVATE).edit()
        try {
            spEdit.putString("musicName", musicPojo.musicName)
            spEdit.putString("musicPath", musicPojo.musicPath)
            spEdit.putInt("currentDuration", musicPojo.musicDuration)
            Log.d("SharedPreferences", "音乐名：" + musicPojo.musicName)
            Log.d("SharedPreferences", "播放到" + musicPojo.musicDuration)
            spEdit.apply()
            Log.d("SharedPreferences", "保存音乐状态")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 获取读取SD卡权限
     */
    open fun getReadStoragePermission(activity: Activity?): Boolean {
        val checkPermission = ContextCompat.checkSelfPermission(
            activity!!,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (checkPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "授权成功", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "【错误】用户取消授权", Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    /**
     * 利用反射，使Menu的图标可见
     * @param menu
     * @param flag 是否可见
     */
    open fun setMenuIconVisible(menu: Menu?, flag: Boolean) {
        //判断menu是否为空
        if (menu != null) {
            try {
                //如果不为空,就反射拿到menu的setOptionalIconsVisible方法
                val method: Method = menu.javaClass.getDeclaredMethod(
                    "setOptionalIconsVisible",
                    java.lang.Boolean.TYPE
                )
                //暴力访问该方法
                method.setAccessible(true)
                //调用该方法显示icon
                method.invoke(menu, flag)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}