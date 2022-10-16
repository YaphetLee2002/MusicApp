package com.example.musicapp.utils

import MusicPojo
import android.media.MediaMetadataRetriever
import android.util.Log
import org.litepal.LitePal
import org.litepal.LitePal.getDatabase
import java.io.File


class MusicUtils {

    /**
     * 从音乐文件列表获取音乐信息
     * MediaMetadataRetriever类，解析媒体文件、获取媒体文件中取得帧和元数据（视频/音频包含的标题、格式、艺术家等信息）
     * @return 返回MusicPojo类的List
     */
    fun getMusicPojoList(fileList: List<File>): List<MusicPojo> {
        val musicPojoList: MutableList<MusicPojo> = ArrayList()
        var musicPojo: MusicPojo
        val mmr = MediaMetadataRetriever()
        for (i in fileList.indices) {
            musicPojo = MusicPojo()
            // 设置文件名
            musicPojo.musicName = fileList[i].name
            // 设置文件路径
            musicPojo.musicPath = fileList[i].path
            // 设置数据源
            mmr.setDataSource(fileList[i].path)
            // 设置标题
            //String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            // 设置艺术家
            val artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            musicPojo.musicArtist = artist
            // 设置播放时长，单位毫秒
            val duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            musicPojo.musicDuration = duration!!.toInt()
            // 设置是否收藏，默认否
            musicPojo.isLove = false
            musicPojoList.add(musicPojo)
        }
        return musicPojoList
    }

    /**
     * 保存音乐列表到数据库
     * @param musicPojoList
     */
    fun saveMusicList(musicPojoList: List<MusicPojo>?) {
        getDatabase() // 创建数据库
        val oldList = loadMusicList() // 数据库中原来的List
        if (musicPojoList != null) {
            if (oldList == null) {
                LitePal.saveAll(musicPojoList)
                Log.d("数据库操作日志", "原音乐列表为空，直接全部添加")
            } else {
                /**
                 * 【保存逻辑】每扫描到一个音乐文件，
                 * 就在数据库判断这项是否存在，无则添加。 */
                for (i in musicPojoList.indices) {
                    // 如果新文件在数据库中不存在，则添加
                    val tempPath = musicPojoList[i].musicPath
                    // 查询。若修改了MusicPojo则这里也要改。
                    val tempMPJ: MusicPojo =
                        LitePal.select("id", "musicName", "musicPath", "isLove")
                            .where("musicPath = ?", tempPath)
                            .findFirst(MusicPojo::class.java)
                    if (tempMPJ != null) {
                        Log.d("数据库操作日志", "【不添加】存在相同音乐文件：" + tempMPJ.musicName)
                    } else {
                        musicPojoList[i].save()
                        Log.d("数据库操作日志", "【添加】" + musicPojoList[i].musicName)
                    }
                }
            }
        }
    }

    companion object {

        /**
         * 从数据库读取音乐列表
         */
        fun loadMusicList(): List<MusicPojo> {
            val musicPojoList: List<MusicPojo> = LitePal.findAll(MusicPojo::class.java)
            if (musicPojoList.isEmpty()) {
                Log.d("音乐工具类", "【错误】从数据库获得的音乐列表为空")
            }
            Log.d("音乐工具类", "从数据库读取音乐列表成功")
            return musicPojoList
        }

        /**
         * 获得收藏的音乐
         * @return
         */
        fun loadFavoriteMusicList(): List<MusicPojo>? {
            // 从音乐表读取收藏的音乐。注：litepal中false为0，true为1
            val musicList: List<MusicPojo> = LitePal
                .where("isLove = ?", "1").find(MusicPojo::class.java)
            if (musicList.isEmpty()) {
                Log.d("音乐工具类", "【错误】读取失败，数据库中无收藏的音乐")
            }
            // 重置ID并添加到List
            for (i in musicList.indices) {
                musicList[i].id = i + 1
                Log.d(
                    "音乐工具类", "读取到收藏的音乐：【id】" + musicList[i].id
                            + " 【name】" + musicList[i].musicName
                )
            }
            return musicList
        }

        /**
         * 格式化得到的音乐时间：毫秒转分:秒
         * @param time
         * @return
         */
        fun formatMusicTime(time: Int): String {
            // 得到的time为毫秒
            val sb = StringBuilder()
            var min = (time / (1000 * 60)).toString() + ""
            var second = (time % (1000 * 60) / 1000).toString() + ""
            if (min.length < 2) {
                min = "0$min"
            }
            if (second.length < 2) {
                second = "0$second"
            }
            sb.append(min)
            sb.append(":")
            sb.append(second)
            return sb.toString()
        }
    }
}