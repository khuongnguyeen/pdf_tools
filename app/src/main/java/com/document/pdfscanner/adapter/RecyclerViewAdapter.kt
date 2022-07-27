package com.document.pdfscanner.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxError
import com.document.pdfscanner.App
import com.document.pdfscanner.R
import com.document.pdfscanner.activity.ReadPDFActivity
import com.document.pdfscanner.model.ItemPDFModel
import com.document.pdfscanner.ulti.ConstantSPKey
import com.document.pdfscanner.ulti.Utils
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import io.me.ndk.adsconfig.LovinInterstitialAds
import io.me.ndk.adsconfig.callbacks.LovinInterstitialOnCallBack
import java.io.File

internal class RecyclerViewAdapter(
    private val mContext: Context,
    private val mRecyclerViewItems: MutableList<Any>,val lovinInterstitialAds: LovinInterstitialAds
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var textViewName: TextView = itemView.findViewById(R.id.item_name)
        internal var textViewSize: TextView = itemView.findViewById(R.id.item_size)
        internal var imageViewTick: LinearLayout = itemView.findViewById(R.id.item_ok)
        internal var card: CardView = itemView.findViewById(R.id.card)

    }

    override fun getItemCount(): Int {
        return mRecyclerViewItems.size
    }


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

                val menuItemLayoutView: View =
                    LayoutInflater.from(viewGroup.context).inflate(
                        R.layout.item_select_single_pdf, viewGroup, false
                    )
            return    ItemViewHolder(menuItemLayoutView)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

                val itemViewHolder = holder as ItemViewHolder
                val item = mRecyclerViewItems[position] as ItemPDFModel
                itemViewHolder.textViewName.text = item.name
                itemViewHolder.textViewSize.text = item.size
                itemViewHolder.imageViewTick.visibility = View.GONE
                itemViewHolder.card.setOnClickListener {
                    lovinInterstitialAds.showInterstitialAds()
                    lovinInterstitialAds.loadShowAndLoadInterstitialAd(mContext.getString(R.string.applovin_interstitial_main_ids),
                        true,
                        false,object: LovinInterstitialOnCallBack {
                            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                            }

                            override fun onAdLoaded(maxAd: MaxAd?) {
                            }

                            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {

                                val file = File(item.path!!)
                                val itemPDFModel =
                                    ItemPDFModel(
                                        Utils.convertUnits(file.length().toDouble()),
                                        file.name,
                                        file.absolutePath
                                    )
                                val bundle = Bundle()
                                bundle.putSerializable(ConstantSPKey.INFO_PDF, itemPDFModel)
                                mContext.startActivity(Intent(mContext, ReadPDFActivity::class.java).putExtras(bundle))
                            }

                            override fun onAdDisplayed(maxAd: MaxAd?) {
                            }

                            override fun onAdClicked(maxAd: MaxAd?) {
                            }

                            override fun onAdHidden(maxAd: MaxAd?) {
                                val file = File(item.path!!)
                                val itemPDFModel =
                                    ItemPDFModel(
                                        Utils.convertUnits(file.length().toDouble()),
                                        file.name,
                                        file.absolutePath
                                    )
                                val bundle = Bundle()
                                bundle.putSerializable(ConstantSPKey.INFO_PDF, itemPDFModel)
                                mContext.startActivity(Intent(mContext, ReadPDFActivity::class.java).putExtras(bundle))

                            }


                        })


                }

    }


    companion object {
        private const val MENU_ITEM_VIEW_TYPE = 0
    }
}