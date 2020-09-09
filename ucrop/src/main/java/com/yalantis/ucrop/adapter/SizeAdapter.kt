package com.yalantis.ucrop.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yalantis.ucrop.R
import com.yalantis.ucrop.model.AspectRatio
import kotlinx.android.synthetic.main.child_crop_size.view.*
import java.util.*

class SizeAdapter : RecyclerView.Adapter<ItemHolder>() {
    private val list: ArrayList<AspectRatio> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.child_crop_size, parent, false))
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.itemView.imageViewRatioChild.setXYSize(list[position].aspectRatioX / list[position].aspectRatioY);
    }

    override fun getItemCount(): Int {
        return list.size
    }

    init {
        list.add(AspectRatio("1:1", 1f, 1f))
        list.add(AspectRatio("3:4", 3f, 4f))
        list.add(AspectRatio("3:2", 3f, 2f))
        list.add(AspectRatio("16:9", 16f, 9f))
    }
}