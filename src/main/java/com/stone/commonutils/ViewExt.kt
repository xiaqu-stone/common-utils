package com.stone.commonutils

import android.app.Activity
import android.content.Context
import android.support.annotation.IdRes
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup

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


/**
 * 操作符扩展 使得ViewGroup获取子View时具有数组的操作性
 */
operator fun ViewGroup.get(position: Int): View = getChildAt(position)

/**
 * 扩展TextView的文案监听器，使得不需要每次都去实现三个方法
 */
interface BaseTextWatcher : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {}
}