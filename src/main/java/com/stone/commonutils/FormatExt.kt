package com.stone.commonutils

import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created By: sqq
 * Created Time: 8/22/18 5:47 PM.
 *
 * 格式化工具类
 */


/**
 * 时间戳 格式化
 */
@JvmOverloads
fun Long.formatDate(format: String = "yyyy-MM-dd HH:mm:ss E"): String {
    if (this <= 0L) return toString()
    val simpleDateFormat = SimpleDateFormat(format, Locale.CHINA)
    return simpleDateFormat.format(Date(this))
}

/**
 * byte -> MB or GB
 * 将字节大小转换为 M G
 */
fun Long.formatFileSize(): String {
    if (this <= 0L) return toString()
    val l = this.toDouble()
    return when {
        l <= 1024 -> this.toString() + "B"
        l / 1024 <= 1024 -> (l / 1024).toInt().toString() + "KB"
        l / 1024 / 1024 <= 1024 -> (l / 1024 / 1024).toInt().toString() + "MB"
        l / 1024 / 1024 / 1024 <= 1024 -> (l / 1024 / 1024 / 1024).toInt().toString() + "GB"
        else -> (l / 1024 / 1024 / 1024 / 1024).toInt().toString() + "TB"
    }
}

/**
 * 保留小数
 * @param scale 几位小数
 * @param roundMode 针对5的处理，默认是四舍六入五成双；
 *
 * [BigDecimal.ROUND_UP]：全部进1；远离0的方向舍入
 * [BigDecimal.ROUND_DOWN]：全部舍去；靠近0的方向舍入
 * [BigDecimal.ROUND_FLOOR]: 正数等同于[BigDecimal.ROUND_DOWN];负数等同于[BigDecimal.ROUND_UP];即靠近负无穷大的方向舍入
 * [BigDecimal.ROUND_CEILING]:正数等同于[BigDecimal.ROUND_UP];负数等同于[BigDecimal.ROUND_DOWN];即靠近正无穷大的方向舍入
 * [BigDecimal.ROUND_HALF_EVEN]:四舍六入五成双
 * [BigDecimal.ROUND_HALF_DOWN]:五舍六入
 * [BigDecimal.ROUND_HALF_UP]:四舍五入
 *
 */
fun Double.formatDecimals(scale: Int = 2, roundMode: Int = BigDecimal.ROUND_HALF_EVEN): String {
    // Note：8/23/18 此处创建BigDecimal对象不可直接使用Double，需要将其想转化为字符串之后再行创建BigDecimal。
    val bigDecimal = BigDecimal(this.toString())
    return bigDecimal.setScale(scale, roundMode).toString()
}