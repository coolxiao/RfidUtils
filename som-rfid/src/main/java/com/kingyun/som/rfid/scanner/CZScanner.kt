package com.kingyun.som.rfid.scanner

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.kingyun.som.rfid.RFIDScanner
import com.kingyun.som.rfid.TagListener
import com.speedata.libuhf.IUHFService
import com.speedata.libuhf.UHFManager
import com.speedata.libuhf.bean.SpdInventoryData
import com.speedata.libuhf.interfaces.OnSpdInventoryListener


/**
 * Created by xifan on 17-12-18.
 */
class CZScanner : RFIDScanner {

    private var iuhfService: IUHFService? = null
    private var nfcScanner: NFCScanner? = null
    private var tagListener: TagListener? = null

    override fun start(activity: Activity, listener: TagListener?): Boolean {
        tagListener = listener
        try {
            nfcScanner = NFCScanner()
            nfcScanner!!.start(activity, object : TagListener {
                override fun onSuccess(rfid: String?, info: String?) {
                    tagListener?.onSuccess(rfid, info)
                }

                override fun onLogReceive(log: String?, action: String?) {
                    tagListener?.onLogReceive(log, action)
                }
            })
            iuhfService = UHFManager.getUHFService(activity)
            val success = iuhfService?.openDev() != 0
            iuhfService!!.setOnInventoryListener(object : OnSpdInventoryListener {

                override fun onInventoryStatus(status: Int) {
                }

                override fun getInventoryData(var1: SpdInventoryData?) {
                    val info = "rssi: ${var1?.rssi}, epc: ${var1?.epc}, tid: ${var1?.tid}"
                    Handler(Looper.getMainLooper()).post {
                        tagListener?.onSuccess(var1?.epc, info)
                    }
                }

            })
            iuhfService?.inventoryStart()
            return success
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    override fun stop() {
        iuhfService?.setOnReadListener(null)
        nfcScanner?.stop()
        tagListener = null

        try {
            iuhfService?.inventoryStop()
            iuhfService?.closeDev()
            iuhfService = null
            UHFManager.closeUHFService()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun release() {
    }

    override fun onResume(activity: Activity) {
        nfcScanner?.onResume(activity)
    }

    override fun onPause(activity: Activity) {
        nfcScanner?.onPause(activity)
    }

    override fun onNewIntent(intent: Intent) {
        nfcScanner?.onNewIntent(intent)
    }
}