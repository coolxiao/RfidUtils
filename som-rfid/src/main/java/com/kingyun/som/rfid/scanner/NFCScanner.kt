package com.kingyun.som.rfid.scanner

import android.app.Activity
import android.content.Intent
import com.kingyun.som.rfid.RFIDScanner
import com.kingyun.som.rfid.TagListener
import com.kingyun.som.rfid.nfc.NfcHelper

/**
 * Created by xifan on 17-11-2.
 */
class NFCScanner:RFIDScanner {
    private var helper:NfcHelper?= null
    private var listener: TagListener? = null

    override fun start(activity: Activity, listener: TagListener?): Boolean {
        val status = NfcHelper.getStatus(activity)
        return if (status > 0) {
            helper = NfcHelper(activity)
            this.listener = listener
            true
        } else {
            false
        }
    }

    override fun onNewIntent(intent: Intent) {
        if (helper?.onNewIntent(intent) == true) {
            listener?.onSuccess(helper!!.hexId,"")
        }
    }

    override fun onResume(activity: Activity) {
        helper?.onResume(activity)
    }

    override fun onPause(activity: Activity) {
        helper?.onPause(activity)
    }

    override fun stop() {
        helper?.release()
        helper= null
    }

    override fun release() {
    }
}