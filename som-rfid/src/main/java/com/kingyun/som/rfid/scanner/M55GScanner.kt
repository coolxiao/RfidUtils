package com.kingyun.som.rfid.scanner

import android.app.Activity
import android.content.Intent
import com.kingyun.som.rfid.RFIDScanner
import com.kingyun.som.rfid.TagListener
import com.magicrf.uhfreaderlib.reader.A80STUhfReader
import com.magicrf.uhfreaderlib.reader.Tools
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.FileWriter


class M55GScanner(val portPath: String) : RFIDScanner {
  val POWER_PHAT = "/sys/bus/platform/devices/infrared_pwr/infrared_setting"

  private var reader: A80STUhfReader? = null
  @Volatile private var startFlag = false

  override fun start(activity: Activity, listener: TagListener?): Boolean {
    //上电操作
    write(POWER_PHAT, "1")

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
      write(POWER_PHAT, "0")
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

  @Throws(java.lang.Exception::class)
  private fun write(path: String, value: String) {
    val fw = FileWriter(path)
    fw.write(value)
    fw.flush()
    fw.close()
  }
}