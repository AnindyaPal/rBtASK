package com.example.shantunu.redbusassgn.ui.adapter

import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shantunu.redbusassgn.R
import com.example.shantunu.redbusassgn.Utils
import com.example.shantunu.redbusassgn.apiModels.Inventory
import com.example.shantunu.redbusassgn.ui.viewHolders.EachBusHolder


class RvSearchResultsAdapter (val data: MutableList<Inventory>, val context : Context,
                              private val types : LinkedHashMap<String, String>, private val travels : LinkedHashMap<String, String>)
    : RecyclerView.Adapter<EachBusHolder>() {

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: EachBusHolder, position: Int) {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EachBusHolder {
        val v = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_search_results, parent, false)
        return EachBusHolder(v)
    }

}