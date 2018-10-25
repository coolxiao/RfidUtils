package com.kingyun.som.rfid.scanner

import android.app.Activity
import android.content.Intent
import cn.pda.rfid.hf.HfError
import cn.pda.rfid.hf.HfReader
import cn.pda.serialport.Tools
import com.kingyun.som.rfid.RFIDScanner
import com.kingyun.som.rfid.TagListener
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 * Created by xifan on 17-11-1.
 * Support 14443A only
 */
class HFScanner : RFIDScanner {
    @Volatile private var reader: HfReader? = null
    @Volatile private var listener: TagListener? = null
    @Volatile private var loop: Boolean = false

    override fun start(activity: Activity, listener: TagListener?): Boolean {
        doAsync {
            this@HFScanner.reader = HfReader(14, HfReader.POWER_PSAM)
            this@HFScanner.listener = listener
            loopNfc()
        }
        return true
    }

    private fun loopNfc() {
        doAsync {
            loop = true
            while (loop) {
                val error = HfError()
                val result = reader!!.findCard14443A(error)
                if (result != null) {
                    uiThread {
                        listener?.onSuccess(Tools.Bytes2HexString(result, result.size), "")
                    }
                } else {
                    // try another card
//                val resultList = reader!!.findCard15693(error)
                    Thread.sleep(300)
                }
            }
        }
    }

    override fun stop() {
        loop = false
        reader?.close()
        reader = null
        listener = null
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