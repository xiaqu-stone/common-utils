package com.stone.commonutils

import org.junit.Assert
import org.junit.Test

/**
 * Created By: sqq
 * Created Time: 2019/2/26 13:35.
 */
class EncryptUnitTest {
    @Test
    fun testSHA1() {
        Assert.assertEquals("01b307acba4f54f55aafc33bb06bbbf6ca803e9a", "1234567890".toSHA1())
//        Assert.assertEquals("11111", "1234567890".toSHA1())
    }

    //%7B%22code%22%3A %20%20 20 %2C%22msg%22%3A%20 %20 %22 ok%22%7D
    //%7B%22code%22%3A %20    20 %2C%22msg%22%3A%20 %22    ok%22%7D
    //{"code": 20,"msg": "ok"}
    @Test
    fun testUrlDecode() {
        //%7B %22 code %22 %3A +   [20] %2C %22 msg %22 %3A +   %22 [ok] %22 %7D
        //%7B% 22 code %22 %3A %20 [20] %2C %22 msg %22 %3A %20 %22 [ok] %22 %7D
        val encodeUrl = "%7B%22code%22%3A%2020%2C%22msg%22%3A%20%22ok%22%7D"
        val decodeUrl = "{\"code\": 20,\"msg\": \"ok\"}"
        Assert.assertEquals(decodeUrl, encodeUrl.toUrlDecoded())
//        Assert.assertEquals("11", encodeUrl.toUrlDecoded())

//        Assert.assertEquals(encodeUrl, encodeUrl.toUrlDecoded().toUrlEncoded())
//        Assert.assertEquals("111", decodeUrl.toUrlEncoded())
        val urlEncoded = decodeUrl.toUrlEncoded()
        println("testUrlDecode: ${decodeUrl.toUrlEncoded()}")
        println("testUrlDecode: ${urlEncoded.toUrlDecoded()}")
        Assert.assertEquals(decodeUrl, urlEncoded.toUrlDecoded())
    }

    @Test
    fun testSubString() {
        //niuhotel://dsq/result?params={code:20,msg:"ok"}
        val url = "niuhotel://dsq/result?&key=value&params={code:20,msg:\"ok\"}"
        val weexPath = url.substring("niuhotel://".length, url.indexOfFirst { it == '?' })
        println("the weex path is : $weexPath")

        Assert.assertEquals("dsq/result", weexPath)

        val paramsStr = url.substring(url.indexOfFirst { it == '?' } + 1)
        val paramsKv = paramsStr.split('&').first {
            println("the foreach is $it")
            it.startsWith("params")
        }
        val param = paramsKv.substring(paramsKv.indexOfFirst { it == '=' } + 1)
        println("the params is $param")
        Assert.assertEquals("11", param)
    }
}