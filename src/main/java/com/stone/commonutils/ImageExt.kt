package com.stone.commonutils

import android.graphics.Bitmap
import android.graphics.Canvas
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

