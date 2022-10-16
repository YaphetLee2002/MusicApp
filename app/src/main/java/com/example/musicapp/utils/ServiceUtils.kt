package com.example.musicapp.utils

import android.app.ActivityManager
import android.content.Context
import android.util.Log


class ServiceUtils {
    /**
     * 判断服务是否正在运行
     * @param mContext   上下文对象
     * @param className  Service类的全路径类名 "包名+类名" 如com.demo.test.MyService
     * @return
     */
    fun isServiceRunning(mContext: Context, className: String): Boolean {
        // ActivityManager用于管理Activity
        val mActivityManager = mContext
            .applicationContext.getSystemService(
                Context.ACTIVITY_SERVICE
            ) as ActivityManager
        var runSerInfoList: List<ActivityManager.RunningServiceInfo> = ArrayList()
        // getRunningServices()得到正在运行的服务
        runSerInfoList = mActivityManager.getRunningServices(30)
        // 遍历数组判断是否存在服务
        for (i in runSerInfoList.indices) {
            val serName = runSerInfoList[i].service.className
            Log.d("服务工具类日志", "找到服务：$serName")
            if (serName == className) {
                return true
            }
        }
        return false
    }

    companion object {
        /**
         * 判断音乐服务是否正在运行
         * @param mContext 上下文对象
         * @return
         */
        fun isMusicServiceRunning(mContext: Context): Boolean {
            // ActivityManager用于管理Activity
            val mActivityManager = mContext
                .applicationContext.getSystemService(
                    Context.ACTIVITY_SERVICE
                ) as ActivityManager
            // getRunningServices()得到正在运行的服务
            val runSerInfoList: List<ActivityManager.RunningServiceInfo> = mActivityManager.getRunningServices(30)
            // 遍历数组判断是否存在服务
            for (i in runSerInfoList.indices) {
                val serName = runSerInfoList[i].service.className
                Log.d("服务工具类日志", "找到服务：$serName")
                if (serName == "com.example.musicapp.service.MyMusicService") {
                    return true
                }
            }
            return false
        }
    }
}
