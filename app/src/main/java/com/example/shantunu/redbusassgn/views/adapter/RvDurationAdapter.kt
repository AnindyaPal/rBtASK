package com.example.shantunu.redbusassgn.views.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shantunu.redbusassgn.R
import com.example.shantunu.redbusassgn.apiModels.EachRange
import com.example.shantunu.redbusassgn.views.viewHolders.FilterHolder

class RvDurationAdapter (val durationRange : MutableList<EachRange>,
                         val setRangePostion :(positon : Int)-> Unit ): RecyclerView.Adapter<FilterHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterHolder {
        val v = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_filter, parent, false)
        return FilterHolder(v)
    }

    override fun getItemCount(): Int {
        return durationRange.size
    }

    override fun onBindViewHolder(holder: FilterHolder, position: Int) {

        if (position != 0) {
            holder.cbFilter.text = durationRange[position].range+" hours"
        }
        else {
            holder.cbFilter.text = durationRange[position].range
        }

        holder.cbFilter.isChecked = durationRange[position].isChecked

        holder.cbFilter.setOnClickListener {
            for (fares in durationRange){
                fares.isChecked = false
            }
            durationRange[position].isChecked = true
            setRangePostion(position)
            notifyDataSetChanged()
        }
    }

}