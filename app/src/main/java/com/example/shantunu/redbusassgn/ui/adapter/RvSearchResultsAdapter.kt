package com.example.shantunu.redbusassgn.ui.adapter

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shantunu.redbusassgn.R
import com.example.shantunu.redbusassgn.Utils
import com.example.shantunu.redbusassgn.apiModels.Inventory
import com.example.shantunu.redbusassgn.ui.viewHolders.EachBusHolder
import com.example.shantunu.redbusassgn.ui.viewHolders.FooterViewHolder


class RvSearchResultsAdapter (val data: MutableList<Inventory>, val context : Context,
                              private val types : LinkedHashMap<String, String>, private val travels : LinkedHashMap<String, String>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_FOOTER = 0
    private val TYPE_ITEM = 1
    var maxSize = 0
    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is FooterViewHolder){
            maxSize = data[position].maxSize!!
            if (position == maxSize) {
                holder.itemView.visibility = View.GONE
            } else {
                holder.itemView.visibility = View.VISIBLE
            }
        } else if (holder is EachBusHolder) {
            holder.tvStartTime.text = data[position].startTime?.let { Utils.getTime(it) }
            holder.tvBusType.text = types[data[position].bus?.type?.toString()]
            holder.tvBusCompany.text = travels[data[position].bus?.travelsName?.toString()]
            holder.tvRating.text = data[position].rating.toString()
            data[position].rating?.let { if (it > 4) {
                holder.tvRating.background = context.resources.getDrawable(R.drawable.rectangle_rating)
            }else {
                holder.tvRating.background = context.resources.getDrawable(R.drawable.rectangle_below_five) }
            }

            data[position].seats?.discount?.let {
                val strRedDeal = "Red Deal "
                val onlyBaseFare : String = context.getString(R.string.rs)+ " "+data[position].seats?.baseFare.toString()
                val strBaseFare : String = strRedDeal+onlyBaseFare
                val spannable = SpannableString(strBaseFare)
                spannable.setSpan(StrikethroughSpan(), strRedDeal.length, strBaseFare.length     , Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                holder.tvRedBusDeal.text = spannable
            }

            holder.tvNoRating.text = data[position].nosRating.toString()+ " ratings"
            val seatsLeft : String = data[position].seats?.seatsRemaining.toString() + " seats left"
            holder.tvSeatsLeft.text = seatsLeft
            var netFare : Int ?= data[position].seats?.baseFare?.minus(data[position].seats?.discount!!)
            var strFare : String ? = context.getString(R.string.rs)+ " "+netFare.toString()
            holder.tvTotalAmt.text = strFare
            holder.tvArrivalTime.text = data[position].reachesLocationIn?.let { Utils.getReachesLocationIn(it) }
            holder.tvEndTime.text = data[position].duration?.let { data[position].startTime?.let { it1 ->
                Utils.getEndTime(it,
                    it1
                )
            } }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_FOOTER) {
            val layoutView = LayoutInflater.from(parent.context).inflate(R.layout.progress_bar, parent, false)
            return FooterViewHolder(layoutView)
        } else if (viewType == TYPE_ITEM) {
            val layoutView = LayoutInflater.from(parent.context).inflate(R.layout.item_search_results, parent, false)
            return EachBusHolder(layoutView)
        }
        throw RuntimeException("No match for $viewType.")
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == data.size - 1){
            TYPE_FOOTER
        } else{
            TYPE_ITEM
        }
    }
}