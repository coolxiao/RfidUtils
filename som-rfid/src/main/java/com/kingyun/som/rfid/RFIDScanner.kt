package com.kingyun.som.rfid

import android.app.Activity
import android.content.Intent

/**
 * Created by xifan on 17-11-1.
 */
interface RFIDScanner {
    fun start(activity: Activity, listener: TagListener?): Boolean
    fun stop()

    /**
     * 对于一些需要缓存实例的Scanner，不能立即stop，需要等退出程序时再进行释放
     */
    fun release()

    /**
     * NFC 模块需要实现以下方法，其他Scanner默认不实现
     */
    fun onResume(activity: Activity)
    fun onPause(activity: Activity)
    fun onNewIntent(intent: Intent)
}