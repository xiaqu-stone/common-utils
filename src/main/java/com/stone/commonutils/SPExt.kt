package com.stone.commonutils

import android.content.Context
import android.content.SharedPreferences

/**
 * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
 */
fun Context.getSP(key: String, defaultValue: Any): Any {
    return SPUtils.get(this, key, defaultValue)
}

/**
 * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
 */
fun Context.putSP(key: String, value: Any?) {
    SPUtils.put(this, key, value)
}


fun Context.removeSP(key: String) {
    SPUtils.remove(this, key)
}

fun Context.clearSP() {
    SPUtils.clear(this)
}

fun Context.getAllSP(): Map<String, *> {
    return SPUtils.getAll(this)
}

fun Context.containsSP(key: String): Boolean {
    return SPUtils.contains(this, key)
}

// 更新时间：8/22/18 1:52 PM
/*为了方便外部扩展函数，开放下述两个方法*/

fun Context.getSPEditor(fileName: String = SPUtils.getFileName()): SharedPreferences.Editor {
    return SPUtils.getEditor(this, fileName)
}

fun Context.applySP(editor: SharedPreferences.Editor) {
    SPUtils.apply(editor)
}

