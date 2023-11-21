package com.kingyun.rfid

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.PermissionUtils.SimpleCallback
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.ThreadUtils.Task
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.ToastUtils
import com.kingyun.som.rfid.RFIDSwitcher
import com.kingyun.som.rfid.TagListener
import kotlinx.android.synthetic.main.activity_main.btnScan
import kotlinx.android.synthetic.main.activity_main.rvList

class MainActivity : AppCompatActivity() {
  private lateinit var adapter: RfidAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    btnScan.setOnClickListener {
      btnScan.text = "扫描中"
      btnScan.isEnabled = false
      RFIDSwitcher.instance.open(this, object : TagListener {
        override fun onSuccess(rfid: String?, info: String?) {
          btnScan.text = "扫描"
          btnScan.isEnabled = true
          RFIDSwitcher.instance.close()
          if (!rfid.isNullOrEmpty()) {
            adapter.addData(rfid)
            ThreadUtils.executeByIo(object : Task<Boolean>() {
              override fun doInBackground(): Boolean {
                return FileIOUtils.writeFileFromString(
                  Environment.getExternalStorageDirectory().absolutePath + "/" + TimeUtils.millis2String(
                    System.currentTimeMillis(), "yyyy-MM-dd"
                  ) + "rfid.txt",
                  rfid + "\n",
                  true
                )
              }

              override fun onCancel() {
              }

              override fun onFail(t: Throwable?) {
                ToastUtils.showShort("文件写入失败" + t?.message)
              }

              override fun onSuccess(result: Boolean?) {
              }
            })
          }
        }

        override fun onLogReceive(log: String?, action: String?) {
          btnScan.text = "扫描"
          btnScan.isEnabled = true
        }
      })
    }

    adapter = RfidAdapter()
    rvList.adapter = adapter


    PermissionUtils.permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
      .callback(object : SimpleCallback {
        override fun onGranted() {

        }

        override fun onDenied() {
          ToastUtils.showShort("未授予读写权限，请在设置中授予app读写权限")
        }
      })
      .request()
  }

  override fun onDestroy() {
    super.onDestroy()
    RFIDSwitcher.instance.close()
  }
}
