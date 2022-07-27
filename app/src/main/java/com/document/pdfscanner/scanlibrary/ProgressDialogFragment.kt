package com.document.pdfscanner.scanlibrary

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.DialogFragment

@SuppressLint("ValidFragment")
class ProgressDialogFragment(var message: String) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = ProgressDialog(activity)
        val keyListener =
            DialogInterface.OnKeyListener { _, keyCode, _ -> keyCode == KeyEvent.KEYCODE_BACK }
        with(dialog) {
            isIndeterminate = true
            setMessage(message)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            setOnKeyListener(keyListener)
        }
        return dialog
    }
}