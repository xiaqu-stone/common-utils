package com.stone.commonutils

import android.widget.EditText

/**
 * Created By: sqq
 * Created Time: 8/31/18 7:25 PM.
 *
 * EditText工具类
 */

/**
 * EditText 定位光标位置到最后
 */
fun EditText.setCursorToLast() {
    if (!this.text.isNullOrEmpty()) {
        this.setSelection(this.text.length)
    }
}

/**
 * editText 获取焦点
 */
fun EditText.getFocus() {
    this.isFocusable = true
    this.isFocusableInTouchMode = true
    this.requestFocus()
    this.findFocus()
}