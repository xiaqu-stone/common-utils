package com.stone.commonutils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable

/**
 * Created By: sqq
 * Created Time: 8/22/18 3:26 PM.
 *
 * 图片相关工具类（Bitmap、Drawable、Canvas等）
 *
 */

/**
 * Drawable to Bitmap
 */
fun Drawable.toBitmapByCanvas(): Bitmap {
    val bitmap = Bitmap.createBitmap(this.intrinsicWidth, this.intrinsicHeight, if (this.opacity == PixelFormat.OPAQUE) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888)
    this.setBounds(0, 0, intrinsicWidth, intrinsicHeight)
    this.draw(Canvas(bitmap))
    return bitmap
}

/**
 * 重新设置位图的宽高
 * @param newWidth 新位图的宽高
 * @param newHeight 新位图的宽高
 * @return 指定宽高的新位图
 */
fun Bitmap.resizeBitmap(newWidth: Float, newHeight: Float): Bitmap {
    val w1 = this.width
    val h1 = this.height
    val sw = newWidth * 1f / w1
    val sh = newHeight * 1f / h1
    val matrix = Matrix()
    matrix.setScale(sw, sh)
    return Bitmap.createBitmap(this, 0, 0, w1, h1, matrix, true)
}


