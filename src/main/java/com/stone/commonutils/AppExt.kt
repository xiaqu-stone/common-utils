package com.stone.commonutils

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.provider.Settings
import android.support.annotation.RequiresPermission
import android.telephony.TelephonyManager
import com.stone.log.Logs
import org.jetbrains.anko.ctx

/**
 * Created By: sqq
 * Created Time: 8/28/18 3:24 PM.
 *
 * 设备 or 应用级别的工具类
 */

/**
 * 程序是否在前台运行
 */
fun Context.isAppOnForeground(): Boolean {
    val activityManager = this
        .getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    val packageName = this.packageName

    val appProcesses = activityManager
        ?.runningAppProcesses ?: return false
    for (appProcess in appProcesses) {
        if (appProcess.processName == packageName && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) return true
    }
    return false
}

/**
 * 依赖TelephonyManager，即需要通话硬件功能；手机之外的一些Android设备，可能会没有通话的硬件功能，那么TelephonyManager也就不存在，导致无法正常获取到IMEI
 */
@RequiresPermission(Manifest.permission.READ_PHONE_STATE)
fun Context.getIMEI(): String {
    val manager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    try {
        val imei: String? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.imei
        } else {
            @Suppress("DEPRECATION")
            manager.deviceId
        }
        Logs.i(TAG, "getIMEI: the imei is : $imei")
        return imei ?: ""
    } catch (e: Exception) {
        Logs.w("getIMEI: ${e.message}")
    }
    return ""
}

/**
 * 获取 AndroidId，9774d56d682e549c 为模拟器的常见 androidId
 */
fun Context.getAndroidId(): String {
    return Settings.System.getString(contentResolver, Settings.Secure.ANDROID_ID)
}


/**
 * @param name the name of meta-data
 * @return get the value of meta-data
 */
fun Context.getMetaDataValue(name: String): String {
    var value: String? = null
    val applicationInfo: ApplicationInfo?
    try {
        applicationInfo = this.packageManager.getApplicationInfo(
            this
                .packageName, PackageManager.GET_META_DATA
        )
        if (applicationInfo?.metaData != null) {
            value = applicationInfo.metaData.getString(name)
        }
    } catch (e: PackageManager.NameNotFoundException) {
        throw RuntimeException("Could not read the name in the manifest file.", e)
    }
    if (value == null) {
        throw RuntimeException(
            "The name '" + name
                    + "' is not defined in the manifest file's meta data."
        )
    }
    return value
}

/**
 * 判断应用是否处于启动状态
 * @return Boolean true：进程活着并且应用的任务栈不为空； false：进程死亡或者应用的任务栈为空
 */
@RequiresPermission(Manifest.permission.GET_TASKS)
fun Context.isAppAlive(): Boolean {
    val activityManager = ctx.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val runningApps = activityManager.runningAppProcesses ?: return false
    for (i in runningApps.indices) {
        Logs.d("isAppAlive: i = $i , processName = ${runningApps[i].processName}")
        if (runningApps[i].processName == this.packageName) { //此处processName进程名以applicationId命名，与包路径的包名无关
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val appTasks = activityManager.appTasks
                for (task in appTasks) {
                    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        //直接获取的当前进程的活动栈，无需判断包名
                        Logs.i("isAppAlive: the ${this.packageName} is running, isAppAlive return true. the num of activities is ${task.taskInfo.numActivities}")
                        // Note：9/4/18 API 23才加入此属性，故需要在此处再做一层判断
                        task.taskInfo.numActivities > 0
                    } else {
                        // Note：9/4/18 获取当前APP任务栈的id，当任务栈不处于运行状态时，id == -1
                        task.taskInfo.id != -1
                    }
                }
            } else {
                // Note：9/4/18 从API 21 此方法已废弃，仅在debug下可以获取信息,在API21-，需要声明 GET_TASKS 权限
                // 注释中有说明，为了向后兼容，API 21 + 仅可获取到当前APP的信息
                val runningTasks = activityManager.getRunningTasks(10)
                for (taskInfo in runningTasks) {
                    //task.taskInfo.baseActivity.packageName 与 applicaitonId等同
                    //task.taskInfo.baseActivity.className 全限定了类名，前缀不一定是applicationId
                    //获取当前手机的所有活动栈，需要判断当前应用的包名
                    if (taskInfo.baseActivity.packageName == this.packageName && taskInfo.numActivities > 0) {
                        Logs.i("isAppAlive: the ${this.packageName} is running, isAppAlive return true. the num of activities is ${taskInfo.numActivities}")
                        return true
                    }
                }
            }

        }
    }
    Logs.i("isAppAlive: the ${this.packageName} is not running, isAppAlive return false.")
    return false
}

/**
 * 获取当前进程名称
 */
fun Context.getProcessName(): String {
    val am = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val runningApps = am.runningAppProcesses ?: return ""
    for (procInfo in runningApps) {
        if (procInfo.pid == android.os.Process.myPid()) {
            return procInfo.processName
        }
    }
    return ""
}

/**
 * 通过包名与应用安装列表，检查微信客户端是否安装
 */
fun Context.isWeChatAvailable(): Boolean {
    val info = this.packageManager.getInstalledPackages(0)// 获取所有已安装程序的包信息
    if (info != null) {
        for (i in info.indices) {
            val pn = info[i].packageName
            if (pn == "com.tencent.mm") {
                return true
            }
        }
    }
    return false
}

/**
 * 当前网络连接是否可用
 */
@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
fun Context?.isNetworkAvailable(): Boolean {
    if (this != null) {
        val manager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (manager?.activeNetworkInfo?.isAvailable == true) return true
    }
    return false
}