package com.stone.commonutils

import android.app.Activity
import android.os.Build

/**
 * Created By: sqq
 * Created Time: 8/22/18 4:50 PM.
 *
 *
 */



/**
 * 验证当前Activity是否有效可用
 * @return true: 可用
 */
fun Activity?.isValid(): Boolean {
    return if (this != null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            !isDestroyed
        } else {
            !isFinishing
        }
    } else false
}
