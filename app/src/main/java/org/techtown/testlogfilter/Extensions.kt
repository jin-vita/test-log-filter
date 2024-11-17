package org.techtown.testlogfilter

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import android.widget.ProgressBar
import android.widget.Toast

var isDebug = true
fun Activity.debug(tag: String, msg: String) {
    if (isDebug) Log.d(tag, msg)
}

fun Activity.error(tag: String, msg: String) {
    if (isDebug) Log.e(tag, msg)
}

fun Activity.method(thread: Thread): String = thread.stackTrace[2].methodName

private var toast: Toast? = null
fun Activity.toast(msg: String, duration: Int = Toast.LENGTH_SHORT) {
    toast?.cancel()
    toast = Toast.makeText(this, msg, duration)
    toast?.show()
}

fun Activity.loadingDialog(): Dialog = Dialog(this).apply {
    requestWindowFeature(Window.FEATURE_NO_TITLE)
    setCancelable(true)
    setContentView(ProgressBar(this@loadingDialog))
    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    window?.setLayout(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    show()
}