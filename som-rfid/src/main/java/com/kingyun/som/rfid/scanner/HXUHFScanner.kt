package com.kingyun.som.rfid.scanner

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.cw.phychipsuhfsdk.UHFHXAPI
import com.cw.serialportsdk.cw
import com.cw.serialportsdk.utils.DataUtils
import com.kingyun.som.rfid.RFIDScanner
import com.kingyun.som.rfid.TagListener
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 * Created by xifan on 17-11-2.
 */
class HXUHFScanner() : RFIDScanner {
    private var api: UHFHXAPI? = null
    private var listener: TagListener? = null
    private var loop: Boolean = false

    override fun start(activity: Activity, tagListener: TagListener?): Boolean {
        api = UHFHXAPI()
        listener = tagListener
        val open = api!!.openHXUHFSerialPort(cw.getDeviceModel())
        if (open) {
            read()
        }
        return open
    }

    private fun read() {
        doAsync {
            api!!.readEPC(object : UHFHXAPI.AutoRead {
                override fun start() {
                    Log.e("HXUHFScanner", "start")
                }

                override fun timeout() {
                    read()
                    Log.e("HXUHFScanner", "timeout")
                }

                override fun processing(p0: ByteArray?) {
                    Log.e("HXUHFScanner", p0.toString())
                    uiThread {
                        listener?.onSuccess(DataUtils.toHexString(p0).substring(4).toUpperCase(), "")
                    }
                }

                override fun end() {
                    read()
                    Log.e("HXUHFScanner", "end")
                }
            })
        }
    }

    override fun stop() {
        loop = false
        api?.closeHXUHFSerialPort(cw.getDeviceModel())
        api = null
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