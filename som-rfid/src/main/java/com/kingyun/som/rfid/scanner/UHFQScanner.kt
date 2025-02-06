package com.kingyun.som.rfid.scanner

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import cn.pda.serialport.Tools
import com.kingyun.som.rfid.RFIDScanner
import com.kingyun.som.rfid.TagListener
import com.kingyun.som.rfid.rugged.UhfManager
import kotlin.concurrent.thread

class UHFQScanner() : RFIDScanner {
    var uhfManager: UhfManager? = null

    override fun start(activity: Activity, listener: TagListener?): Boolean {
        uhfManager = UhfManager.getInstance()
        uhfManager?.setOutputPower(26)
        uhfManager?.setWorkArea(UhfManager.WorkArea_USA)
        val handler = Handler(Looper.getMainLooper())
        thread {
            while (true) {
                val epcList = uhfManager?.inventoryRealTime()
                if (epcList != null && epcList.isNotEmpty()) {
                    for (epc in epcList) {
                        val epcStr: String = Tools.Bytes2HexString(epc,
                                epc.size)
                        handler.post {
                            listener?.onSuccess(epcStr, "")
                        }
                        return@thread
                    }
                }
            }
        }
        return true
    }

    override fun stop() {
        uhfManager?.close()
        uhfManager = null
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