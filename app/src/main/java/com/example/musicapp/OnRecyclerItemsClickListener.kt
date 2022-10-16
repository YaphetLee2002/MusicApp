package com.example.musicapp

import android.view.View
import com.example.musicapp.pojo.FilePojo


/**
 * RecyclerView回调接口。当点击其子项时，可交回Activity处理
 * @param <T> 类型应为子项类（Pojo类）
 */
interface OnRecyclerItemsClickListener<T>{
    /**
     * 该方法可在Activity中重写，即点击子项时的事件处理
     * @param view
     * @param info
     */
    fun onRecyclerItemsClick(view: View?, info: T)
}