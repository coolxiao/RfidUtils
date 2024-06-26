package com.kingyun.som.rfid.scanner

import android.app.Activity
import android.content.Intent
import com.kingyun.som.rfid.RFIDScanner
import com.kingyun.som.rfid.TagListener
import com.magicrf.uhfreaderlib.reader.Tools
import com.magicrf.uhfreaderlib.reader.A80STUhfReader
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class A80STScanner(val portPath: String) : RFIDScanner {

  private var reader: A80STUhfReader? = null
  @Volatile private var startFlag = false

  override fun start(activity: Activity, listener: TagListener?): Boolean {
    doAsync {
      startFlag = true
      A80STUhfReader.setPortPath(portPath)
      reader = A80STUhfReader.getInstance()
      reader?.setOutputPower(26)
      try {
        while (startFlag) {
          val epcList = reader?.inventoryRealTime() //实时盘存
          if (!epcList.isNullOrEmpty()) {
            //播放提示音
            for (epc in epcList) {
              if (epc != null) {
                val epcStr = Tools.Bytes2HexString(epc, epc.size)
                if (!epcStr.isNullOrEmpty()) {
                  uiThread {
                    listener?.onSuccess(epcStr, "")
                  }
                  startFlag = false
                }
              }
            }
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
    return true
  }

  override fun stop() {
    startFlag = false
    try {
      reader?.close()
    } catch (e: Exception) {
      e.printStackTrace()
    }
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