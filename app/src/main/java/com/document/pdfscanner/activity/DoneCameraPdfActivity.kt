package com.document.pdfscanner.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxError
import com.applovin.mediation.nativeAds.MaxNativeAdListener
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder
import com.document.pdfscanner.App
import com.document.pdfscanner.R
import com.document.pdfscanner.model.ItemPDFModel
import com.document.pdfscanner.ulti.ConstantSPKey
import com.document.pdfscanner.ulti.Utils
import com.document.pdfscanner.ulti.Utils.convertUnits
import com.document.pdfscanner.ulti.Utils.shareFile
import io.me.ndk.adsconfig.LovinInterstitialAds
import kotlinx.android.synthetic.main.ts.*
import java.io.File

class DoneCameraPdfActivity : AppCompatActivity() {
    lateinit var lovinInterstitialAds: LovinInterstitialAds
    private lateinit var nativeAdLoader: MaxNativeAdLoader
    private lateinit var nativeAdLayout: FrameLayout
    private var nativeAd: MaxAd? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ts)
        val s = intent.getStringExtra("DoneCameraPdfActivity")
        if (s != null) setupOpenPath(this, s)

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
        lovinInterstitialAds = LovinInterstitialAds(this)
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



    override fun onBackPressed() {}

    private fun setupOpenPath(context: Context, path: String) {
        home_progress_view.setOnClickListener {
            context.startActivity(
                Intent(
                    context,
                    HomeActivity::class.java
                ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
        }
        open_file.setOnClickListener {
            val file = File(path)
            val itemPDFModel =
                ItemPDFModel(
                    convertUnits(file.length().toDouble()),
                    file.name,
                    file.absolutePath
                )
            val bundle = Bundle()
            bundle.putSerializable(ConstantSPKey.INFO_PDF, itemPDFModel)
            context.startActivity(
                Intent(
                    context,
                    ReadPDFActivity::class.java
                ).putExtras(bundle)
            )
        }

        share_file.setOnClickListener {
            shareFile(context, Uri.fromFile(File(path)))
        }
    }


}