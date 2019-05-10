package com.stone.commonutils

import android.graphics.Bitmap
import android.util.Log
import com.stone.log.Logs
import org.jetbrains.anko.doAsync
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created By: sqq
 * Created Time: 8/22/18 3:34 PM.
 *
 * File IO 操作工具类
 */

const val TAG = "SPExt.kt"

/**
 * save Bitmap to the dest file
 *
 * Note：9/19/18 by sqq
 * 该方法未做线程操作，使用时请注意要切换到 子Thread 中调用
 */
@JvmOverloads
fun Bitmap.saveTo(dest: File, quality: Int = 100) {
    checkFile(dest)
    try {
        FileOutputStream(dest).use {
            if (this.compress(Bitmap.CompressFormat.PNG, quality, it)) it.flush()
        }
    } catch (e: IOException) {
        Log.w(TAG, "saveTo: ${e.message}")
    }
}

/**
 * async save Bitmap to the dest file
 *
 * @param onIOFinished run on UI Thread , 异步保存结束后的回调; result: true IO操作成功；false IO操作异常
 */
@JvmOverloads
fun Bitmap.saveAsyncTo(dest: File, quality: Int = 100, onIOFinished: ((result: Boolean) -> Unit)? = null) {
    checkFile(dest)
    try {
        doAsync {
            FileOutputStream(dest).use {
                if (this@saveAsyncTo.compress(Bitmap.CompressFormat.PNG, quality, it)) it.flush()
            }
            Logs.d(".saveAsyncTo() called with: ")
            if (onIOFinished != null) HandlerManager.postMainHandler { onIOFinished.invoke(true) }
        }
    } catch (e: IOException) {
        Log.w(TAG, "saveTo: ${e.message}")
        if (onIOFinished != null) HandlerManager.postMainHandler { onIOFinished.invoke(false) }
    }
}

/**
 * ByteArray to file
 */
fun ByteArray.toFile(dest: File): File {
    try {
        BufferedOutputStream(FileOutputStream(dest)).use {
            it.write(this)
            it.flush()
        }
    } catch (e: Exception) {
        Log.w(TAG, "toFile: ${e.message}")
    }
    return dest
}

/**
 * @param file : this file is the file, not the directory
 */
private fun checkFile(file: File) {
    if (!file.parentFile.exists()) {
        file.parentFile.mkdirs()
    }
    if (file.exists() && file.isDirectory) {
        file.delete()
    }
}

/**
 * 获取File 的占用空间大小
 *
 * 若是 单个文件 则直接返回大小
 * 若是 目录 则进行递归遍历，返回目录中全部文件的大小
 */
fun File?.getFileSize(): Long {
    if (this == null || !this.exists()) return 0L
    return if (this.isDirectory) {
        var size = 0L
        this.listFiles().forEach {
            size += it.getFileSize()
        }
        return size
    } else {
        this.length()
    }
}
//
//fun File?.deleteFile(){
//
//}
