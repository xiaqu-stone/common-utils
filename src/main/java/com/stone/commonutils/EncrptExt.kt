package com.stone.commonutils

import android.util.Base64
import com.stone.log.Logs
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.security.DigestInputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Created By: sqq
 * Created Time: 8/22/18 2:32 PM.
 *
 * 加解密工具类
 */


/**
 * MD5加密字符串
 */
fun String?.toMD5(): String {
    if (this == null) return "null"
    return try {
        (MessageDigest.getInstance("MD5").digest(
                this.toByteArray())).toHex()
    } catch (e: NoSuchAlgorithmException) {
        return this
    } catch (e: UnsupportedEncodingException) {
        return this
    }
}

fun String?.toSHA1():String {
    if (this == null) return "null"
    return try {
        (MessageDigest.getInstance("SHA-1").digest(
                this.toByteArray())).toHex()
    } catch (e: NoSuchAlgorithmException) {
        return this
    } catch (e: UnsupportedEncodingException) {
        return this
    }
}


/**
 * 获取文件的MD5值
 *
 * Note: 此方法仅可在小文件时，推荐使用；由于需要会把文件的全部内部读到内存中，转换为字节数组，所以大文件会占用很大的内存；
 *          不考虑内存占用的情况，基于内部限制，文件不可超过2G
 */
fun File.toMD5(): String {
    if (!this.exists() || !this.isFile) throw RuntimeException("the file is not exist or is not a file!!!")
    return try {
        MessageDigest.getInstance("MD5").digest(this.readBytes()).toHex()
    } catch (e: NoSuchAlgorithmException) {
        throw RuntimeException("Huh, MD5 should be supported?", e)
    }
}

/**
 * 获取大文件的MD5值
 */
fun File.toMD5BigFile(): String {
    if (!this.exists() || !this.isFile) throw RuntimeException("the file is not exist or is not a file!!!")
    try {
        this.inputStream().use {
            val digestInputStream = DigestInputStream(it, MessageDigest.getInstance("MD5"))
            val buffer = ByteArray(2 * 1024 * 1024)
            while (digestInputStream.read(buffer) > 0);
            return digestInputStream.messageDigest.digest().toHex()
        }
    } catch (e: Exception) {
        Logs.w("toMD5BigFile: ${e.message}")
        return "error"
    }
}

/**
 * 将字节数组转换为十六进制字符串
 */
fun ByteArray.toHex(): String {
    val hex = StringBuilder(this.size * 2)
    for (b in this) {
//            println("origin byte is $b, 位运算后 ${b.toInt() and 0xFF}, hex运算之后 ${Integer.toHexString(b.toInt() and 0xFF)}")
        if ((b.toInt() and 0xFF) < 0x10) hex.append("0")
        hex.append(Integer.toHexString(b.toInt() and 0xFF))
    }
    return hex.toString()
}

/**
 * OkHttp 不支持中文
 * 一定要转码
 * @return utf-8 转码后格式
 */
fun String?.toUrlEncoded(): String {
    if (this == null) return "unknown"
    val newValue = this.replace("\n", "")
    for (t in newValue.toCharArray().withIndex()) {
        if (t.value <= '\u001f' || t.value >= '\u007f') {
            return try {
                URLEncoder.encode(newValue, "UTF-8")
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                "unknown"
            }
        }
    }
    return newValue
}

/**
 * 利用android工具类，扩展Base64加密快速调用
 */
fun String.encodeBase64(): String {
    return Base64.encodeToString(this.toByteArray(), Base64.NO_WRAP)
}

fun String.decodeBase64(): String {
    return String(Base64.decode(this, Base64.NO_WRAP))
}