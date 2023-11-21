package com.kingyun.rfid

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

/**
 * company 重庆庆云石油工程技术有限责任公司
 * FileName RfidAdapter
 * Package com.kingyun.rfid
 * Description
 * author coolxiao
 * create 2023-11-21 10:17
 * version V1.0
 */
class RfidAdapter() : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_rfid, null) {
  override fun convert(helper: BaseViewHolder, item: String) {
    helper.setText(R.id.tvRfid, item)
  }
}