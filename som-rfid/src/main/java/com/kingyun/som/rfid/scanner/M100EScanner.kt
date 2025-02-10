package com.kingyun.som.rfid.scanner

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.kingyun.som.rfid.RFIDScanner
import com.kingyun.som.rfid.TagListener
import cus.gg.reader.api.dal.GClient
import cus.gg.reader.api.dal.HandlerTagEpcLog
import cus.gg.reader.api.dal.communication.AndroidUsbHidClient
import cus.gg.reader.api.dal.communication.OnUsbHidDeviceListener
import cus.gg.reader.api.protocol.gx.EnumG
import cus.gg.reader.api.protocol.gx.MsgBaseInventoryEpc
import cus.gg.reader.api.protocol.gx.MsgBaseStop
import kotlin.concurrent.thread

/**
 * company 重庆庆云石油工程技术有限责任公司
 * FileName M100EScanner
 * Package com.kingyun.som.rfid.scanner
 * Description
 * author coolxiao
 * create 2025-02-10 14:15
 * version V1.0
 */
class M100EScanner : RFIDScanner {
  private var gClient: GClient = GClient()

  override fun start(activity: Activity, listener: TagListener?): Boolean {
    val handler = Handler(Looper.getMainLooper())
    val hidEnumerate = AndroidUsbHidClient.enumerate(activity)
    if (hidEnumerate.isEmpty()) {
      handler.post {
        Toast.makeText(activity, "未找到设备", Toast.LENGTH_SHORT).show()
      }
      return false
    }

    val hidClient = hidEnumerate[hidEnumerate.keys.first()]
    if (hidClient == null) {
      handler.post {
        Toast.makeText(activity, "未找到设备", Toast.LENGTH_SHORT).show()
      }
      return false
    }
    hidClient.deviceListener = object : OnUsbHidDeviceListener {
      override fun onDeviceConnected(p0: AndroidUsbHidClient?) {
        handler.post {
          Toast.makeText(activity, "连接成功", Toast.LENGTH_SHORT).show()
        }

        val msg = MsgBaseInventoryEpc();
        msg.antennaEnable = 1
        msg.inventoryMode = EnumG.InventoryMode_Inventory
        thread {
          gClient.sendSynMsg(msg)
        }
      }

      override fun onDeviceConnectFailed(p0: AndroidUsbHidClient?) {
        handler.post {
          Toast.makeText(activity, "连接失败", Toast.LENGTH_SHORT).show()
        }
      }
    }
    gClient.openAndroidUsbHid(hidClient)


    gClient.onTagEpcLog = HandlerTagEpcLog { p0, p1 ->
      val msgStop = MsgBaseStop()
      gClient.sendSynMsg(msgStop)
      handler.post {
        listener?.onSuccess(p1?.epc, "")
      }
    }
    return true
  }

  override fun stop() {
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