package com.stone.commonutils

import android.app.Activity
import android.os.Build
import android.support.v7.app.AppCompatActivity

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

fun Activity?.getActivityTitle(): String {
    if (this == null) return ""
    var title = ""
    try {
        title = getToolbarTitle()
        if (title.isEmpty()) {
            if (packageManager != null) {
                val info = packageManager.getActivityInfo(componentName, 0)
                if (info != null) {
                    title = info.loadLabel(packageManager).toString()
                }
            }
        }
    } catch (e: Exception) {
    }
    return title
}

fun Activity?.getToolbarTitle(): String {
    if (this == null) return ""
    if (actionBar != null) {
        if (!actionBar?.title.isNullOrEmpty()) {
            return actionBar!!.title.toString()
        }
    } else {
        if (this is AppCompatActivity) {
            if (supportActionBar != null) {
                if (!supportActionBar?.title.isNullOrEmpty()) {
                    return supportActionBar!!.title.toString()
                }
            }
        }
    }
    return ""
}
