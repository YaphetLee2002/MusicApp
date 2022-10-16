package com.example.musicapp.activity

import MusicPojo
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.*
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.FileAdapter
import com.example.musicapp.OnRecyclerItemsClickListener
import com.example.musicapp.R
import com.example.musicapp.pojo.FilePojo

import com.example.musicapp.utils.MusicUtils
import com.example.musicapp.utils.FileUtils
import java.io.File


class ChooseDirectoryActivity : BaseActivity(){

    private lateinit var recyclerView: RecyclerView
    private lateinit var fileAdapter: FileAdapter
    lateinit var editText_path: EditText
    private lateinit var button_upDir: Button
    private lateinit var button_scanDir:Button
    lateinit var checkBox_scanChildDir: CheckBox
    private lateinit var initPath // 初始路径
            : String

    override fun onDestroy() {
        super.onDestroy()
        Log.d("ChooseDirectoryActivity", "onDestroy()")
        unregisterReceiver(exitBroadcastReceiver) // 注销广播接收器
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choosedirectory)
        registBroadcastReceiver() // 注册广播接收器
        try {
            initPath = Environment.getExternalStorageDirectory().toString()
            initComp()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStart() {
        super.onStart()
        try {
            fileAdapter.notifyDataSetChanged()
        } catch (e: Exception) {
        }
        Log.d("ChooseDirectoryActivity", "onStart() end")
    }

    override fun onPause() {
        super.onPause()
    }

    /**
     * 初始化控件并设置事件处理
     */
    private fun initComp() {
        editText_path = findViewById<View>(R.id.editText_path) as EditText
        editText_path.setText(initPath)
        // 实现按下回车不换行
        editText_path.setOnEditorActionListener { v, actionId, event ->
            if (event.keyCode === KeyEvent.KEYCODE_ENTER) {
                true
            } else true
        }
        // 抬起回车键的事件：隐藏键盘，并跳转文件夹
        editText_path.setOnKeyListener(enterActionUp)
        // 复选框
        checkBox_scanChildDir = findViewById<View>(R.id.checkbox_scanChildDir) as CheckBox
        fileAdapter = FileAdapter(FileUtils.getFileList(File(initPath)))
        recyclerView = findViewById<View>(R.id.recyclerView_directory) as RecyclerView
        val manager = LinearLayoutManager(this)
        recyclerView.layoutManager = manager
        recyclerView    .adapter = fileAdapter
        fileAdapter.setOnRecyclerItemsClickListener(object :
            OnRecyclerItemsClickListener<FilePojo> {
            override fun onRecyclerItemsClick(view: View?, info: FilePojo) {
                inDir(info)
            }
        })
        // 上一级
        button_upDir = findViewById<View>(R.id.button_upDirectory) as Button
        button_upDir.setOnClickListener { upDir() }
        // 扫描当前目录
        button_scanDir = findViewById<View>(R.id.button_scanDirectory) as Button
        button_scanDir.setOnClickListener {
            val scanChildDir = checkBox_scanChildDir.isChecked
            val dirPath = editText_path.text.toString()
            if (scanChildDir) {
                scanDir(dirPath, true)
            } else {
                scanDir(dirPath, false)
            }
        }
    }

    /**
     * 点击子项时进入文件夹的事件
     * @param filePojo
     */
    private fun inDir(filePojo: FilePojo) {
        val tempFile = File(filePojo.filePath)
        if (tempFile.isFile) {
            // 当点击项是文件时
            Toast.makeText(
                this@ChooseDirectoryActivity,
                "您点击的是文件：" + tempFile.name,
                Toast.LENGTH_SHORT
            ).show()
        } else {
            // 点击某目录时，重新获取该路径下的文件列表
            val newFilePojoList: List<FilePojo> = FileUtils.getFileList(File(filePojo.filePath))
            fileAdapter.setFilePojoList(newFilePojoList)
            editText_path.setText(filePojo.filePath)
        }
    }

    /**
     * 进入上一级目录
     */
    private fun upDir() {
        // 从editText获取当前路径
        val filePath = editText_path.text.toString()
        if (filePath != initPath) {
            /** BUG: 根目录的上一级目录不能访问  */
            // 获得上一级路径
            val parentPath: String = File(filePath).parent
            editText_path.setText(parentPath)
            // 进入上一级目录
            fileAdapter.setFilePojoList(FileUtils.getFileList(File(parentPath)))
        }
    }

    /**
     * 扫描当前目录
     * @param dirPath
     * @param isScanChildDir 是否扫描子目录
     */
    private fun scanDir(dirPath: String, isScanChildDir: Boolean) {
        val dialog: AlertDialog.Builder = AlertDialog.Builder(this@ChooseDirectoryActivity)
            .setTitle("确定扫描该目录？可能花费较多时间！")
            .setMessage(dirPath)
        dialog.setCancelable(false)
        dialog.setPositiveButton("扫描", DialogInterface.OnClickListener { dialog, which ->
            /** 确定扫描。先获取目录路径，然后利用MusicUtils扫描音乐文件并保存  */
            /** 确定扫描。先获取目录路径，然后利用MusicUtils扫描音乐文件并保存  */
            val musicUtils = MusicUtils()
            val fileList: List<File> = FileUtils().searchMusicFiles(dirPath, isScanChildDir)
            if (fileList.isEmpty()) {
                Toast.makeText(this@ChooseDirectoryActivity, "【错误】要查找的目录没有音乐文件", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val musicPojoList: List<MusicPojo>? = musicUtils.getMusicPojoList(fileList)
                if (musicPojoList != null) {
                    if (musicPojoList.isEmpty()) {
                        Toast.makeText(this@ChooseDirectoryActivity, "【错误】音乐列表为空", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        musicUtils.saveMusicList(musicPojoList)
                        Toast.makeText(this@ChooseDirectoryActivity, "保存音乐列表", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        })
        dialog.setNegativeButton("取消", DialogInterface.OnClickListener { dialog, which ->
            // 不扫描
        })
        dialog.create().show()
    }

    /** 实现抬起回车键时隐藏键盘，并跳转文件夹  */
    private val enterActionUp: View.OnKeyListener =
        View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER &&
                event.action === KeyEvent.ACTION_UP) {
                Log.d("编辑框操作日志", "抬起回车键")
                // 先隐藏键盘
//                (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
//                    .hideSoftInputFromWindow(this@ChooseDirectoryActivity.currentFocus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS)
                // 进入指定目录
                val tempPath = editText_path.text.toString()
                val file = File(tempPath)
                if (file.exists()) { // 如果目录存在
                    /** BUG:如果目录是/storage/emulated/0之前的目录，会无法跳转  */
                    /** BUG:如果目录是/storage/emulated/0之前的目录，会无法跳转  */
                    Log.d("编辑框操作日志", "尝试进入指定目录：$tempPath")
                    try {
                        fileAdapter.setFilePojoList(FileUtils.getFileList(File(tempPath)))
                    } catch (e: Exception) {
                    }
                } else {
                    editText_path.setText(initPath)
                    fileAdapter.setFilePojoList(FileUtils.getFileList(File(initPath)))
                    Toast.makeText(this@ChooseDirectoryActivity, "【错误】目录不存在", Toast.LENGTH_LONG).show()
                }
            }
            false
        }

}