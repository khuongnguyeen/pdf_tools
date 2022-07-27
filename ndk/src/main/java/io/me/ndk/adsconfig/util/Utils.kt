package io.me.ndk.adsconfig.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.mediation.nativeAds.MaxNativeAdListener
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder
import io.me.ndk.R
import io.me.ndk.adsconfig.LovinCoverAds
import io.me.ndk.adsconfig.LovinInterstitialAds
import io.me.ndk.adsconfig.callbacks.LovinBannerCallBack
import io.me.ndk.adsconfig.callbacks.LovinNativeCallBack

object Utils {
    @SuppressLint("StaticFieldLeak")
    var lovinCoverAds: LovinCoverAds? = null
    @SuppressLint("StaticFieldLeak")
    var lovinInterstitialAds: LovinInterstitialAds? = null
    @SuppressLint("StaticFieldLeak")
    var lovinBannerAds: LovinCoverAds? = null
    @SuppressLint("StaticFieldLeak")
    var lovinSplashInterstitial: LovinInterstitialAds? = null
    @SuppressLint("StaticFieldLeak")

    var mInterstitialAd: MaxInterstitialAd? = null





    fun loadNativeBan(context: Context,id:String, adsContainerLayout: LinearLayout, nativeAdContainer: FrameLayout, loadingFrameLayout: FrameLayout){

        lovinCoverAds = LovinCoverAds(context as Activity)
        lovinCoverAds!!.loadNative(adsContainerLayout, nativeAdContainer, loadingFrameLayout, id,
            true,
            false,
            object : LovinNativeCallBack {
                override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, ad: MaxAd) {
                }

                override fun onNativeAdLoadFailed(adUnitId: String, error: MaxError) {
                }

                override fun onNativeAdClicked(ad: MaxAd) {
                }
            })
    }


    
    fun loadAndShowBanner(context: Context, adsContainerLayout: LinearLayout,
                          bannerAdContainer: LinearLayout,
                          loadingFrameLayout: FrameLayout){
        lovinBannerAds!!.loadBannerAds(adsContainerLayout,
            bannerAdContainer,
            loadingFrameLayout,
            context.getString(R.string.applovin_banner_ids),
            true,
            false,
            object : LovinBannerCallBack {
                override fun onAdLoaded(maxAd: MaxAd?) {
                }

                override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                }

                override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                }

                override fun onAdClicked(maxAd: MaxAd?) {
                }

                override fun onAdExpanded(maxAd: MaxAd?) {
                }

                override fun onAdCollapsed(maxAd: MaxAd?) {
                }

                override fun onAdDisplayed(maxAd: MaxAd?) {
                }

                override fun onAdHidden(maxAd: MaxAd?) {
                }


            })
    }



}