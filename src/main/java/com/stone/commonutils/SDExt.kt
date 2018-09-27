package com.stone.commonutils

import android.os.Environment

/**
 * Created By: sqq
 * Created Time: 9/17/18 12:09 PM.
 *
 * 关于外部存储的操作
 */

fun canUseSD(): Boolean {
    return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
}

fun builExternalDirPatn(vararg components:String){

}