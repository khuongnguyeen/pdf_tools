package io.me.ndk.adsconfig.callbacks

import com.applovin.mediation.MaxAd

interface NDKCallback {
    fun onInClose(ad: MaxAd)
}