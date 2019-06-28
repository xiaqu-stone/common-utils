package com.stone.commonutils

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Process
import android.os.Vibrator
import android.provider.Settings
import android.support.annotation.RequiresPermission
import android.telephony.TelephonyManager
import com.stone.log.Logs
import org.jetbrains.anko.ctx
import java.io.*
import java.lang.reflect.InvocationTargetException
import java.net.NetworkInterface
import java.net.SocketException
import java.security.MessageDigest
import java.security.cert.CertificateFactory


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
fun Context.getProcessNameQ(): String {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        // added in API level 28
        return Application.getProcessName()
    } else {
        val am = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningApps = am.runningAppProcesses ?: return ""
        for (procInfo in runningApps) {
            if (procInfo.pid == android.os.Process.myPid()) {
                return procInfo.processName
            }
        }

        return try {
            val file = File("/proc/" + Process.myPid() + "/" + "cmdline")
            val mBufferedReader = BufferedReader(FileReader(file))
            val processName = mBufferedReader.readLine().trim()
            mBufferedReader.close()
            processName
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
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

@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
fun Context.isNetworkAvailable2(): Boolean {
    val conn = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val capabilities = conn.getNetworkCapabilities(conn.activeNetwork)
        capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    } else {
        isNetworkAvailable()
    }
}


@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
fun Context.getNetworkState(): String {
    val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    var state: NetworkInfo.State
    var networkInfo: NetworkInfo?
    networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
    if (networkInfo != null) {
        state = networkInfo.state
        if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
            return "wifi"
        }
    }

    networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
    if (networkInfo != null) {
        state = networkInfo.state
        if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
            return getNetworkType(networkInfo)
        }
    }
    return ""
}

/**
 * 获取网络类型
 *
 * @param networkInfo
 * @return
 */
fun Context.getNetworkType(networkInfo: NetworkInfo): String {
    val strNetworkType: String//= null;
    val strSubTypeName = networkInfo.subtypeName
    val networkType = networkInfo.subtype
    when (networkType) {
        TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN //api<8 : replace by 11
        -> strNetworkType = "2G"
        TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EVDO_B //api<9 : replace by 14
            , TelephonyManager.NETWORK_TYPE_EHRPD  //api<11 : replace by 12
            , TelephonyManager.NETWORK_TYPE_HSPAP  //api<13 : replace by 15
        -> strNetworkType = "3G"
        TelephonyManager.NETWORK_TYPE_LTE    //api<11 : replace by 13
        -> strNetworkType = "4G"
        else ->
            // http://baike.baidu.com/item/TD-SCDMA 中国移动 联通 电信 三种3G制式
            strNetworkType = if (strSubTypeName.equals("TD-SCDMA", ignoreCase = true) || strSubTypeName.equals(
                            "WCDMA",
                            ignoreCase = true
                    ) || strSubTypeName.equals("CDMA2000", ignoreCase = true)
            ) {
                "3G"
            } else {
                strSubTypeName
            }
    }
    return strNetworkType
}


