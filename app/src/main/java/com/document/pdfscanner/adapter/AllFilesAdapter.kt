package com.document.pdfscanner.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.document.pdfscanner.R
import com.document.pdfscanner.activity.ReadPDFActivity
import com.document.pdfscanner.interfaces.ItemClickListener
import com.document.pdfscanner.interfaces.ItemMenuClickListener
import com.document.pdfscanner.interfaces.ItemSelectClickListener
import com.document.pdfscanner.model.BackList
import com.document.pdfscanner.model.ItemPDFModel
import com.document.pdfscanner.ulti.Action
import com.document.pdfscanner.ulti.ConstantSPKey
import com.document.pdfscanner.ulti.NetworkUtil
import com.document.pdfscanner.ulti.Utils
import java.io.File


class AllFilesAdapter
    (
    var mContext: Context,
    var mList: MutableList<ItemPDFModel>,
    var grid: Boolean
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val start = System.currentTimeMillis()
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
        itemViewHolder.textViewName.text = item.name
        itemViewHolder.textViewSize.text = item.size

        itemViewHolder.imageViewTick.visibility = View.GONE

        itemViewHolder.card.setOnClickListener {
            val file = File(item.path!!)
            val itemPDFModel =
                ItemPDFModel(
                    Utils.convertUnits(file.length().toDouble()),
                    file.name,
                    file.absolutePath
                )
            val bundle = Bundle()
            bundle.putSerializable(ConstantSPKey.INFO_PDF, itemPDFModel)
            mContext.startActivity(
                Intent(
                    mContext,
                    ReadPDFActivity::class.java
                ).putExtras(bundle)
            )

        }
        Log.w("yyttt", "${System.currentTimeMillis() - start}")
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