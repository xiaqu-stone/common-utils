package com.stone.commonutils

import com.stone.log.Logs
import org.jetbrains.anko.doAsync
import java.security.SecureRandom

/**
 * Created By: sqq
 * Created Time: 8/23/18 4:06 PM.
 *
 * 其他工具类
 */
object QUtil {

    /**
     * 获取指定位数的随机字符串
     *
     * @param count 指定字符串的长度
     */
    fun getRandomString(count: Int = 1): String {
        if (count <= 0) throw RuntimeException(" the count should be 0+")
        return try {
            val secureRandom = SecureRandom.getInstance("SHA1PRNG")
            val stringBuffer = StringBuffer()
            for (i in 0 until count) {
                val randomNum = Math.abs(secureRandom.nextInt() % 52)
                if (randomNum > 26) {
                    stringBuffer.append(('a'.toInt() + (randomNum - 26)).toChar())
                } else {
                    stringBuffer.append(('A'.toInt() + randomNum).toChar())
                }
            }
            stringBuffer.toString()
        } catch (e: Exception) {
            Logs.w("getRandomString: ${e.message}")
            "error"
        }
    }

    /**
     * 输出一段程序的执行时间
     * @param times 指定程序执行多少次
     * @param isAsync 是否开启子线程中去执行
     * @param codeFun 将被执行的程序
     */
    @JvmOverloads
    fun logDuration(times: Int = 1, isAsync: Boolean = true, codeFun: () -> Unit) {
        val task = {
            val start = System.currentTimeMillis()
            Logs.i("logDuration: the start is $start >>>>>>>>>>>>>>>>>>")
            for (i in 1..times) {
                codeFun()
            }
            val end = System.currentTimeMillis()
            Logs.i("logDuration: the end is $end and duration is {${end - start}} <<<<<<<<<<<<<<<<<<<<<<")
        }
        if (isAsync) doAsync { task() } else task()
    }
}