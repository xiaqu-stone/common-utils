package com.stone.commonutils

import android.app.Activity
import android.content.Context
import android.support.annotation.IdRes
import android.view.View

/**
 * Created By: sqq
 * Created Time: 8/23/18 3:47 PM.
 *
 * View相关的扩展函数
 */


/**
 * 重新定义View下的ctx对象，与anko-common库中关于Activity，Fragment等的ctx保持一致
 */
val View.ctx: Context
    get() = context

/**
 * 快速为 单个或者多个View 添加Click监听
 */
inline fun Activity.btnClick(@IdRes vararg ids: Int, crossinline callback: (v: View) -> Unit) {
    ids.forEach { id -> this.findViewById<View>(id).setOnClickListener { callback(it) } }
}

inline fun View.btnClick(@IdRes vararg ids: Int, crossinline callback: (v: View) -> Unit) {
    ids.forEach { id -> this.findViewById<View>(id).setOnClickListener { callback(it) } }
}


