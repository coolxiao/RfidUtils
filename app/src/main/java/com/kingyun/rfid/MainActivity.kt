package com.kingyun.rfid

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.kingyun.som.rfid.RFIDSwitcher
import com.kingyun.som.rfid.TagListener
import kotlinx.android.synthetic.main.activity_main.rfid

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    rfid.setOnClickListener {
      RFIDSwitcher.instance.open(this, object :TagListener{
        override fun onSuccess(rfid: String?, info: String?) {
          Toast.makeText(this@MainActivity, rfid, Toast.LENGTH_SHORT).show()
        }
        override fun onLogReceive(log: String?, action: String?) {
        }
      })
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    RFIDSwitcher.instance.close()
  }
}
