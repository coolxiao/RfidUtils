package com.kingyun.som.rfid.scanner

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.kingyun.som.rfid.BatteryReceiver
import com.kingyun.som.rfid.RFIDScanner
import com.kingyun.som.rfid.TagListener
import com.speedata.libuhf.IUHFService
import com.speedata.libuhf.UHFManager


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
            val success = if (iuhfService == null) {
                iuhfService = UHFManager.getUHFService(activity)
                iuhfService?.OpenDev() ?: -1 == 0
            } else {
                true
            }

            if (success) {
                iuhfService!!.setOnInventoryListener {
                    it.run {
                        val info = "rssi: $rssi, epc: $epc, tid: $tid"
                        Handler(Looper.getMainLooper()).post {
                            tagListener?.onSuccess(epc, info)
                        }
                    }
                }
                if (!BatteryReceiver.LOW_POWER_MODEL) {
                    iuhfService!!.newInventoryStart()
                }
            }
            return success
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    override fun stop() {
        try {
            iuhfService?.inventory_stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        iuhfService?.setOnReadListener(null)
        nfcScanner?.stop()
        tagListener = null
    }

    override fun release() {
        try {
            iuhfService?.CloseDev()
            iuhfService = null
            UHFManager.closeUHFService()
        } catch (e: Exception) {
            e.printStackTrace()
        }
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