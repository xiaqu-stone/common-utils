package com.stone.commonutils

import android.support.test.runner.AndroidJUnit4
import com.stone.log.Logs
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created By: sqq
 * Created Time: 2019-07-08 10:38.
 */
@RunWith(AndroidJUnit4::class)
class ExampleTest {

    @Test
    fun testRsa() {
        val data = "大小不同abc"
        val keyPair = RsaEncrypt.generateRSAKeyPair() ?: return
        val encrypt = RsaEncrypt.encryptData(data.toByteArray(), keyPair.public) ?: return
        Logs.i(String(encrypt))
        val base64String = encrypt.encodeBase64String()
        Logs.i(base64String)
        assertEquals("", base64String)
    }

    @Test
    fun testAes() {

    }
}