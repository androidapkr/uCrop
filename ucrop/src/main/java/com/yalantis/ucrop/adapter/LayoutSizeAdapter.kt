package com.yalantis.ucrop.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yalantis.ucrop.R
import com.yalantis.ucrop.util.PREFIX_X
import com.yalantis.ucrop.util.resizeInRangeOf
import com.yalantis.ucrop.util.toPx
import kotlinx.android.synthetic.main.layout_size_child.view.*

internal class LayoutSizeAdapter(val listener: OnLayoutSizeListener?) : RecyclerView.Adapter<ItemHolder>() {

    val list = mutableListOf(
            "2048 x 2048 x Free x Free",
            "2048 x 2048 x Square x 1:1",
            "1242 x 2208 x Smartphone x phone",
            "1080 x 1080 x Square x instagram",
            "1080 x 1350 x Portrait x instagram",
            "1080 x 566 x Landscape x instagram",
            "2048 x 1024 x Wide x 2:1",
            "2048 x 683 x Extra Wide x 3:1",
            "2048 x 1365 x Mid Width x 3:2",
            "1536 x 2048 x Portrait x 3:4",
            "2048 x 1152 x Cinematic x 16:9",
            "1600 x 1600 x Square x facebook",
            "1702 x 630 x Cover x facebook",
            "1200 x 627 x Post x facebook",
            "2220 x 1500 x Brand cover x pinterest",
            "1200 x 1800 x Post x pinterest",
            "1024 x 512 x Post x twitter",
            "1500 x 500 x Cover x twitter",
            "1080 x 2340 x Geo Filter x snapchat",
            "1400 x 800 x Post x linkedin",
            "1280 x 720 x Thumbnail x youtube",
            "3360 x 840 x Cover x E",
            "1200 x 1000 x Medium Rectangle x google",
            "1344 x 1120 x Large Rectangle x google",
            "728 x 90 x Leaderboard x google",
            "900 x 1800 x Half Page x google",
            "1280 x 400 x Large Mobile Banner x google"
    )

    val size = 100.toPx()
    val size80 = 80.toPx()

    val marginTop = 4.toPx()
    val marginBottom = 16.toPx()

    val marginLeft = 8.toPx()
    val marginRight = marginLeft

    private var selectedItemPosition = -1

    fun updateCustomList(width: String, height: String) {
        if (itemCount == 26) {
            selectedItemPosition = 0
            list.add(0, "$width x $height x Custom x Custom")
            notifyItemInserted(0)
        } else {
            selectedItemPosition = 0
            list.removeAt(0)
            notifyItemRemoved(0)
            list.add(0, "$width x $height x Custom x Custom")
            notifyItemInserted(0)
        }
    }

    fun setSelected(position: Int) {
        if(position == RecyclerView.NO_POSITION) return
        val old: Int = selectedItemPosition
        selectedItemPosition = -1
        notifyItemChanged(old)

        selectedItemPosition = position
        notifyItemChanged(selectedItemPosition)
    }

    fun getSelected(): Int {
        return selectedItemPosition
    }

    interface OnLayoutSizeListener {
        fun onLayoutSizeSelected(position: Int, width: Int, height: Int, view: View?)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ItemHolder {
        return ItemHolder(LayoutInflater.from(viewGroup.context).inflate(R.layout.layout_size_child, viewGroup, false))
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        holder.itemView.card_view.strokeWidth = if (position == selectedItemPosition) 2.toPx() else 0
//        holder.itemView.card_view.strokeWidth = 0

        val array = list[position].split(PREFIX_X)

        when (array[3]) {
            "Free" -> {
                holder.itemView.image_view.setImageResource(R.drawable.svg_free_crop)
                holder.itemView.text_view.text = ""
            }
            "phone" -> {
                holder.itemView.image_view.setImageResource(R.drawable.svg_cellphone)
                holder.itemView.text_view.text = ""
            }
            "instagram" -> {
                holder.itemView.image_view.setImageResource(R.drawable.svg_instagram)
                holder.itemView.text_view.text = ""
            }
            "facebook" -> {
                holder.itemView.image_view.setImageResource(R.drawable.svg_facebook)
                holder.itemView.text_view.text = ""
            }
            "pinterest" -> {
                holder.itemView.image_view.setImageResource(R.drawable.svg_pinterest)
                holder.itemView.text_view.text = ""
            }
            "twitter" -> {
                holder.itemView.image_view.setImageResource(R.drawable.svg_twitter)
                holder.itemView.text_view.text = ""
            }
            "snapchat" -> {
                holder.itemView.image_view.setImageResource(R.drawable.svg_snapchat)
                holder.itemView.text_view.text = ""
            }
            "linkedin" -> {
                holder.itemView.image_view.setImageResource(R.drawable.svg_linkedin)
                holder.itemView.text_view.text = ""
            }
            "youtube" -> {
                holder.itemView.image_view.setImageResource(R.drawable.svg_youtube)
                holder.itemView.text_view.text = ""
            }
            "google" -> {
                holder.itemView.image_view.setImageResource(R.drawable.svg_google)
                holder.itemView.text_view.text = ""
            }
            "E" -> {
                holder.itemView.image_view.setImageResource(R.drawable.svg_etsy)
                holder.itemView.text_view.text = ""
            }
            else -> {
                holder.itemView.image_view.setImageDrawable(null)
                holder.itemView.text_view.text = array[3]
            }
        }

        val param = holder.itemView.card_view.layoutParams as RecyclerView.LayoutParams
        val dimen = doubleArrayOf(array[0].toDouble(), array[1].toDouble()).resizeInRangeOf((size80).toDouble(), (size).toDouble())

        if (dimen[1] < 90) dimen[1] = 90.toDouble()

        param.width = size80
        param.height = dimen[1].toInt()

        if (position == 0)
            param.leftMargin = marginLeft + marginTop / 2
        else
            param.leftMargin = marginLeft

        if (position == itemCount - 1)
            param.rightMargin = marginRight + marginTop / 2
        else
            param.rightMargin = marginRight

        param.topMargin = marginTop + (size - dimen[1].toInt())
        param.bottomMargin = marginBottom

        holder.itemView.setOnClickListener {
            listener!!.onLayoutSizeSelected(position, array[0].toInt(), array[1].toInt(), holder.itemView)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}