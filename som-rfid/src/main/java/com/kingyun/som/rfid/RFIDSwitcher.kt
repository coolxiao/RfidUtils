package com.kingyun.som.rfid

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.util.Log
import com.kingyun.som.rfid.nfc.NfcHelper
import com.kingyun.som.rfid.scanner.*
import java.io.File

/**
 * Created by xifan on 17-11-1.
 */
class RFIDSwitcher private constructor() {
    private var rfidScanner: RFIDScanner? = null

    private fun initRFID(activity: Activity) {
        val model = Build.MODEL
        Log.e("rfid", "init rfid, model: $model")
        try {
            rfidScanner = when (model) {
                "RMPC-M01" -> M6eScanner()
                "XPad_05" -> HFScanner()
                "ax6753_66_sh_n" -> UHF13Scanner()
                "KT80", "T80", "SD100T", "SD100","ZKHD-HL-XN8QTA" -> CZScanner()
                "T71-V3" -> UHFScanner(File("/dev/ttyMT2"))
                "KY71" -> UHFScanner(File("/dev/ttyMT2"))
                "KY70", "T70HEX", "T70EX", "T70HEX2", "T70HEX-V3" -> STScanner()
                "U8" -> HXUHFScanner()
                "Q802+","Q802", "G82-EX" -> UHFQScanner()
                "conquest-S19" -> ZFScanner(S19_NODE, POWER_ON_STR_S19, POWER_OFF_STR_S19, "/dev/ttyS1")
                "conquest-S22" -> ZFScanner(S22_NODE, POWER_ON_STR_S22, POWER_OFF_STR_S22, "/dev/ttyS1")
                "H29" -> ZFScanner(S29_915M_NODE, POWER_ON_S29_915M, POWER_OFF_S29_915M, "/dev/ttyS0")
                "S22C" -> ZFS22CScanner()
                "EV8", "P8000", "EV8000", "W810" -> XYScanner("/dev/ttyUW0")
                "M5" -> UHFScanner(File("/dev/ttyMT1"))
                "M5-5G" -> M55GScanner("/dev/ttyS1")
                "P9000" -> P9000Scanner("/dev/ttyMT1")
                "A80ST" -> A80STScanner()
                "A2308" -> XYScanner("/dev/ttySWK0")
                "L800", "L800D" -> M5Scanner(("/dev/ttyXRM1"))
                "ZYSK-M100E" -> M100EScanner()
                else -> {
                    try {
                        UHFScanner(null)
                    } catch (e: Exception) {
                        //理论上:抛SecurityException表示没有权限,即不支持rfid
                        if (NfcHelper.getStatus(activity.application) > 0) {
                            NFCScanner()
                        } else null
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.e("rfid", "init rfid, scanner: ${rfidScanner?.javaClass?.name}")
    }

    fun open(activity: Activity, listener: TagListener?): Boolean {
        if (this.rfidScanner == null) {
            initRFID(activity)
        }
        return this.rfidScanner?.start(activity, listener) ?: false
    }

    fun onNewIntent(intent: Intent) {
        this.rfidScanner?.onNewIntent(intent)
    }

    fun onResume(activity: Activity) {
        this.rfidScanner?.onResume(activity)
    }

    fun onPause(activity: Activity) {
        this.rfidScanner?.onPause(activity)
    }

    fun close() {
        this.rfidScanner?.stop()
    }

    fun release() {
        this.rfidScanner?.release()
        this.rfidScanner = null
    }

    fun isNfc(): Boolean {
        return rfidScanner is NFCScanner
    }

    fun getScanner(): RFIDScanner? {
        return rfidScanner
    }

    companion object {
        @JvmStatic
        val instance: RFIDSwitcher = RFIDSwitcher()
    }
}