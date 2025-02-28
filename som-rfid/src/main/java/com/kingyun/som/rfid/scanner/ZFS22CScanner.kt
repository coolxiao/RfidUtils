package com.kingyun.som.rfid.scanner

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.widget.Toast
import com.jeason.rfidsdk.IRFIDScanResultListener
import com.jeason.rfidsdk.RFIDClientManager
import com.jeason.rfidsdk.RFIDClientManager.RFIDServerStateListener
import com.kingyun.som.rfid.RFIDScanner
import com.kingyun.som.rfid.TagListener
import kotlin.concurrent.thread

/**
 * Created by xifan on 17-11-2.
 */


class ZFS22CScanner(
) : RFIDScanner {
  private var tagListener: TagListener? = null
  override fun start(activity: Activity, listener: TagListener?): Boolean {
    tagListener = listener
    RFIDClientManager.getInstance().init(activity, object : RFIDServerStateListener {
      override fun onServiceConnected() {
        Toast.makeText(activity, "RFID服务已连接", Toast.LENGTH_SHORT).show()
        try {
          RFIDClientManager.getInstance().setRfidModule(RFIDClientManager.RFID_MODULE_S29C_915M)
          RFIDClientManager.getInstance().setScanResultListener(object : IRFIDScanResultListener.Stub(){
            override fun onTotalData(p0: String?) {
            }

            override fun onEffectData(p0: String?) {
              p0?.let {
                if (it.contains(",")){
                  val array = it.split(",")
                  if (array.isNotEmpty()) {
                      val rifd = array.first()
                      formatData(StringBuffer(rifd))
                  }
                } else {
                  formatData(StringBuffer(it))
                }
              }
            }
          })

        } catch (e: Exception) {
          e.printStackTrace()
        }
        RFIDClientManager.getInstance().powerOn()

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
          RFIDClientManager.getInstance().scanContinuous()
        }, 1000)
      }

      override fun onServiceDisconnected(s: String) {
        Toast.makeText(activity, "RFID服务断开", Toast.LENGTH_SHORT).show()
      }
    })
    return true
  }

  override fun stop() {
    RFIDClientManager.getInstance().powerOff()
    RFIDClientManager.getInstance().deInit()
  }

  override fun release() {
  }

  override fun onResume(activity: Activity) {
  }

  override fun onPause(activity: Activity) {
  }

  override fun onNewIntent(intent: Intent) {
  }

  fun formatData(sb: StringBuffer) {
    var result = sb.toString()
    if (!TextUtils.isEmpty(result)) {
      val handler = Handler(Looper.getMainLooper())
      thread {
        handler.post {
          if (result.length == 34) {
            RFIDClientManager.getInstance().stopScan()
            result = result.substring(6, 30)
            tagListener?.onSuccess(result, "")
          }
        }
      }
    }
  }
}