package com.stone.commonutils

import android.util.Base64
import com.stone.log.Logs
import java.io.*
import java.math.BigInteger
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.*
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPublicKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

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

fun String?.toSHA1(): String {
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
        val v = b.toInt() and 0xFF
        if (v < 0x10) hex.append("0")
        hex.append(Integer.toHexString(v))
    }
    return hex.toString()
}

/**
 * OkHttp 不支持中文
 * 一定要转码
 * @return utf-8 转码后格式
 */
fun String?.toUrlEncoded(): String {
    if (this == null) return "null"
    return try {
        URLEncoder.encode(this, "UTF-8")
    } catch (e: Exception) {
        e.printStackTrace()
        "unknown"
    }
}

fun String?.toUrlDecoded(): String {
    if (this == null) return "null"
    return try {
        URLDecoder.decode(this, "UTF-8")
    } catch (e: Exception) {
        "null"
    }
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

fun ByteArray.encodeBase64String(): String {
    return Base64.encodeToString(this, Base64.NO_WRAP)
}

fun ByteArray.decodeBase64String(): String {
    return String(Base64.decode(String(this), Base64.NO_WRAP))
}


object RsaEncrypt {
    //    const val RSA = "RSA/ECB/PKCS1Padding"
    const val RSA = "RSA"

    /**
     * 随机生成RSA密钥对
     * @param keyLength 密钥长度，范围：512～2048  一般1024
     */
    fun generateRSAKeyPair(keyLength: Int): KeyPair? {
        return try {
            val kpg = KeyPairGenerator.getInstance(RSA)
            kpg.initialize(keyLength)
            kpg.genKeyPair()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 随机生成RSA密钥对(默认密钥长度为1024)
     */
    fun generateRSAKeyPair(): KeyPair? {
        return generateRSAKeyPair(1024)
    }

    /**
     * 用公钥加密
     * 每次加密的字节数，不能超过密钥的长度值除以 8 减去11
     *
     * @param data      需加密数据的byte数据
     * @param publicKey 公钥
     * @return 加密后的byte型数据
     */
    fun encryptData(data: ByteArray, publicKey: PublicKey): ByteArray? {
        return try {
            val cipher = Cipher.getInstance(RSA)
            // 编码前设定编码方式及密钥
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            // 传入编码数据并返回编码结果
            cipher.doFinal(data)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 分段加密
     *
     * 注意 ByteData 过大时，会导致OOM
     */
    fun encryptBigData(source: ByteArray, publicKey: PublicKey, keyLength: Int): ByteArray? {
        return try {//可支持的最大加密长度
            val encryptBlock = keyLength / 8 - 11
            //需要分成多少段
            var nBlock = source.size / encryptBlock
            if (source.size % encryptBlock != 0) nBlock += 1
            Logs.d("encryptBigData: nBlock:$nBlock, lengthBlock:$encryptBlock")
            //输出Buffer
            val baos = ByteArrayOutputStream(nBlock * encryptBlock)
            val cipher = Cipher.getInstance(RSA)
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            var offset = 0
            while (offset < source.size) {
                var inputLen = source.size - offset
                if (inputLen > encryptBlock) inputLen = encryptBlock
                val encryptData = cipher.doFinal(source, offset, inputLen)
                baos.write(encryptData)
                offset += encryptBlock
            }
            val toByteArray = baos.toByteArray()
            baos.close()
            toByteArray
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun encryptBigData(source: File, destEncrypt: File, publicKey: PublicKey, keyLength: Int) {
        return try {//可支持的最大加密长度
            if (!source.exists()) return
            val encryptBlock = keyLength / 8 - 11
            val bis = BufferedInputStream(FileInputStream(source))
            if (!destEncrypt.parentFile.exists()) {
                destEncrypt.parentFile.mkdirs()
            }

            if (!destEncrypt.exists()) destEncrypt.createNewFile()

            val bos = BufferedOutputStream(FileOutputStream(destEncrypt))
            val blockData = ByteArray(encryptBlock)
            val cipher = Cipher.getInstance(RSA)
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            var read = bis.read(blockData)
            while (read != -1) {
                val encryptData = cipher.doFinal(blockData, 0, read)
                bos.write(encryptData)
                read = bis.read(blockData)
            }
            bos.flush()
            bis.close()
            bos.close()
            Logs.d("encryptBigData: 加密成功")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun decryptBigData(source: File, destDecrypt: File, privateKey: PrivateKey, keyLength: Int) {
        return try {//可支持的最大加密长度
            if (!source.exists()) return
            val decryptBlock = keyLength / 8
            val bis = BufferedInputStream(FileInputStream(source))
            if (!destDecrypt.parentFile.exists()) {
                destDecrypt.parentFile.mkdirs()
            }
            if (!destDecrypt.exists()) destDecrypt.createNewFile()
            val bos = BufferedOutputStream(FileOutputStream(destDecrypt))
            val blockData = ByteArray(decryptBlock)
            val cipher = Cipher.getInstance(RSA)
            cipher.init(Cipher.DECRYPT_MODE, privateKey)
            var read = bis.read(blockData)
            while (read != -1) {
                val encryptData = cipher.doFinal(blockData, 0, read)
                bos.write(encryptData)
                read = bis.read(blockData)
            }
            bos.flush()
            bis.close()
            bos.close()
            Logs.d("decryptBigData: 解密成功")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 分段解密
     *
     * 注意 ByteData OOM
     */
    fun decryptBigData(source: ByteArray, privateKey: PrivateKey, keyLength: Int): ByteArray? {
        return try {//可支持的最大加密长度
            val decryptBlock = keyLength / 8
            //需要分成多少段
            var nBlock = source.size / decryptBlock
            if (source.size % decryptBlock != 0) nBlock += 1
            Logs.d("encryptBigData: nBlock:$nBlock, lengthBlock:$decryptBlock")
            //输出Buffer
            val baos = ByteArrayOutputStream(nBlock * decryptBlock)
            val cipher = Cipher.getInstance(RSA)
            cipher.init(Cipher.DECRYPT_MODE, privateKey)
            var offset = 0
            while (offset < source.size) {
                var inputLen = source.size - offset
                if (inputLen > decryptBlock) inputLen = decryptBlock
                val decryptData = cipher.doFinal(source, offset, inputLen)
                baos.write(decryptData)
                offset += decryptBlock
            }
            val toByteArray = baos.toByteArray()
            baos.close()
            toByteArray
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    /**
     * 用私钥解密
     *
     * @param encryptedData 经过encryptedData()加密返回的byte数据
     * @param privateKey    私钥
     */
    fun decryptData(encryptedData: ByteArray, privateKey: PrivateKey): ByteArray? {
        return try {
            val cipher = Cipher.getInstance(RSA)
            cipher.init(Cipher.DECRYPT_MODE, privateKey)
            cipher.doFinal(encryptedData)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 通过公钥byte[](publicKey.getEncoded())将公钥还原，适用于RSA算法
     */
    @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class)
    fun getPublicKey(keyBytes: ByteArray): PublicKey {
        val keySpec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance(RSA)
        return keyFactory.generatePublic(keySpec)
    }

    /**
     * 通过私钥byte[]将私钥还原，适用于RSA算法
     */
    @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class)
    fun getPrivateKey(keyBytes: ByteArray): PrivateKey {
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance(RSA)
        return keyFactory.generatePrivate(keySpec)
    }

    /**
     * 使用N、e值还原公钥
     */
    @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class)
    fun getPublicKey(modulus: String, publicExponent: String): PublicKey {
        val bigIntModulus = BigInteger(modulus)
        val bigIntPrivateExponent = BigInteger(publicExponent)
        val keySpec = RSAPublicKeySpec(bigIntModulus, bigIntPrivateExponent)
        val keyFactory = KeyFactory.getInstance(RSA)
        return keyFactory.generatePublic(keySpec)
    }

    /**
     * 使用N、d值还原私钥
     */
    @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class)
    fun getPrivateKey(modulus: String, privateExponent: String): PrivateKey {
        val bigIntModulus = BigInteger(modulus)
        val bigIntPrivateExponent = BigInteger(privateExponent)
        val keySpec = RSAPublicKeySpec(bigIntModulus, bigIntPrivateExponent)
        val keyFactory = KeyFactory.getInstance(RSA)
        return keyFactory.generatePrivate(keySpec)
    }

    /**
     * 从字符串中加载公钥
     *
     * @param publicKeyStr 公钥数据字符串
     * @throws Exception 加载公钥时产生的异常
     */
    @Throws(Exception::class)
    fun loadPublicKey(publicKeyStr: String): PublicKey {
        try {
            val buffer = Base64.decode(publicKeyStr, Base64.NO_WRAP)
            val keyFactory = KeyFactory.getInstance(RSA)
            val keySpec = X509EncodedKeySpec(buffer)
            return keyFactory.generatePublic(keySpec)
        } catch (e: NoSuchAlgorithmException) {
            throw Exception("无此算法")
        } catch (e: InvalidKeySpecException) {
            throw Exception("公钥非法")
        } catch (e: NullPointerException) {
            throw Exception("公钥数据为空")
        } catch (e: Exception) {
            throw Exception("公钥数据为空")
        }
    }

    /**
     * 从字符串中加载私钥<br></br>
     * 加载时使用的是PKCS8EncodedKeySpec（PKCS#8编码的Key指令）。
     */
    @Throws(Exception::class)
    fun loadPrivateKey(privateKeyStr: String): PrivateKey {
        try {
            val buffer = Base64.decode(privateKeyStr, Base64.NO_WRAP)
            // X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
            val keySpec = PKCS8EncodedKeySpec(buffer)
            val keyFactory = KeyFactory.getInstance(RSA)
            return keyFactory.generatePrivate(keySpec)
        } catch (e: NoSuchAlgorithmException) {
            throw Exception("无此算法")
        } catch (e: InvalidKeySpecException) {
            throw Exception("私钥非法")
        } catch (e: NullPointerException) {
            throw Exception("私钥数据为空")
        }
    }

    /**
     * 读取密钥信息
     */
    @Throws(IOException::class)
    private fun readKey(inputStream: InputStream): String {
        val br = BufferedReader(InputStreamReader(inputStream))
        var readLine: String? = br.readLine()
        val sb = StringBuilder()
        while (readLine != null) {
            if (readLine[0] == '-') {
                continue
            } else {
                sb.append(readLine)
                sb.append('\r')
            }
            readLine = br.readLine()
        }
        return sb.toString()
    }

    /**
     * 从文件中输入流中加载公钥
     *
     * @param inputStream 公钥输入流
     * @throws Exception 加载公钥时产生的异常
     */
    @Throws(Exception::class)
    fun loadPublicKey(inputStream: InputStream): PublicKey {
        try {
            return loadPublicKey(readKey(inputStream))
        } catch (e: IOException) {
            throw Exception("公钥数据流读取错误")
        } catch (e: NullPointerException) {
            throw Exception("公钥输入流为空")
        }
    }

    /**
     * 从文件中加载私钥
     */
    @Throws(Exception::class)
    fun loadPrivateKey(inputStream: InputStream): PrivateKey {
        try {
            return loadPrivateKey(readKey(inputStream))
        } catch (e: IOException) {
            throw Exception("私钥数据读取错误")
        } catch (e: NullPointerException) {
            throw Exception("私钥输入流为空")
        }
    }
}

object AesEncrpt {
    //算法/工作模式/填充模式
    private const val CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding"

    fun aesEncrypt(keyStr: String, plainText: String): String {
        var encrypt: ByteArray? = null
        try {
            val key = generateKey(keyStr)
            val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            encrypt = cipher.doFinal(plainText.toByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return String(Base64.encode(encrypt, Base64.DEFAULT))
    }

    fun aesDecrypt(keyStr: String, encryptData: String): String {
        var decrypt: ByteArray? = null
        try {
            val key = generateKey(keyStr)
            val cipher = Cipher.getInstance(CIPHER_ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, key)
            decrypt = cipher.doFinal(Base64.decode(encryptData, Base64.DEFAULT))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        decrypt ?: return "null"
        return String(decrypt).trim { it <= ' ' }
    }

    private fun generateKey(key: String): Key {
        try {
            return SecretKeySpec(key.toByteArray(), "AES")
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

}
