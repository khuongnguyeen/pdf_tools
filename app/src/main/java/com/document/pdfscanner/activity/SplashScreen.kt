package com.document.pdfscanner.activity

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxError
import com.document.pdfscanner.R
import com.google.android.gms.ads.LoadAdError
import io.me.ndk.adsconfig.LovinInterstitialAds
import io.me.ndk.adsconfig.callbacks.LovinInterstitialOnCallBack
import io.me.ndk.adsconfig.util.Utils
import kotlinx.android.synthetic.main.activity_splash_screen.*
import kotlinx.coroutines.*

class SplashScreen : AppCompatActivity() {

    private var isShowDialog = false
    var isShowAds = false
    var checkShow = false
    var checkLoad = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
        window.statusBarColor = Color.TRANSPARENT
        initAds()
        btn_start.setOnClickListener {
            btn_start.visibility = View.INVISIBLE
            intentMethod()
        }
    }

    private fun initAds() {
        Utils.lovinSplashInterstitial = LovinInterstitialAds(this)
        Utils.lovinSplashInterstitial!!.loadAndShowInterstitialAd(getString(R.string.applovin_interstitial_main_ids),
            isRemoteConfigActive = true,
            isAppPurchased = false, mListener = object : LovinInterstitialOnCallBack {
                override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                    checkPerCallActivity()
                }

                override fun onAdLoaded(maxAd: MaxAd?) {
                    checkLoad = true
                    if (checkShow) {
                        Utils.lovinSplashInterstitial?.showInterstitialAds()
                    }
                    Log.e("", "")
                }

                override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                    Log.e("", "")
                }

                override fun onAdDisplayed(maxAd: MaxAd?) {
                    Log.e("", "")
                }

                override fun onAdClicked(maxAd: MaxAd?) {
                    Log.e("", "")
                }

                override fun onAdHidden(maxAd: MaxAd?) {
                    checkPerCallActivity()
                }
            })
    }

    private fun intentMethod() {
        checkShow = true
        if (checkLoad)
            Utils.lovinSplashInterstitial?.showInterstitialAds()

    }

    override fun onDestroy() {
        super.onDestroy()
        Utils.lovinSplashInterstitial?.destroyInterstitialAds()
    }




    private var job: Job? = null
    private var job2: Job? = null



    private fun checkPerCallActivity() {
        try {
            if (Build.VERSION.SDK_INT < 30) {
                if (checkPermission()) {
                     nextScreen()
                } else {
                    d()
                }
            } else {
                if (Environment.isExternalStorageManager()) {
                     nextScreen()
                } else {
                    d()
                }
            }
        } catch (unused: Exception) {
            Log.e("call permission", "___________$unused")
        }
    }

    private fun nextScreen() {
        startMain()
    }

    private fun startMain() {
        val intent = Intent(this@SplashScreen, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }


    override fun onPause() {
        super.onPause()
        job?.cancel()
        job2?.cancel()
    }

    private fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                "android.permission.WRITE_EXTERNAL_STORAGE"
            ) == 0
            && ContextCompat.checkSelfPermission(
                this,
                "android.permission.READ_EXTERNAL_STORAGE"
            ) == 0
        )
            return true
        return false
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
            ), 1
        )
    }

    override fun onRequestPermissionsResult(i: Int, strArr: Array<String?>, iArr: IntArray) {
        super.onRequestPermissionsResult(i, strArr, iArr)
        if (i == 1) {
            checkPerCallActivity()
        }
    }

    private fun f() {
        try {
            val intent = Intent("android.settings.MANAGE_APP_ALL_FILES_ACCESS_PERMISSION")
            intent.addCategory("android.intent.category.DEFAULT")
            intent.data = Uri.fromParts("package", packageName, null as String?)
            startActivityForResult(intent, 7512)
        } catch (unused: java.lang.Exception) {
            val intent2 = Intent()
            intent2.action = "android.settings.MANAGE_ALL_FILES_ACCESS_PERMISSION"
            try {
                startActivityForResult(intent2, 7512)
            } catch (unused2: java.lang.Exception) {
                Log.e("permission __ splash", "\"Error Occurred. Please try after sometime\"")
                finishAffinity()
            }
        }
    }

    private fun d() {
        try {
            if (Build.VERSION.SDK_INT < 30) {
                requestPermission()
            } else {
                f()
            }
        } catch (unused: Exception) {
            Log.e("call permission", "___________$unused")
        }
    }

    override fun onActivityResult(i7: Int, i8: Int, intent: Intent?) {
        super.onActivityResult(i7, i8, intent)
        if (i7 == 7512 && Build.VERSION.SDK_INT >= 30) {
            if (Environment.isExternalStorageManager()) {
                nextScreen()
                isShowDialog = false
                return
            }
            try {
                isShowDialog = true
                val dialog = Dialog(this)
                dialog.requestWindowFeature(1)
                dialog.setContentView(R.layout.dialog_explain_permision)
                dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
                dialog.setCanceledOnTouchOutside(false)
                dialog.setCancelable(false)
                (dialog.findViewById(R.id.txtOk) as TextView).setOnClickListener {
                    d()
                    dialog.dismiss()
                }
                (dialog.findViewById(R.id.txtClose) as TextView).setOnClickListener {
                    finishAffinity()
                }
                dialog.show()
            } catch (unused: Exception) {
                finishAffinity()
            }
        }
    }

    private fun setWindowFlag(bits: Int, on: Boolean) {
        val win = window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }
}
