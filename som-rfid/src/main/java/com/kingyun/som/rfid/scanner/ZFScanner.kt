package com.kingyun.som.rfid.scanner

import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import com.kingyun.som.rfid.RFIDScanner
import com.kingyun.som.rfid.TagListener
import com.kingyun.som.rfid.zf.FileWriter
import com.kingyun.som.rfid.zf.SerialCallBack
import com.kingyun.som.rfid.zf.SerialPortCallBackUtils
import com.kingyun.som.rfid.zf.SerialPortUtil
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.Timer
import kotlin.concurrent.schedule

/**
 * Created by xifan on 17-11-2.
 */

const val POWER_ON_STR_S19 = "rfiden 3"
const val POWER_OFF_STR_S19 = "rfiden 0"

const val POWER_ON_STR_S22 = "uart1 1"
const val POWER_OFF_STR_S22 = "uart1 0"

class ZFScanner(var powerOn: String, var powerOff: String) : RFIDScanner, SerialCallBack {
  private var tagListener: TagListener? = null
  override fun start(activity: Activity, listener: TagListener?): Boolean {
    SerialPortCallBackUtils.setCallBack(this)
    tagListener = listener
    FileWriter.writeFile(powerOn)
    val open = SerialPortUtil.open("/dev/ttyS1", 115200, 0)
    Timer().schedule(1000) {
      SerialPortUtil.sendString("0xBB00270003222710837E")
    }
    return open
  }

  override fun stop() {
    SerialPortCallBackUtils.setCallBack(null)
    SerialPortUtil.sendString("0xBB00280000287E")
    FileWriter.writeFile(powerOff)
    SerialPortUtil.closeCom()
  }

  override fun release() {
  }

  override fun onResume(activity: Activity) {
  }

  override fun onPause(activity: Activity) {
  }

  override fun onNewIntent(intent: Intent) {
  }

  override fun onSerialPortData(sb: StringBuffer) {
    var result = ""
    if ("22" == sb.subSequence(4, 6)) { //获取卡片信息
      if (sb.indexOf("7E") < sb.length - 2) {
        val array = sb.toString().split("7E")
        if (array.size > 1) {
          var first_str = array[0]
          if (first_str.length > 2) {
            if ("BB" == first_str.subSequence(0, 2)) {
              first_str = first_str.substring(10, first_str.length - 2)
            }
            result = first_str
          }
        }
      } else {
        if ("BB" == sb.subSequence(0, 2)) {
          sb.delete(0, 10)
        }
        val sb1 = sb.delete(sb.length - 4, sb.length)
        result = sb1.toString()
      }
    }
    if (!TextUtils.isEmpty(result)) {
      doAsync {
        uiThread {
          if (result.length == 34) {
            result = result.substring(6, 30)
          }
          tagListener?.onSuccess(result, "")
        }
      }
    }
  }
}