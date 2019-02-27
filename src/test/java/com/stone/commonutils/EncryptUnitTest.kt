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
        Assert.assertEquals("11111", "1234567890".toSHA1())
    }
}