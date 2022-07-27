package com.document.pdfscanner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.document.pdfscanner.R
import com.document.pdfscanner.model.ItemIMG
import java.io.File

class ViewImageAdapter(
    var mContext: Context,
    var mList: ArrayList<ItemIMG>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        val viewHolder: RecyclerView.ViewHolder
        view = LayoutInflater.from(mContext)
            .inflate(R.layout.item_image, parent, false)
        viewHolder = ItemViewHolder(view)
        return viewHolder

    }

    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val itemViewHolder = holder as ItemViewHolder
        val item = mList[position]
        Glide.with(mContext).load(File(item.path)).error(R.drawable.icon).into(itemViewHolder.img)
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var img: ImageView
        init {
            img = itemView.findViewById(R.id.iv_item)
        }

    }

}