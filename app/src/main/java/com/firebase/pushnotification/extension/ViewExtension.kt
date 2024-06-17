package com.firebase.pushnotification.extension

import android.content.Context
import android.view.View
import android.widget.Toast

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.enabled() {
    this.isEnabled = true
}

fun View.disabled() {
    this.isEnabled = false
}

fun Context.showToastMessage(message:String){
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}