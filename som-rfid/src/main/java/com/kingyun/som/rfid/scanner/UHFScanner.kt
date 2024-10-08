package com.kingyun.som.rfid.scanner

import android.app.Activity
import android.content.Intent
import android.os.Build
import com.kingyun.som.rfid.RFIDScanner
import com.kingyun.som.rfid.RFIDUtils
import com.kingyun.som.rfid.TagListener
import java.io.File

/**
 * Created by xifan on 17-11-2.
 */
class  UHFScanner(var device: File?) : RFIDScanner {
    init {
        if (device == null) {
            device = when (Build.VERSION.SDK_INT) {
                Build.VERSION_CODES.JELLY_BEAN_MR1 -> File("/dev/ttyMT0")
                Build.VERSION_CODES.LOLLIPOP_MR1, Build.VERSION_CODES.N -> File("/dev/ttyMT2")
                else -> File("/dev/ttyMT1")
            }
        }
        val hasPermission = RFIDUtils.checkDevicePermission(device)
        if (!hasPermission) {
            throw SecurityException()
        }
    }

    override fun start(activity: Activity, listener: TagListener?): Boolean {
        val success = RFIDUtils.connect(activity.application, device!!)
        return if (success) {
            RFIDUtils.start(activity.application, listener)
            true
        } else {
            false
        }
    }

    override fun stop() {
        RFIDUtils.disconnect()
        RFIDUtils.release()
    }

    override fun release() {
    }

    override fun onResume(activity: Activity) {
    }

    override fun onPause(activity: Activity) {
    }

    override fun onNewIntent(intent: Intent) {
    }
}