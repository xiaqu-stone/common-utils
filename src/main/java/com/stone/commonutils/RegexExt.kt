package com.stone.commonutils

import java.util.regex.Pattern

/**
 * Created By: sqq
 * Created Time: 8/22/18 11:19 AM.
 *
 * 正则验证工具类
 */


/**
 * 验证URL地址
 */
const val REGEX_URL = "(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]"

fun String?.checkURL() = check(REGEX_URL)
fun String?.matchURL() = match(REGEX_URL)

/**
 * 验证域名 host name
 */
const val REGEX_HOST = "((([A-Za-z]{3,9}:(?:\\/\\/)?)(?:[-;:&=\\+\\\$,\\w]+@)?[A-Za-z0-9.-]+(:[0-9]+)?|(?:ww\u200C\u200Bw.|[-;:&=\\+\\\$,\\w]+@)[A-Za-z0-9.-]+)((?:\\/[\\+~%\\/.\\w-_]*)?\\??(?:[-\\+=&;%@.\\w_]*)#?\u200C\u200B(?:[\\w]*))?)"

fun String?.checkHost() = check(REGEX_HOST)
fun String?.matchHost() = match(REGEX_HOST)

/**
 * 颜色 RRGGBB，RGB，AARRGGBB，ARGB
 */
const val REGEX_COLOR = "#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3}|[A-Fa-f0-9]{8}|[A-Fa-f0-9]{4})"

fun String?.checkColor() = check(REGEX_COLOR)
fun String?.matchColor() = match(REGEX_COLOR)

/**
 *
 * CJK Unified Ideographs : http://jrgraphix.net/r/Unicode/4E00-9FFF
 *
 * CJK：The Chinese、Japanese and Korean
 *
 * @用户名
 */
const val REGEX_MENTION = "@[\\w\\p{InCJKUnifiedIdeographs}-]{1,26}"

fun String?.matchMention() = match(REGEX_MENTION)
fun String?.matchAllMention(): List<String>? {
    if (this == null) return null
    val matcher = Pattern.compile(REGEX_MENTION).matcher(this)
    var result: MutableList<String>? = null
    while (matcher.find()) {
        val group = matcher.group()
        if (result == null) result = mutableListOf(group)
        else result.add(group)
    }
    return result
}

/**
 * 验证有效性
 *
 * @return true: 验证通过； false: 验证不通过
 */
private fun String?.check(regex: String): Boolean {
    if (this == null) return false
    return Pattern.matches(regex, this)
}

/**
 * 匹配并返回符合规则的字符串
 *
 * @param regex 正则表达式
 * @return 符合规则的子字符串
 */
private fun String?.match(regex: String): String? {
    if (this == null) return null
    val matcher = Pattern.compile(regex).matcher(this)
    return if (matcher.find()) matcher.group() else null
}
