package com.document.pdfscanner.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.document.pdfscanner.R
import com.document.pdfscanner.interfaces.ItemSelectClickListener
import com.document.pdfscanner.model.BackList
import com.document.pdfscanner.model.DataListMerge

import com.document.pdfscanner.model.ItemPDFModel
import com.document.pdfscanner.ulti.Action

class SelectPDFAdapter(
    var mContext: Context,
    var mList: MutableList<DataListMerge>,
    var grid: Boolean,
    var mColor: Int,
    var mAction: String
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val start = System.currentTimeMillis()
    var setItemSelectClickListener: ItemSelectClickListener = mContext as ItemSelectClickListener
    private val columns1 = 1
    private val columns3 = 3
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == columns1) {
            ItemViewHolder(
                LayoutInflater.from(mContext)
                    .inflate(R.layout.item_select_single_pdf, parent, false)
            )
        } else {
            ItemViewHolder(
                LayoutInflater.from(mContext)
                    .inflate(R.layout.item_pdf_select_grid, parent, false)
            )
        }

    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val itemViewHolder = holder as ItemViewHolder
        val item = mList[position]
        itemViewHolder.textViewName.text = item.itemPDFModel?.name
        itemViewHolder.textViewSize.text = item.itemPDFModel?.size
        if (mAction == Action.MERGE_PDF) {
            if (BackList.instance!!.list[position].click == true) {
                itemViewHolder.imageViewTick.setBackgroundResource(R.drawable.bg_ads)
            } else {
                itemViewHolder.imageViewTick.setBackgroundResource(R.drawable.background_item_ads)
            }
        } else {
            itemViewHolder.imageViewTick.visibility = View.GONE
        }
        itemViewHolder.card.setOnClickListener {
            setOnClick(position, itemViewHolder, item.itemPDFModel!!)
        }
        Log.w("yyttt", "${System.currentTimeMillis() - start}")
    }

    private fun setOnClick(position: Int, itemViewHolder: ItemViewHolder, item: ItemPDFModel) {
        if (mAction == Action.SPLIT_PDF) {
            setItemSelectClickListener.onSplitClick(item)
        } else
            if (mAction == Action.PDF_TO_IMG) {
                setItemSelectClickListener.onPDFToImageClick(item)
            } else
                if (mAction == Action.EXTRACT_IMG) {
                    setItemSelectClickListener.onExtractImageClick(item)
                } else
                    if (mAction == Action.EXTRACT_TEXT) {
                        setItemSelectClickListener.onExtractTextClick(item)
                    } else
                        if (mAction == Action.COMPRESS_PDF) {
                            setItemSelectClickListener.onCompressClick(item)
                        } else
                            if (mAction == Action.REMOVE_DUCALICATE_PAGES) {
                                setItemSelectClickListener.onRemovePageClick(item)
                            } else if (mAction == Action.OPEN_PDF) {
                                setItemSelectClickListener.onOpenPDF(item)
                            } else if (mAction == Action.MERGE_PDF) {
                                if (BackList.instance!!.list[position].click == false) {
                                    itemViewHolder.imageViewTick.setBackgroundResource(R.drawable.bg_ads)
                                    BackList.instance!!.list[position].click = true
                                } else {
                                    itemViewHolder.imageViewTick.setBackgroundResource(R.drawable.background_item_ads)
                                    BackList.instance!!.list[position].click = false
                                }
                            }
    }

    override fun getItemViewType(position: Int): Int {
        return if (!grid) {
            columns1
        } else {
            columns3
        }
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var textViewName: TextView = itemView.findViewById(R.id.item_name)
        internal var textViewSize: TextView = itemView.findViewById(R.id.item_size)
        internal var imageViewTick: LinearLayout = itemView.findViewById(R.id.item_ok)
        internal var card: CardView = itemView.findViewById(R.id.card)

    }
}