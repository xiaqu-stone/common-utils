package com.stone.commonutils

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.provider.MediaStore
import android.support.annotation.RequiresPermission
import android.text.TextUtils
import java.io.File

/**
 * Created By: sqq
 * Created Time: 8/31/18 7:35 PM.
 *
 * Intent 隐式跳转工具类
 *
 */

/**
 * 直接拨打电话
 *
 * // Note：8/31/18 需要 危险权限 CALL_PHONE
 *
 * @param phone the phone number
 */
@RequiresPermission(Manifest.permission.CALL_PHONE)
fun Context.callPhoneDirectly(phone: String) {
    this.startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$phone")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

/**
 * 将手机号码带入进入拨号界面，由用户手机拨出电话
 *
 * // Note：8/31/18 不需要权限
 *
 * @param phone the phone number
 */
fun Context.callPhoneDial(phone: String) {
    this.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

/**
 * 打开发短信页面并填充内容
 */
fun Context.sendSMS(phone: String, message: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$phone"))
    intent.putExtra("sms_body", message)
    this.startActivity(intent)
}

/**
 *
 * 跳转去联系人页面，并返回
 * Note：9/18/18 by sqq 跳转返回的时候 接收并处理联系人数据时，需要做运行权限检查，READ_CONTACTS
 */
fun Context.jumpChooseContact(requestCode: Int) {
    (this as? Activity)?.startActivityForResult(
        (Intent(
            Intent.ACTION_PICK,
            ContactsContract.Contacts.CONTENT_URI
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)), requestCode
    )
}

/**
 * 从图库or相册选图
 */
fun Context.selectPicFromGallery(requestCode: Int) {
    (this as? Activity)?.startActivityForResult(
        Intent(Intent.ACTION_PICK).setDataAndType(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            "image/*"
        ), requestCode
    )
}

/**
 * 将拍照后的照片添加到图库中
 *
 * 通过发送广播,通知图库扫描指定文件并添加到对应的媒体数据库中
 *
 * 照片的临时存储目录要在外部存储的共享目录下,否则图库是无法访问到的
 */
fun Context.addPhotoToGallery(photoPath: String?) {
    if (TextUtils.isEmpty(photoPath)) return
    val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
    intent.data = Uri.fromFile(File(photoPath))
    this.sendBroadcast(intent)
}

/**
 * 调用系统相机拍照, 可以获取高清拍照原图的URI
 *
 * @return 当前拍照操作 生成照片对应的URI
 */
fun Context.takePhoto(requestCode: Int): Uri? {
    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)// "android.media.action.IMAGE_CAPTURE"
    val curPhotoUri = this.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())
    intent.putExtra(MediaStore.EXTRA_OUTPUT, curPhotoUri)
    (this as? Activity)?.startActivityForResult(intent, requestCode)
    return curPhotoUri
}

