package com.stone.commonutils

import android.os.Build
import com.stone.log.Logs
import org.jetbrains.anko.doAsync
import java.util.concurrent.TimeUnit

/**
 * Created By: sqq
 * Created Time: 2019/3/5 15:29.
 */
object DeviceUtil {
    /**
     * 是否模拟器
     */
    fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || "google_sdk" == Build.PRODUCT)
    }

    /**
     * @param callback 主线程中执行
     */
    fun isNetworkOnline(callback: (status: Boolean) -> Unit) {
        doAsync {
            try {
                val process = Runtime.getRuntime().exec("ping -c 3 www.baidu.com")
                val status = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    process.waitFor(3, TimeUnit.SECONDS)
                } else {
                    process.waitFor() == 0
                }
                HandlerManager.postMainHandler { callback(status) }
            } catch (e: Exception) {
                Logs.w("isNetworkOnline: ${e.message}")
                HandlerManager.postMainHandler { callback(false) }
            }
        }
    }

}