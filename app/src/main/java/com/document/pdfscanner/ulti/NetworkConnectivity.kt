package com.document.pdfscanner.ulti

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager

object NetworkConnectivity {
    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        @SuppressLint("MissingPermission") val netInfo = cm.activeNetworkInfo
        //should check null because in airplane mode it will be null
        return netInfo != null && netInfo.isConnected
    }
}