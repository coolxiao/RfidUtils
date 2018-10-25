package com.kingyun.som.rfid.scanner

import android.app.Activity
import android.content.Intent
import cn.pda.serialport.Tools
import com.android.hdhe.uhf.reader.UhfReader
import com.kingyun.som.rfid.RFIDScanner
import com.kingyun.som.rfid.TagListener
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

/**
 * company 重庆庆云石油工程技术有限责任公司
 * FileName UHF13Scanner
 * Package com.kingyun.som.rfid.scanner
 * Description ${DESCRIPTION}
 * author jiexiaoqiang
 * create 2018-10-24 10:27
 * version V1.0
 */
class UHF13Scanner:RFIDScanner {
  @Volatile private var start: Boolean = false
  private var reader: UhfReader? = null
  override fun start(activity: Activity, listener: TagListener?): Boolean {
    doAsync {
      reader = UhfReader.getInstance()
      reader?.setWorkArea(0)
      reader?.setOutputPower(0)
      start = true
      var epcList: List<ByteArray>? = null
      var accessPassword = Tools.HexString2Bytes("00000000")
      while (start){
        epcList = reader?.inventoryRealTime()
        if (epcList != null && !epcList.isEmpty()) {
          for (epc in epcList) {
            if (epc != null && epc.isNotEmpty()) {
              val epcStr = Tools.Bytes2HexString(epc,
                  epc.size)
              uiThread {
                listener?.onSuccess(epcStr, "")
              }
            }
          }
        }
        epcList = null
        try {
          Thread.sleep(30)
        } catch (e: InterruptedException) {
          e.printStackTrace()
        }
      }
    }
    return true
  }

  override fun stop() {
    start = false
    reader?.close()
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