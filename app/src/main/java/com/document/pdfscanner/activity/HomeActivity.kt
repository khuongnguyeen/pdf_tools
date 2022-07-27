package com.document.pdfscanner.activity

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxError
import com.applovin.mediation.nativeAds.MaxNativeAdListener
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder
import com.document.pdfscanner.App
import com.document.pdfscanner.BuildConfig
import com.document.pdfscanner.R
import com.document.pdfscanner.adapter.RecyclerViewAdapter
import com.document.pdfscanner.internal.Statics
import com.document.pdfscanner.model.ItemPDFModel
import com.document.pdfscanner.ulti.Action
import com.document.pdfscanner.ulti.FileStorage
import com.document.pdfscanner.ulti.Utils.showDialogSweet
import com.document.pdfscanner.view.OnSingleClickListener
import com.google.android.gms.ads.nativead.NativeAd
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.suddenh4x.ratingdialog.AppRating
import com.suddenh4x.ratingdialog.preferences.RatingThreshold
import io.me.ndk.adsconfig.LovinInterstitialAds
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_select_pdf.*
import kotlinx.android.synthetic.main.item_toolmain.*
import kotlinx.android.synthetic.main.navigatation_view.*
import java.io.File

class HomeActivity : BaseActivity(), PermissionListener {
    private var adapter: RecyclerViewAdapter? = null
    val checkNative = MutableLiveData<Int>()

    lateinit var lovinInterstitialAds: LovinInterstitialAds
    private lateinit var nativeAdLoader: MaxNativeAdLoader
    private lateinit var nativeAdLayout: FrameLayout
    private var nativeAd: MaxAd? = null
    val data = mutableListOf<Any>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        lovinInterstitialAds = LovinInterstitialAds(this)
        initView()
        nativeAdLayout = findViewById(R.id.native_ad_layout)
        val adView: View = LayoutInflater.from(this).inflate(R.layout.loading_ad_big, null)
        nativeAdLayout.removeAllViews()
        nativeAdLayout.addView(adView)
        nativeAdLoader = MaxNativeAdLoader(getString(R.string.applovin_small_native_ids), this)

        nativeAdLoader.setNativeAdListener(object : MaxNativeAdListener() {
            override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, ad: MaxAd) {
                if (nativeAd != null) {
                    nativeAdLoader.destroy(nativeAd)
                }

                // Save ad to be rendered later.
                nativeAd = ad
                showAd()
            }

            override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
                nativeAdLayout.removeAllViews()
            }

