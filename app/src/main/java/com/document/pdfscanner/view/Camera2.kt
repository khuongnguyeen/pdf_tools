package com.document.pdfscanner.view

import android.app.Activity
import android.view.View


fun Activity.hideBottomUIMenu() {
    val decorView = this.window.decorView
    val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN
    decorView.systemUiVisibility = uiOptions
}



