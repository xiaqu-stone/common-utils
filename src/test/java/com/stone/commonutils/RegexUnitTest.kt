package com.stone.commonutils

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.regex.Pattern

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class RegexUnitTest {
    @Test
    fun regexUrl() {
//        assertEquals(true, "http://www.google.com".checkURL())
//        assertEquals(true, "https://www.google.com".checkURL())
        assertEquals(true, "https://www.google.com/".checkURL())
//        assertEquals(true, "www.google.com/".checkURL())
//        assertEquals(true, "http://12.34.56.78/".checkURL())
//        assertEquals(true, "http://12.34.56.78/test.html".checkURL())
//        assertEquals(true, "颠倒是非http://12.34.56.78/test.html大幅度".checkURL())
//        assertEquals(true, "ftp://12.34.56.78/test.html".checkURL())
//        assertEquals(true, "file://12.34.56.78/test.html".checkURL())

        println("https://www.google.com/".matchURL())
        println("颠倒是非http://12.34.56.78/test.html大幅度".matchURL())
    }

    @Test
    fun regexHost() {
        assertEquals(true, "ssss://www.google.com".checkHost())
        assertEquals(true, "http://nkz-testing.kuainiujinke.com".checkHost())
        println("http://nkz-testing.kuainiujinke.com".checkHost())
        println("http://www.google.com/".matchHost())
        println("颠倒是非http://12.34.56.78/test.html".matchHost())
    }

    @Test
    fun regexColor() {
//        assertTrue("#123".checkColor())
//        assertTrue("#111222".checkColor())
//        assertTrue("#1234".checkColor())
//        assertTrue("#12345678".checkColor())

        println("#123".matchColor())
        println("#111222".matchColor())
        println("#1234".matchColor())
        println("#12345678".matchColor())
    }

    @Test
    fun regexMention() {
        val source = "【官员辱骂采访记者被微@stone博曝光后遭通报批评】5月4日，新疆人民广播电台 @新疆新闻广播 新疆新闻广播新疆新闻广播新疆新闻广播新疆新闻广播新疆新闻广播新疆新闻广播新疆新闻广播-孙建忠在喀什地区建设局采访时，" +
                "遭行政办公室主任霍敏辱骂，后被@xiaqu 微博曝光，霍敏当@天就根据-被建设局通报批评，扣罚一个月工资并取消@年终评优 资格。孙建忠称已接到霍敏电话说要当面道歉。http://t.cn/hg3PxV"
        val pattern = Pattern.compile(REGEX_MENTION)
        val matcher = pattern.matcher(source)
        println("start match")
        while (matcher.find()) {
            println(matcher.group())
        }
        println("matcher end ")

    }
}