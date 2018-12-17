package com.stone.commonutils

import android.os.Handler
import android.os.Looper

/**
 * Created By: sqq
 * Created Time: 9/18/18 6:30 PM.
 */
object HandlerManager {

    private val mMainHandler: Handler = Handler(Looper.getMainLooper())

    fun getMainHandler(): Handler {
        return mMainHandler
    }

    fun postMainHandler(delayMillis: Long = 0L, runnable: () -> Unit) {
        mMainHandler.postDelayed({ runnable() }, delayMillis)
    }
}