            override fun onNativeAdClicked(ad: MaxAd) {
            }
        })

        loadAd()
    }


    fun loadAd() {
        nativeAdLoader.loadAd()
    }

    fun showAd() {
        val adView = createNativeAdView()
        // Render the ad separately
        nativeAdLoader.render(adView, nativeAd)
        nativeAdLayout.removeAllViews()
        nativeAdLayout.addView(adView)
    }

    private fun createNativeAdView(): MaxNativeAdView {
        val binder: MaxNativeAdViewBinder = MaxNativeAdViewBinder.Builder(R.layout.native_custom_ad_view)
            .setTitleTextViewId(R.id.title_text_view)
            .setBodyTextViewId(R.id.body_text_view)
            .setAdvertiserTextViewId(R.id.advertiser_textView)
            .setIconImageViewId(R.id.icon_image_view)
            .setMediaContentViewGroupId(R.id.media_view_container)
            .setOptionsContentViewGroupId(R.id.options_view)
            .setCallToActionButtonId(R.id.cta_button)
            .build()
        return MaxNativeAdView(binder, this)
    }

    override fun onDestroy() {
        // Must destroy native ad or else there will be memory leaks.
        if (nativeAd != null) {
            // Call destroy on the native ad from any native ad loader.
            nativeAdLoader.destroy(nativeAd)
        }

        // Destroy the actual loader itself
        nativeAdLoader.destroy()

        super.onDestroy()
    }


    private fun initView() {
        recyclerview.layoutManager = LinearLayoutManager(this)
        adapter =  RecyclerViewAdapter(this, data,lovinInterstitialAds)
        recyclerview.adapter = adapter
        recyclerview.isNestedScrollingEnabled = false

        show_more.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                startActivity(Intent(this@HomeActivity, RecentFileActivity::class.java))
            }
        })
        cl_share_app.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                shareApp()
            }
        })
        cl_rate_app.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                rateApp()
            }
        })
        cl_list_app.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                listApp()
            }
        })
        cl_send_feedback.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                sendEmailFeedback(this@HomeActivity, BuildConfig.VERSION_NAME)
            }
        })
        merge_pdf.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {

                        startActivity(
                            Intent(applicationContext, SelectPDFActivity::class.java)
                                .putExtra(Action.ACTION_INTENT, Action.MERGE_PDF)
                                .putExtra(Action.ST_IT, Action.ST_ALL)
                        )

            }
        })
        split_pdf.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {

                        startActivity(
                            Intent(applicationContext, SelectPDFActivity::class.java)
                                .putExtra(Action.ACTION_INTENT, Action.SPLIT_PDF)
                                .putExtra(Action.ST_IT, Action.ST_ALL)
                        )

            }
        })
        extra_image.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {


                        startActivity(
                            Intent(applicationContext, SelectPDFActivity::class.java)
                                .putExtra(Action.ACTION_INTENT, Action.EXTRACT_IMG)
                                .putExtra(Action.ST_IT, Action.ST_ALL)
                        )

            }
        })
        image_to_pdf.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {


                        startActivity(Intent(applicationContext, SelectImagesActivity::class.java))

            }
        })
        pdf_ti_image.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {

                        startActivity(
                            Intent(
                                applicationContext,
                                SelectPDFActivity::class.java
                            ).putExtra(Action.ACTION_INTENT, Action.PDF_TO_IMG).putExtra(
                                Action.ST_IT,
                                Action.ST_ALL
                            )
                        )

            }
        })
        compress_pdf.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                        startActivity(
                            Intent(
                                applicationContext,
                                SelectPDFActivity::class.java
                            ).putExtra(Action.ACTION_INTENT, Action.COMPRESS_PDF).putExtra(
                                Action.ST_IT,
                                Action.ST_ALL
                            )
                        )
            }
        })
        applicationContext.getSharedPreferences("PDFtools", MODE_PRIVATE).edit()
            .putString("sessionName", Statics.randString()).apply()
        camera_scanner.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                takePicture()
            }
        })
        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar_main, R.string.nav_open, R.string.nav_close)
        toggle.drawerArrowDrawable.color = Color.BLACK
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        toolbar_main.navigationIcon =
            resources.getDrawable(R.drawable.ic_menu_black_24dp, resources.newTheme())
    }

    fun sendEmailFeedback(context: Context, appVersionName: String = "") {

        val to = "monster@true.ad"
        val subject = "User Feedback for ${getString(R.string.app_name)} ver: $appVersionName"
        val body = "My feedback: "

        val uriBuilder = StringBuilder("mailto:" + Uri.encode(to))
        uriBuilder.append("?subject=" + Uri.encode(subject))
        uriBuilder.append("&body=" + Uri.encode(body))
        val uriString = uriBuilder.toString()

        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(uriString))

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("LOG_TAG", e.localizedMessage)

            // If there is no email client application, than show error message for the user.
            if (e is ActivityNotFoundException) {
                Toast.makeText(
                    context,
                    "No application can handle this request. Please install an email client app.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun shareApp() {
        try {
            val start = System.currentTimeMillis()
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.app_name)
            val sb = StringBuilder()
            sb.append("\nLet me recommend you this application\n\n")
            sb.append("https://play.google.com/store/apps/details?id=")
            sb.append(BuildConfig.APPLICATION_ID + "\n\n")
            shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString())
            startActivity(Intent.createChooser(shareIntent, "choose one"))
            Log.e("ststst", "${System.currentTimeMillis() - start}")
        } catch (unused: Exception) {
            unused.printStackTrace()
        }
    }

    private fun rateApp() {
        try {
            val uri = Uri.parse("market://developer?id=" + BuildConfig.APPLICATION_ID)
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    "android.intent.action.VIEW",
                    Uri.parse("http://play.google.com/store/apps/developer?id=" + BuildConfig.APPLICATION_ID)
                )
            )
        }
    }

    private fun listApp() {
        try {
            val uri = Uri.parse("market://developer?id=AdTrue+Global")
            val goToMarket = Intent(Intent.ACTION_VIEW, uri)
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    "android.intent.action.VIEW",
                    Uri.parse("http://play.google.com/store/apps/developer?id=AdTrue+Global")
                )
            )
        }
    }

    fun takePicture() {
        Dexter.withContext(this)
            .withPermission(android.Manifest.permission.CAMERA)
            .withListener(this)
            .check()
    }

    private fun startScannerActivity() {
        val intent = Intent(applicationContext, ScannerActivity::class.java)
        startActivity(intent)

    }

    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
        startScannerActivity()
    }

    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
        if (p0!!.isPermanentlyDenied) {
           try {
               SweetAlertDialog (this, SweetAlertDialog.WARNING_TYPE).apply {
                   contentText = "You permanently refuse the right to take pictures"
                   confirmText = "Go to settings!"
                   setConfirmClickListener {
                       it.cancel()
                       val inte = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                       val uri: Uri = Uri.fromParts("package", packageName, null)
                       inte.data = uri
                       startActivity(inte)
                   }
                   showCancelButton(true)
                   cancelText = "Cannel"
                   setCancelClickListener {
                       it.cancel()
                   }
                       .show()
               }
           }catch (ex:Exception){
               Toast.makeText(this,"You have denied permission to take pictures this time.",Toast.LENGTH_SHORT).show()
           }
        } else {
           try{
               showDialogSweet(
                   this,
                   SweetAlertDialog.WARNING_TYPE,
                   "You have denied permission to take pictures this time."
               )
           }catch (ex:Exception){
               Toast.makeText(this,"You have denied permission to take pictures this time.",Toast.LENGTH_SHORT).show()
           }
        }
    }


    @SuppressLint("ResourceType")
    private fun initRateApp() {
        AppRating.Builder(this)
            .useGoogleInAppReview()
            .setGoogleInAppReviewCompleteListener { successful ->
//                Toast.makeText(this,"Demo $successful",Toast.LENGTH_SHORT).show()
            }
            .setMinimumLaunchTimes(1)
            .setMinimumDays(1)
            .setMinimumLaunchTimesToShowAgain(2)
            .setMinimumDaysToShowAgain(1)
            .setRatingThreshold(RatingThreshold.FOUR)
            .showIfMeetsConditions()

    }

    override fun onResume() {
        super.onResume()
        initRateApp()
        GetFile().execute()
        if (adapter!= null) adapter!!.notifyDataSetChanged()
    }

    override fun onPermissionRationaleShouldBeShown(p0: PermissionRequest?, p1: PermissionToken?) {
        p1?.continuePermissionRequest()
    }





    @SuppressLint("StaticFieldLeak")
    inner class GetFile : AsyncTask<Void, Void, ArrayList<ItemPDFModel>>() {
        override fun doInBackground(vararg p0: Void?): ArrayList<ItemPDFModel> {

            var dataItem = mutableListOf<ItemPDFModel>()
            val fileStorage = FileStorage()
            val file = File(Environment.getExternalStorageDirectory().toString())
            if (Build.VERSION.SDK_INT >= 29) {
                dataItem = fileStorage.allPdfs(this@HomeActivity)
            } else {
                dataItem =  fileStorage.getList(file)
            }
            return dataItem
        }

        override fun onPostExecute(result: ArrayList<ItemPDFModel>?) {
            onPost(result)
        }

    }


    private fun onPost(arr: ArrayList<ItemPDFModel>?) {
        updateRecyclerView(arr!!)
    }




    fun updateRecyclerView(arr: ArrayList<ItemPDFModel>) {

        data.clear()
        data.addAll(arr)
        if (data.size >10){
            for (i in data.size -1 downTo 10){
                data.removeAt(i)
            }
        }
        Log.e("___________________","HomeActivity_______________________ data = ${data.size}")
        if (adapter!= null) adapter!!.notifyDataSetChanged()

    }




}
