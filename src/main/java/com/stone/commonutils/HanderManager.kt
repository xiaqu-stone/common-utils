package com.stone.commonutils

import android.os.Handler
import android.os.Looper

/**
 * Created By: sqq
 * Created Time: 9/18/18 6:30 PM.
 */
object HanderManager {

    private val mMainHandler: Handler = Handler(Looper.getMainLooper())

    fun getMainHandler(): Handler {
        return mMainHandler
    }

    fun postMainHandler(runnable: () -> Unit) {
        mMainHandler.post { runnable() }
    }
}