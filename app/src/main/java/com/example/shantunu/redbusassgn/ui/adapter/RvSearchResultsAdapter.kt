package com.example.shantunu.redbusassgn.ui.adapter

import android.content.Context
import android.opengl.Visibility
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.shantunu.redbusassgn.R
import com.example.shantunu.redbusassgn.Utils
import com.example.shantunu.redbusassgn.apiModels.Inventory
import com.example.shantunu.redbusassgn.ui.viewHolders.EachBusHolder
import com.example.shantunu.redbusassgn.ui.viewHolders.FooterViewHolder
import com.example.shantunu.redbusassgn.ui.viewHolders.HeaderViewHolder


class RvSearchResultsAdapter (val data: MutableList<Inventory>, val context : Context,
                              private val types : LinkedHashMap<String, String>, private val travels : LinkedHashMap<String, String>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_FOOTER = 0
    private val TYPE_ITEM = 1
    private val TYPE_HEADER = 2
    var maxSize = 0
    override fun getItemCount(): Int {
        return data.size + 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is FooterViewHolder){
            if (data.size > 0) {
                maxSize = data[position - 1].maxSize!!
                if (position == (maxSize + 2)) {           // for header and footer + 2 is done
                    holder.itemView.visibility = View.GONE
                } else {
                    holder.itemView.visibility = View.VISIBLE
                }
            } else{
                holder.itemView.visibility = View.GONE
            }
        }
        else if (holder is EachBusHolder) {

            holder.tvStartTime.text = data[position - 1].startTime?.let { Utils.getTime(it) }
            holder.tvBusType.text = types[data[position - 1].bus?.type?.toString()]
            holder.tvBusCompany.text = travels[data[position - 1].bus?.travelsName?.toString()]
            holder.tvRating.text = data[position - 1].rating.toString()

            data[position - 1].rating?.let { if (it > 4) {
                holder.tvRating.background = ResourcesCompat.getDrawable(context.resources, R.drawable.rectangle_rating, null)
            }else {
                holder.tvRating.background = ResourcesCompat.getDrawable(context.resources, R.drawable.rectangle_below_five, null) }
            }

            data[position - 1].seats?.discount?.let {
                val strRedDeal = "Red Deal "
                val onlyBaseFare : String = context.getString(R.string.rs)+ " "+data[position - 1].seats?.baseFare.toString()
                val strBaseFare : String = strRedDeal+onlyBaseFare
                val spannable = SpannableString(strBaseFare)
                spannable.setSpan(StrikethroughSpan(), strRedDeal.length, strBaseFare.length     , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                holder.tvRedBusDeal.text = spannable
            }

            holder.tvNoRating.text = data[position - 1].nosRating.toString()+ " ratings"

            val seatsLeft : String = data[position - 1].seats?.seatsRemaining.toString() + " seats left"

            holder.tvSeatsLeft.text = seatsLeft

            var netFare : Int ?= data[position - 1].seats?.baseFare?.minus(data[position].seats?.discount!!)
            var strFare : String ? = context.getString(R.string.rs)+ " "+netFare.toString()

            holder.tvTotalAmt.text = strFare

            holder.tvArrivalTime.text = data[position - 1].reachesLocationIn?.let { Utils.getReachesLocationIn(it) }

            holder.tvEndTime.text = data[position - 1].duration?.let { data[position].startTime?.let { it1 ->
                Utils.getEndTime(it,
                    it1
                )
            } }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_FOOTER -> {
                val layoutView = LayoutInflater.from(parent.context).inflate(R.layout.progress_bar, parent, false)
                return FooterViewHolder(layoutView)
            }
            TYPE_ITEM -> {
                val layoutView = LayoutInflater.from(parent.context).inflate(R.layout.item_search_results, parent, false)
                return EachBusHolder(layoutView)
            }
            TYPE_HEADER -> {
                val layoutView = LayoutInflater.from(parent.context).inflate(R.layout.header_layout, parent, false)
                return HeaderViewHolder(layoutView)
            }
            else -> throw RuntimeException("No match for $viewType.")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            data.size -> TYPE_FOOTER
            0 -> TYPE_HEADER
            else -> TYPE_ITEM
        }
    }
}