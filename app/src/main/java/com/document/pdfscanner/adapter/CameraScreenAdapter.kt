package com.document.pdfscanner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.document.pdfscanner.R
import java.io.File

class CameraScreenAdapter(val context: Context, val list: Array<File>?, val inter: ICameraSA): RecyclerView.Adapter<CameraScreenAdapter.ItemViewHolder>() {
    var isClickable = true
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ItemViewHolder(
        LayoutInflater.from(
            context
        ).inflate(R.layout.pdf_card, parent, false)
    )

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val data = list!![position]
        Glide.with(context).load(data).into(holder.thumbnail)


        var name: String
        name = try {
            data.name.replace(".jpg", "")
        } catch (e: Exception) {
            data.name
        }
        if (name.length > 15) {
            val sb = StringBuilder()
            for (i in 0..14) {
                sb.append(name[i])
            }
            name = "$sb..."
        }
        holder.title.text = name
        holder.itemView.setOnClickListener {
            inter.viewClick(data)
        }

    }

    override fun getItemCount(): Int = list!!.size

    interface ICameraSA{
        fun viewClick(file: File)
    }

    inner class ItemViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){

        var thumbnail : ImageView
        var title:TextView
//        var view:View
//        var share:ImageButton
//        var options:ImageButton

        init {
            thumbnail = itemView.findViewById(R.id.pdfThumbnail)
            title = itemView.findViewById(R.id.pdfName)
//            view = itemView.findViewById(R.id.view)
//            share = itemView.findViewById(R.id.share)
//            options = itemView.findViewById(R.id.options)
        }
    }
}