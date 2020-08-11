package com.kingyun.som.rfid.scanner

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.kingyun.som.rfid.RFIDScanner
import com.kingyun.som.rfid.TagListener
import com.soten.libs.base.MessageResult
import com.soten.libs.uhf.UHFManager
import com.soten.libs.uhf.UHFResult
import com.soten.libs.uhf.base.ResultBundle
import com.soten.libs.uhf.impl.UHF
import com.soten.libs.uhf.impl.UHFModelListener
import com.soten.libs.utils.PowerManagerUtils
import java.lang.ref.WeakReference
import java.util.*

/**
 * company 重庆庆云石油工程技术有限责任公司
 * FileName STScanner
 * Package com.kingyun.som.rfid.scanner
 * Description
 * author jiexiaoqiang
 * create 2020-08-11 10:18
 * version V1.0
 */
class STScanner : RFIDScanner, UHFModelListener {
    private var mUHFManager: UHFManager = UHFManager.getInstance()
    private var mUhf: UHF? = null
    private var mListener: TagListener? = null
    private var mActivity: WeakReference<Activity>? = null
    private var mTimer: Timer? = null

    init {
        mUhf = mUHFManager.uhf
    }

    override fun start(activity: Activity, listener: TagListener?): Boolean {
        val pm = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
        PowerManagerUtils.open(pm, 0x0C)

        mUHFManager.open(activity)
        mUHFManager.register(this)

        if (!mUHFManager.isOpen) {
            return false
        }

        mListener = listener
        mActivity = WeakReference<Activity>(activity)

        if (mTimer == null) {
            mTimer = Timer()
        }
        mTimer?.schedule(object : TimerTask() {
            override fun run() {
                mUhf?.realTimeInventory()
            }
        }, 0, 100)
        return true
    }

    override fun stop() {
        mUHFManager.unregister(this)
        mUHFManager.close(mUhf, mActivity?.get())
        mActivity?.clear()
    }

    override fun release() {
    }

    override fun onResume(activity: Activity) {
    }

    override fun onPause(activity: Activity) {
    }

    override fun onNewIntent(intent: Intent) {
    }

    override fun onLostConnect(p0: Exception?) {
    }

    override fun onReceice(p0: MessageResult?) {
        val uhfResult = p0 as UHFResult
        val bundle = uhfResult.bundle
        val EPC = bundle.getString(ResultBundle.EPC)
        if (!EPC.isNullOrEmpty()) {
            mTimer?.cancel()
            mTimer = null
            mActivity?.get()?.runOnUiThread {
                mListener?.onSuccess(EPC.replace(" ",""), "EPC")
            }
        }
    }
}