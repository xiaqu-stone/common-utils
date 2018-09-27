package com.stone.commonutils

import android.content.Context
import android.util.DisplayMetrics
import android.view.WindowManager

/**
 * Created By: sqq
 * Created Time: 8/22/18 4:24 PM.
 *
 * 转换界面尺寸的工具类
 */


fun Context.dp2px(dp: Float): Int {
    val scale = this.resources.displayMetrics.density
    return (dp * scale + 0.5f * if (dp >= 0) 1 else -1).toInt()
}

fun Context.px2dp(px: Float): Int {
    val scale = this.resources.displayMetrics.density
    return (px / scale + 0.5f * if (px >= 0) 1 else -1).toInt()
}

/**
 * 单位 px，屏幕的宽高
 */
fun Context.getScreenWidth(): Int {
    return getDisplayMetrics().widthPixels
}

fun Context.getScreenHeight(): Int {
    return getDisplayMetrics().heightPixels
}

/**
 * 获取手机屏幕参数
 */
fun Context.getDisplayMetrics(): DisplayMetrics {
    val wm = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val outMetrics = DisplayMetrics()
    wm.defaultDisplay.getMetrics(outMetrics)
    return outMetrics
}

/**
 * 获取状态栏高度
 */
fun Context.getStatusBarHeight(): Int {
    val resId = this.resources.getIdentifier("status_bar_height", "dimen", "android")
    return if (resId > 0) this.resources.getDimensionPixelSize(resId) else 0
}