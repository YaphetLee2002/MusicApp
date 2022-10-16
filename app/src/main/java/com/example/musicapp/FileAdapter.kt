package com.example.musicapp

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.musicapp.pojo.FilePojo


class FileAdapter(private var filePojoList: List<FilePojo>) :
    RecyclerView.Adapter<FileAdapter.ViewHolder>() {
    fun setFilePojoList(filePojoList: List<FilePojo>) {
        this.filePojoList = filePojoList
        notifyDataSetChanged() // 更新数据
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var fileView: View = view
        var fileImage: ImageView = view.findViewById(R.id.fileImage) as ImageView
        var fileName: TextView = view.findViewById(R.id.fileName)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.file_item, parent, false)
        // 已经在onBindViewHolder绑定事件
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val filePojo = filePojoList[position]
        holder.fileImage.setImageResource(filePojo.imageId)
        holder.fileName.text = filePojo.fileName
        holder.fileView.setOnClickListener { v ->
            mOnRecyclerItemsClickListener.onRecyclerItemsClick(
                v,
                filePojoList[position]
            )
        }
    }

    override fun getItemCount(): Int {
        return filePojoList.size
    }

    lateinit var mOnRecyclerItemsClickListener: OnRecyclerItemsClickListener<FilePojo>
    fun setOnRecyclerItemsClickListener(onRecyclerItemsClickListener: OnRecyclerItemsClickListener<FilePojo>) {
        mOnRecyclerItemsClickListener = onRecyclerItemsClickListener
    }
}