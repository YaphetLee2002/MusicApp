package com.example.musicapp

import MusicPojo
import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.utils.MusicUtils


class MusicAdapter     //构造方法
    (private var musicPojoList: List<MusicPojo>) :
    RecyclerView.Adapter<MusicAdapter.ViewHolder>() {
    // 用于回调
    lateinit var mOnRecyclerItemsClickListener: OnRecyclerItemsClickListener<MusicPojo>

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var musicPojoView: View
        var musicId // 音乐ID
                : TextView
        var musicName // 音乐名
                : TextView
        var isLove // 是否收藏
                : ImageButton

        init {
            musicPojoView = view
            musicId = view.findViewById(R.id.musicId)
            musicName = view.findViewById(R.id.musicName)
            isLove = view.findViewById(R.id.imageButton_isLove)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // 载入子项布局
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.music_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val musicPojo = musicPojoList[position]
        holder.musicId.text = musicPojo.id.toString() // 注意将获得的Int转String
        holder.musicName.text = musicPojo.musicName
        // 设置收藏按钮图片
        if (musicPojo.isLove) {
            holder.isLove.setImageResource(R.drawable.love)
        } else {
            holder.isLove.setImageResource(R.drawable.unlove)
        }
        holder.musicPojoView.setOnClickListener { v ->
            // 使用回调给Activity处理
            if (mOnRecyclerItemsClickListener != null) {
                // 将点击后的子项MusicPojo回调
                mOnRecyclerItemsClickListener.onRecyclerItemsClick(v, musicPojoList[position])
            }
        }
        // 用户点击收藏按钮，判断是否已收藏，并更新数据库对应项
        holder.isLove.setOnClickListener {
            if (musicPojo.isLove) {
                musicPojo.isLove = false
                holder.isLove.setImageResource(R.drawable.unlove)
                Log.d("MusicAdapter", "【取消收藏】" + musicPojo.musicName)
            } else {
                musicPojo.isLove = true
                holder.isLove.setImageResource(R.drawable.love)
                Log.d("MusicAdapter", "【收藏】" + musicPojo.musicName)
            }
            if (musicPojo.isSaved) {
                musicPojo.save() // 更新数据库
            }
        }
    }

    override fun getItemCount(): Int {
        return musicPojoList.size
    }

    /**
     * 用于回调。给RecyclerView设置Adapter时调用它，参数里new一个OnRecyclerItemsClickListener类
     * @param onRecyclerItemsClickListener 要实现的接口
     */
    fun setOnRecyclerItemsClickListener(onRecyclerItemsClickListener: OnRecyclerItemsClickListener<MusicPojo>) {
        mOnRecyclerItemsClickListener = onRecyclerItemsClickListener
    }

    fun refreshMusicList() {
        try {
            musicPojoList = MusicUtils.loadMusicList()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        notifyDataSetChanged()
    }
}