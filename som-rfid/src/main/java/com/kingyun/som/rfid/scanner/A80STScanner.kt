package com.kingyun.som.rfid.scanner

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.kingyun.som.rfid.RFIDScanner
import com.kingyun.som.rfid.TagListener
import com.magicrf.uhfreaderlib.reader.Tools
import com.magicrf.uhfreaderlib.reader.A80STUhfReader
import kotlin.concurrent.thread


class A80STScanner() : RFIDScanner {

  private var reader: A80STUhfReader? = null
  @Volatile private var startFlag = false

  override fun start(activity: Activity, listener: TagListener?): Boolean {
    val handler = Handler(Looper.getMainLooper())
    thread {
      try {
        var portPath = "/dev/ttySWK0"
        val display = Build.DISPLAY
        display?.apply {
          val time = split("-").last().toInt()
          //2024年后的，使用/dev/ttySWK1
          if (time > 20240000) {
            portPath = "/dev/ttySWK1"
          }
        }

        startFlag = true
        A80STUhfReader.setPortPath(portPath)
        reader = A80STUhfReader.getInstance()
        reader?.setOutputPower(26)

        while (startFlag) {
          val epcList = reader?.inventoryRealTime() //实时盘存
          if (!epcList.isNullOrEmpty()) {
            //播放提示音
            for (epc in epcList) {
              if (epc != null) {
                val epcStr = Tools.Bytes2HexString(epc, epc.size)
                if (!epcStr.isNullOrEmpty()) {
                  handler.post {
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