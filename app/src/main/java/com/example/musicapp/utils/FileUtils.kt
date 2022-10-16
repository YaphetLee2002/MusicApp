package com.example.musicapp.utils

import android.util.Log
import com.example.musicapp.R
import com.example.musicapp.pojo.FilePojo
import java.io.File
import java.util.*
import java.util.Collections.addAll
import kotlin.collections.ArrayList


class FileUtils {
    private val lstFile: MutableList<String> = ArrayList() //结果 List
    private val myMusicFileList: List<File> = ArrayList()

    /**
     * 自定义获取指定文件的列表
     * @param path 搜索目录
     * @param extension 扩展名
     * @param isIterative 是否进入子文件夹
     */
    fun searchMyFiles(path: String, extension: String, isIterative: Boolean) {
        val files: Array<out File>? = File(path).listFiles() // 目录下的所有文件
        // 遍历文件
        if (files != null) {
            for (i in files.indices) {
                val f: File = files[i]
                if (f.isFile) {
                    //判断扩展名
                    if (f.name.endsWith(extension)) {
                        lstFile.add(f.path)
                    }
                    if (!isIterative) {
                        break
                    }
                } else if (f.isDirectory && f.path.indexOf("/.") === -1) {
                    //忽略点文件（隐藏文件/文件夹）并继续在子目录寻找
                    searchMyFiles(f.path, extension, isIterative)
                }
            }
        }
    }

    /**
     * 扫描音乐文件
     * @param path 要扫描的目录
     * @param isIterative 是否扫描子目录
     * @return
     */
    fun searchMusicFiles(path: String, isIterative: Boolean): List<File> {
        val fileList: MutableList<File> = ArrayList()
        val file = File(path)
        val files: Array<File> = file.listFiles() // 目录下的所有文件
        Log.d("扫描日志", "开始扫描路径：$path")
        /**
         * 特殊情况：当目录为空时
         */
        try {
            if (files == null) {
                Log.d("扫描日志", "目录为空：【" + file.name.toString() + "】")
                throw NullPointerException()
            }
        } catch (e: Exception) {
        }
        // 遍历文件
        for (i in files.indices) {
            val f: File = files[i]
            if (f.isFile) {
                val fileName: String = f.name
                // 获取扩展名
                val extension = fileName.substring(fileName.lastIndexOf(".") + 1)
                Log.d("扫描日志", "文件名：$fileName 识别的扩展名：$extension")
                //判断扩展名
                if (extension.equals("mp3", ignoreCase = true) || extension.equals(
                        "aac",
                        ignoreCase = true
                    )
                    || extension.equals("3gp", ignoreCase = true) || extension.equals(
                        "m4a",
                        ignoreCase = true
                    )
                    || extension.equals("flac", ignoreCase = true) || extension.equals(
                        "wav",
                        ignoreCase = true
                    )
                    || extension.equals("ogg", ignoreCase = true) || extension.equals(
                        "ape",
                        ignoreCase = true
                    )
                ) {
                    fileList.add(f)
                } else {
                    Log.d("扫描日志", "【$fileName】不是音乐文件")
                }
            } else if (f.isDirectory && isIterative) { // 若是文件夹,且确认扫描子目录
                Log.d("扫描日志", "【递归】扫描子目录：" + f.name)
                searchMusicFiles(f.path, isIterative)
            } else if (f.isDirectory && !isIterative) {
                Log.d("扫描日志", "不扫描子目录：" + f.path)
            }
        }
        /**
         * 特殊情况：当前扫描的目录中没有音乐文件，导致返回的音乐文件列表为空
         */
        try {
            if (fileList.isEmpty()) {
                Log.d("扫描日志", "目录中没有音乐文件：" + file.name)
                throw NullPointerException()
            }
        } catch (e: Exception) {
        }
        return fileList
    }

    fun getMyMusicFileList(): List<File> {
        if (myMusicFileList.isEmpty()) {
            throw NullPointerException("列表为空")
        }
        return myMusicFileList
    }

    companion object {
        /**
         * 获取文件列表
         * @param sourceFile 目录路径
         * @return
         */
        fun getFileList(sourceFile: File?): List<FilePojo> {
            val filePojoList: MutableList<FilePojo> = ArrayList()
            val fileList: ArrayList<File> = ArrayList()
            // 注意：先动态获取读取SD卡权限
            if (sourceFile != null) {
                for (file in sourceFile.listFiles()!!) {
                    addAll(fileList, file)
                }
            }
            for (file in fileList) {
                val fileName: String = file.name
                //默认是文件图标
                var imageId: Int = R.drawable.file
                //下面开始判断文件是文件夹或音乐文件
                if (file.isDirectory) {
                    // 是文件夹
                    imageId = R.drawable.folder
                } else {
                    //如果是文件，就从文件名的后缀名来判断是什么文件，从而添加对应图标
                    //获取后缀名前的分隔符"."在fName中的位置。
                    val dotIndex = fileName.lastIndexOf(".")
                    if (dotIndex >= 0) {
                        /* 获取文件的后缀名*/
                        val end = fileName.substring(dotIndex, fileName.length).lowercase(Locale.getDefault())
                        if (!Objects.equals(end, "")) {
                            if (Objects.equals(end, ".mp3") || Objects.equals(end, ".ape")
                                || Objects.equals(end, ".flac") || Objects.equals(end, ".m4a")
                                || Objects.equals(end, ".ape") || Objects.equals(end, ".wav")
                                || Objects.equals(end, ".aac")
                            ) {
                                // 如果是音乐文件
                                imageId = R.drawable.music
                            }
                        }
                    }
                }
                val filePath: String = file.path
                val myFile = FilePojo(fileName, imageId, filePath)
                filePojoList.add(myFile)
            }
            return filePojoList
        }
    }
}
