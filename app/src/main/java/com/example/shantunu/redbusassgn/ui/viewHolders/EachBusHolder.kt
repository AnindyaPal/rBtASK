package com.example.shantunu.redbusassgn.ui.viewHolders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shantunu.redbusassgn.R

class EachBusHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var tvStartTime : TextView
    var tvEndTime : TextView
    var tvBusCompany : TextView
    var tvBusType : TextView
    var tvTotalAmt : TextView
    var tvArrivalTime : TextView
    var tvRating : TextView
    var tvNoRating : TextView
    var tvSeatsLeft : TextView
    var tvRedBusDeal : TextView
    /*@BindView(R.id.tvSeatsLeft)
    lateinit var tvSeatsLeft : TextView*/

    init {
        tvStartTime = itemView.findViewById(R.id.tvStartTime)
        tvEndTime = itemView.findViewById(R.id.tvEndTime)
        tvBusCompany = itemView.findViewById(R.id.tvBusCompany)
        tvBusType = itemView.findViewById(R.id.tvBusName)
        tvTotalAmt = itemView.findViewById(R.id.tvTotalAmt)
        tvArrivalTime = itemView.findViewById(R.id.tvArrivalTime)
        tvRating = itemView.findViewById(R.id.tvRating)
        tvNoRating = itemView.findViewById(R.id.tvNoRating)
        tvSeatsLeft = itemView.findViewById(R.id.tvSeatsLeft)
        tvRedBusDeal = itemView.findViewById(R.id.tvRedBusDeal)
    }
}