@SuppressLint("HardwareIds", "PrivateApi")
fun Context.getMacAddress(): String {
    val wifiManager = this.getSystemService(Context.WIFI_SERVICE) as WifiManager
    var strMacAddress: String?
    strMacAddress = wifiManager.connectionInfo.macAddress ?: ""
    if (!validMacAddress(strMacAddress)) {
        var wlan0 = "wlan0"
        try {
            val systemPro = Class.forName("android.os.SystemProperties")
            val get = systemPro.getMethod("get", String::class.java, String::class.java)
            wlan0 = get.invoke(null, "wifi.interface", "wlan0").toString()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        try {
            val address = NetworkInterface.getByName(wlan0).hardwareAddress
            val builder = StringBuilder()
            for (b in address) {
                builder.append(String.format("%02X:", b))
            }
            if (builder.isNotEmpty()) {
                builder.deleteCharAt(builder.length - 1)
            }
            strMacAddress = builder.toString()
        } catch (e: SocketException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        if (!validMacAddress(strMacAddress)) {
            try {
                val pp = Runtime.getRuntime().exec("cat /sys/class/net/$wlan0/address")
                val ir = InputStreamReader(pp.inputStream)
                val input = LineNumberReader(ir)
                input.use { r ->
                    r.lineSequence().forEach {
                        strMacAddress += it.trim { it1 ->
                            it1 <= ' '
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    return strMacAddress ?: ""
}

private fun validMacAddress(mac: String?): Boolean {
    return !(mac.isNullOrEmpty() || "02:00:00:00:00:00" == mac)
}

/**
 * 注册摇一摇监听
 *
 * @return 将实际注册进入传感器中的监听对象返回，以便调用者寻找合适的时机解注册unregister
 */
@RequiresPermission(Manifest.permission.VIBRATE)
fun Context.registerShakeListener(onShakeListener: () -> Unit): SensorEventListener? {
    val manager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager ?: return null
    val mSensorListener = object : SensorEventListener {
        private val SPEED_THRESHOLD = 2000
        // 两次检测的时间间隔
        private val UPDATE_INTERVAL_TIME = 100
        private var lastX: Float = 0f
        private var lastY: Float = 0f
        private var lastZ: Float = 0f
        private var lastUpdateTime: Long = 0
        private var lastInvokeTime: Long = 0

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        @SuppressLint("MissingPermission")
        override fun onSensorChanged(event: SensorEvent?) {
            event ?: return
            val currentUpdateTime = System.currentTimeMillis()// 两次检测的时间间隔
            val timeInterval = currentUpdateTime - lastUpdateTime// 判断是否达到了检测时间间隔
            if (timeInterval < UPDATE_INTERVAL_TIME) return

            // 现在的时间变成last时间
            lastUpdateTime = currentUpdateTime// 获得x,y,z坐标
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]// 获得x,y,z的变化值
            val deltaX = x - lastX
            val deltaY = y - lastY
            val deltaZ = z - lastZ// 将现在的坐标变成last坐标
            lastX = x
            lastY = y
            lastZ = z
//            Logs.d("onSensorChanged: $deltaX, $deltaY, $deltaZ")
            val speed = Math.sqrt((deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ).toDouble()) / timeInterval * 10000
//            Logs.i("onSensorChanged: $speed")
            // 达到速度阀值，发出提示
            if (speed >= SPEED_THRESHOLD && currentUpdateTime - lastInvokeTime > 2000) {//至少间隔两秒才允许下次触发
                lastInvokeTime = System.currentTimeMillis()
                (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(200)
                onShakeListener()
            }
        }
    }
    manager.registerListener(mSensorListener, manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)
    return mSensorListener
}

fun Context.unregisterShakeListener(listener: SensorEventListener?) {
    listener ?: return
    val manager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager ?: return
    manager.unregisterListener(listener)
}

/**
 * 判断当前是否处于通话状态（包括：通话中，响铃中）
 */
fun Context.isTelephonyCalling(): Boolean {
    val tm = (getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager) ?: return false
    return tm.callState == TelephonyManager.CALL_STATE_RINGING || tm.callState == TelephonyManager.CALL_STATE_OFFHOOK
}

/**
 * 获取SHA1签名信息
 */
fun Context.getSHA1Fingerprint(packageName: String): String {
    val pm = this.packageManager
    var result = ""
    try {
        var cert: ByteArray? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val info = pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES).signingInfo
            cert = if (info.hasMultipleSigners()) {
                info.apkContentsSigners[0].toByteArray()
            } else {
                info.signingCertificateHistory[0].toByteArray()
            }
        } else {
            val info = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            if (info.signatures.isNotEmpty()) {
                cert = info.signatures[0].toByteArray()
            }
        }
        cert ?: return result

        val cf = CertificateFactory.getInstance("X509")
        val certificate = cf.generateCertificate(ByteArrayInputStream(cert))
        val digest = MessageDigest.getInstance("SHA-1")
        val publicKey = digest.digest(certificate.encoded)
        result = publicKey.toHex()

    } catch (e: Exception) {
        result = ""
    }
    return result
}

///**
// * 获取当前手机的应用安装列表(package name & app name)
// */
//fun Context.getApps(): String {
//    val pm = this.packageManager ?: return ""
//    val packages = pm.getInstalledPackages(0) ?: return ""
//    val list = mutableListOf<AppBean>()
//    packages.forEach {
//        // 属于系统应用
//        // if ((ApplicationInfo.FLAG_SYSTEM and it.applicationInfo.flags) != 0)
//        //无Icon的应用
//        //if (it.applicationInfo.loadIcon(pm) == null)
//        list.add(AppBean(it.packageName, pm.getApplicationLabel(it.applicationInfo)?.toString()
//                ?: ""))
//    }
//    return Gson().toJson(list)
//}

