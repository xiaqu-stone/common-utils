package com.stone.commonutils

import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class FormatUnitTest {
    @Test
    fun doubleFormatDecimal() {
        println(0.0.formatDecimals())
        println(0.00.formatDecimals())
        println(0.toDouble().formatDecimals())
        println(1.222224444.formatDecimals())
        println(1.244.formatDecimals())
        println(1.266.formatDecimals())
        println("======================")//even;
        println(1.265.formatDecimals())//1.26
        println(1.255.formatDecimals())//1.25
        println(1.045.formatDecimals())//1.04, -
        println(1.145.formatDecimals())//1.15, +
        println(1.245.formatDecimals())//1.25, +
        println(1.345.formatDecimals())//1.34, -
        println(1.445.formatDecimals())//1.45, +
        println(1.545.formatDecimals())//1.54, -
        println(1.645.formatDecimals())//1.65, +
        println(1.745.formatDecimals())//1.75, +
        println(1.845.formatDecimals())//1.84, -
        println(1.945.formatDecimals())//1.95, +
    }
}