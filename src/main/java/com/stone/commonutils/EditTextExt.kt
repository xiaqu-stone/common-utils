package com.stone.commonutils

import android.text.InputFilter
import android.widget.EditText
import android.widget.TextView

/**
 * Created By: sqq
 * Created Time: 8/31/18 7:25 PM.
 *
 * EditText / TextView 工具类
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
fun TextView.getFocus() {
    this.isFocusable = true
    this.isFocusableInTouchMode = true
    this.requestFocus()
    this.findFocus()
}

fun TextView.setMaxLength(maxLength:Int){
    filters = arrayOf<InputFilter>(InputFilter.LengthFilter(maxLength))
}

