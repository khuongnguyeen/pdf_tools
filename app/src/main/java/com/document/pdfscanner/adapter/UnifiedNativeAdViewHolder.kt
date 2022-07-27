package com.document.pdfscanner.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.document.pdfscanner.R
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAdView

class UnifiedNativeAdViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
     var adView: NativeAdView

     init {
         adView = view.findViewById<NativeAdView>(R.id.ad_view)



         // The MediaView will display a video asset if one is present in the ad, and the
         // first image asset otherwise.
         adView.mediaView = adView.findViewById(R.id.ad_media)

         // Register the view used for each individual asset.
         adView.headlineView = adView.findViewById(R.id.ad_headline)
         adView.bodyView = adView.findViewById(R.id.ad_body)
         adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
         adView.iconView = adView.findViewById(R.id.ad_icon)
         adView.priceView = adView.findViewById(R.id.ad_price)
         adView.starRatingView = adView.findViewById(R.id.ad_stars)
         adView.storeView = adView.findViewById(R.id.ad_store)
         adView.advertiserView = adView.findViewById(R.id.ad_advertiser)
     }


}