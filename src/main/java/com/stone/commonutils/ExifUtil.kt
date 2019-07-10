package com.stone.commonutils

import android.media.ExifInterface
import com.stone.log.Logs
import org.json.JSONObject
import java.io.File
import java.lang.reflect.Modifier

/**
 * Created By: sqq
 * Created Time: 2019-07-02 15:56.
 */
object ExifUtil {

    fun isSupport(file: File): Boolean {
        val name = file.name
        return name.endsWith("JPEG", true) ||
                name.endsWith("JPG", true) ||
                name.endsWith("DNG", true) ||
                name.endsWith("CR2", true) ||
                name.endsWith("NEF", true) ||
                name.endsWith("NRW", true) ||
                name.endsWith("ARW", true) ||
                name.endsWith("RW2", true) ||
                name.endsWith("ORF", true) ||
                name.endsWith("ORF", true) ||
                name.endsWith("PEF", true) ||
                name.endsWith("SRW", true) ||
                name.endsWith("RAF", true) ||
                name.endsWith("HEIF", true)
    }

    fun printExifInfo(filepath: String) {
        try {
            val exif = try {
                ExifInterface(filepath)
            } catch (e: Exception) {
                Logs.w("printExifInfo1111: ${e.message}")
                null
            } ?: return

            val mIsSupportedFile = exif.javaClass.getDeclaredField("mIsSupportedFile")
            mIsSupportedFile.isAccessible = true
            if (!mIsSupportedFile.getBoolean(exif)) {
                Logs.w("printExifInfo: 此图片文件不支持，path = 【$filepath】 ")
                return
            }
            val map = mutableMapOf<String, String>()
            val fields = exif.javaClass.declaredFields
            fields.forEach {
                if (Modifier.isPublic(it.modifiers) && Modifier.isStatic(it.modifiers) && Modifier.isFinal(it.modifiers) && it.type.canonicalName == "java.lang.String") {
                    val value = it.get(exif).toString()
                    Logs.d("printExifInfo: ${it.name}|$value|${exif.getAttribute(value) ?: "null"}")
                    map["${it.name}($value)"] = exif.getAttribute(value) ?: "null"
                }
            }
            val json = JSONObject(map).toString()
            Logs.json(json)
        } catch (e: Exception) {
            Logs.w("printExifInfo222: ${e.message}")
//            e.printStackTrace()
        }
    }

}