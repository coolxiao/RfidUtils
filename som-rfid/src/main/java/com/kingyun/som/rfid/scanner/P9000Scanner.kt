package com.kingyun.som.rfid.scanner

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.kingyun.som.rfid.RFIDScanner
import com.kingyun.som.rfid.TagListener
import com.magicrf.uhfreaderlib.reader.A80STUhfReader
import com.magicrf.uhfreaderlib.reader.Tools
import kotlin.concurrent.thread


class P9000Scanner(val portPath: String) : RFIDScanner {

  private var reader: A80STUhfReader? = null
  @Volatile private var startFlag = false

  override fun start(activity: Activity, listener: TagListener?): Boolean {
    //上电操作
    val intent = Intent("android.intent.action.OPEN_RFID")
    intent.setPackage("com.android.settings")
    activity.sendBroadcast(intent)

    val handler = Handler(Looper.getMainLooper())

    thread {
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