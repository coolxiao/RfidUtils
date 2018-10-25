package com.kingyun.som.rfid.scanner

import android.app.Activity
import android.content.Intent
import com.cetc7.M6e.M6eReader
import com.kingyun.som.rfid.RFIDScanner
import com.kingyun.som.rfid.TagListener

/**
 * Created by xifan on 17-11-1.
 */
class M6eScanner : RFIDScanner {
    private var reader: M6eReader? = null

    override fun start(activity: Activity, listener: TagListener?): Boolean {
        if (this.reader == null) {
            this.reader = M6eReader.GetUHFReader()
        }
        val reader = this.reader
        val connect = reader!!.Connect() == 0
        return if (connect) {
            reader.SetPower(true)
            reader.SetM6eAntPower(9)
            reader.SetM6eProtocolConfig(0, 0)
            val epCsListener = M6eReader.OnEPCsListener { arrayList ->
                val info = System.currentTimeMillis().toString()
                val rfid = arrayList[0].replace(" ", "")
                listener?.onSuccess(rfid, info)
            }
            reader.TirggerStart(epCsListener)
            true
        } else {
            false
        }
    }

    override fun stop() {
        reader?.DisConnect()
        reader = null
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