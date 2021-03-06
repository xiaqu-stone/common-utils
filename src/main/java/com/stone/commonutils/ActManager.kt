package com.stone.commonutils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.util.*

/**
 * Created By: sqq
 * Created Time: 8/29/18 3:39 PM.
 *
 * Activity管理
 *
 * // Note：8/29/18
 * 用可null类型做封装，是因为考虑到Activity会因为内存吃紧被回收的可能性，避免出现非空类型被null赋值的情况
 */
object ActManager {

    private var stack: Stack<Activity?>? = Stack()

    /**
     * 当前APP是否活着（任务栈为空，则认为APP已退出）
     */
    fun isAppAlive(): Boolean {
        return !(stack?.empty() ?: true)
    }

    /**
     * 入栈新的Activity
     */
    fun add(activity: Activity?) {
        stack?.push(activity)// 等同于 stack.add(activity)
    }

    /**
     * 移除栈中指定的Activity
     */
    fun remove(activity: Activity?) {
        stack?.remove(activity)
    }

    /**
     * 获取指定的Activity
     */
    fun get(clazz: Class<*>): Activity? {
        stack?.forEach {
            if (it?.javaClass == clazz) {
                return it
            }
        }
        return null
    }

    /**
     * 移除并finish掉栈中指定的Activity
     */
    fun finish(activity: Activity?) {
        activity?.finish()
    }

    /**
     * 移除并finish掉栈中指定的Activity
     */
    fun finish(clazz: Class<*>) {
        stack?.forEach {
            if (it?.javaClass == clazz) it.finish()
        }
    }

    /**
     * 获取栈顶的Activity，即当前显示的Activity
     *
     * 由于在 ActivityLifecycleCallbacks 中的回调执行中，当 B back to A 时，先执行 A create resume等回调，最后才会执行 B 的onDestroyed 操作 （stack的移除操作放在了这里）；
     * 所以当在这个期间，理论上的栈顶Activity应该是 A 了，但是由于还未执行stack的移除操作，peek 操作获取的仍然是 B ，如果此时使用 B 做了一些依赖Activity 的操作，就会出现异常
     * 故：这里做了一下 peek() 返回的Activity 有效性的校验
     */
    fun top(): Activity? {
        val top = stack?.peek()
        return if (top.isValid()) {
            top
        } else {
            stack?.pop()
            stack?.peek()
        }
    }

    /**
     * 移除并finish栈中指定数量的Activity
     */
    fun pop(count: Int = 1): Unit? {
        return if (count == 1) {
            stack?.pop()?.finish()
        } else {
            for (i in 0 until count) {
                stack?.pop()?.finish()
            }
            null
        }
    }

    /**
     * 清空栈
     */
    fun clear() {
        stack?.clear()
    }

    /**
     * 清除栈，并关闭栈中所有Activity
     */
    fun finishAll() {
        if (stack == null) return
        stack?.forEach {
            it?.finish()
        }
        stack?.clear()
    }

    private val mCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityPaused(activity: Activity?) {
        }

        override fun onActivityResumed(activity: Activity?) {
            val top = stack?.peek()
            if (top != activity && !top.isValid()) {
                stack?.pop()
            }
        }

        override fun onActivityStarted(activity: Activity?) {
        }

        override fun onActivityDestroyed(activity: Activity?) {
            remove(activity)
        }

        override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
        }

        override fun onActivityStopped(activity: Activity?) {
        }

        override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
            add(activity)
        }

    }

    /**
     * Application 中注册全局的Activity生命周期回调
     */
    fun registerActivityLifecycleCallbacks(app: Application) {
        app.registerActivityLifecycleCallbacks(mCallbacks)
    }
}