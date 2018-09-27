package com.stone.commonutils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build

/**
 * Created By: sqq
 * Created Time: 18/6/6 下午4:35.
 */
fun Context.getCompatColor(colorRes: Int): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        this.getColor(colorRes)
    } else {
        this.resources.getColor(colorRes)
    }
}


/**
 * 复制文本至剪切板
 */
fun Context.copyText(text: String) {
    val manager: ClipboardManager? = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    manager?.primaryClip = ClipData.newPlainText(null, text)
}


