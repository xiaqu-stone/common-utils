package com.stone.commonutils

import com.stone.log.Logs
import org.jetbrains.anko.doAsync
import org.json.JSONObject
import java.lang.reflect.Modifier
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

    fun printOsBuild(): Map<String, String> {
        val clazz = Class.forName("android.os.Build")
        val map = mutableMapOf<String, String>()
        mapClassField(clazz, map)
        clazz.declaredClasses.forEach {
            mapClassField(it, map)
        }
        val json = JSONObject(map).toString()
        Logs.json(json)
        return map
    }

    fun mapClassField(clazz: Class<*>, map: MutableMap<String, String>) {
        val cName = clazz.name
        clazz.declaredFields.forEach {
            //            Logs.d("mapClassField: ${it.name}")
            try {
                it.isAccessible = true
                val any = it.get(clazz)
//                Logs.d("mapClassField: isAccessible = ${it.isAccessible}, modifiers = ${it.modifiers}")
                var value = ""
                value = when (any) {
                    is Array<*> -> {
                        value += "["
                        any.forEach { l ->
                            //                        Logs.i("mapClassField: $l")
                            value += l.toString() + ", "
                        }
                        (if (value.endsWith(", ", true)) value.substring(0, value.length - 2) else value) + "]"
                    }
                    is List<*> -> {
                        value += "["
                        any.forEach { l ->
                            //                        Logs.i("mapClassField: $l")
                            value += l.toString() + ", "
                        }
                        (if (value.endsWith(", ", true)) value.substring(0, value.length - 2) else value) + "]"
                    }
                    else -> any.toString()
                }

                var modifier = ""

                modifier += when {
                    Modifier.isPrivate(it.modifiers) -> "private "
                    Modifier.isProtected(it.modifiers) -> "protected "
                    Modifier.isPublic(it.modifiers) -> "public "
                    else -> "default "
                }

                if (Modifier.isFinal(it.modifiers)) modifier += "final "
                if (Modifier.isStatic(it.modifiers)) modifier += "static "

                map["$cName.${it.name}:::$modifier"] = value
            } catch (e: Exception) {
                Logs.w("mapClassField: ${it.name},isAccessible = ${it.isAccessible}, modifiers = ${it.modifiers}")
                e.printStackTrace()
            }
        }
    }

}