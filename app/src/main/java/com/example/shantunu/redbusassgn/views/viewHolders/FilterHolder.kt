package com.example.shantunu.redbusassgn.views.viewHolders

import android.view.View
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.shantunu.redbusassgn.R

class FilterHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val cbFilter : CheckBox = itemView.findViewById(R.id.cbFilterItem)
}