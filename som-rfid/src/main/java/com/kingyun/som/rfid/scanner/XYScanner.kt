package com.kingyun.som.rfid.scanner

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.kingyun.som.rfid.RFIDScanner
import com.kingyun.som.rfid.TagListener
import uhf.AsyncSocketState
import uhf.MultiLableCallBack
import uhf.Reader
import uhf.Types
import kotlin.concurrent.thread

/**
 * Created by xifan on 17-11-2.
 */
class XYScanner(val portPath: String) : RFIDScanner, MultiLableCallBack {
    var _clients: List<AsyncSocketState>? = null
    var clientstate: AsyncSocketState? = null
    var ReaderController: Reader? = null
    var mlistener: TagListener? = null;
    var deal = false

    override fun start(activity: Activity, listener: TagListener?): Boolean {
        deal = false
        ReaderController = Reader(this)
        mlistener = listener;
        ReaderController?.OpenSerialPort_Android(portPath)
        _clients = ReaderController?.GetClientInfo()
        clientstate = _clients?.get(0)
        ReaderController?.StartMultiEPC(clientstate)
        return true
    }

    override fun method(RecvStr: String?) {
        if (deal) {
            return
        }
        val result = ("$RecvStr,0").split(",")
        val cmdType = toBytes(result[1])[0]
        when (cmdType) {
            Types.START_MULTI_EPC_RESPOND,
            Types.START_SINGLE_EPC_RESPOND,
            Types.READ_SENSOR_TAGS_RESPOND -> {
                if (result[2] == "1") {
                    val rfid = result[5].replace("-", "");
                    if (rfid.isNotEmpty()) {
                        deal = true
                        ReaderController?.StopMultiEPC(clientstate)
                        val handler = Handler(Looper.getMainLooper())
                        thread {
                            handler.post {
                                mlistener?.onSuccess(rfid, "")
                            }
                        }
                    } else {
                        deal = false
                    }
                } else {
                    deal = false
                }
            }
        }
    }

    override fun ReaderNotice(p0: String?) {

    }

    private fun toBytes(str: String?): ByteArray {
        if (str == null || str.trim { it <= ' ' } == "") {
            return ByteArray(0)
        }
        val bytes = ByteArray(str.length / 2)
        for (i in 0 until str.length / 2) {
            val subStr = str.substring(i * 2, i * 2 + 2)
            bytes[i] = subStr.toInt(16).toByte()
        }
        return bytes
    }

    override fun stop() {
        ReaderController?.StopMultiEPC(clientstate)
        ReaderController?.CloseSerialPort_Android()
        ReaderController?.ServerClose()
        ReaderController = null
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