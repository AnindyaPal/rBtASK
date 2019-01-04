package com.example.shantunu.redbusassgn.views.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.shantunu.redbusassgn.R
import com.example.shantunu.redbusassgn.apiModels.EachRange
import com.example.shantunu.redbusassgn.views.viewHolders.FilterHolder

class RvFareFilterAdapter(val fareRange : MutableList<EachRange> ,
                          val setRangePostion :(positon : Int)-> Unit ): RecyclerView.Adapter<FilterHolder>() {

    var context : Context ? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterHolder {
        val v = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_filter, parent, false)
        context = parent.context
        return FilterHolder(v)
    }

    override fun getItemCount(): Int {
        return fareRange.size
    }

    override fun onBindViewHolder(holder: FilterHolder, position: Int) {
        if (position != 0)
            holder.cbFilter.text = context?.getString(R.string.rs)+" " + fareRange[position].range
        else
            holder.cbFilter.text = fareRange[position].range
        holder.cbFilter.isChecked = fareRange[position].isChecked

        holder.cbFilter.setOnClickListener {
            for (fares in fareRange){
                fares.isChecked = false
            }
            fareRange[position].isChecked = true
            setRangePostion(position)
            notifyDataSetChanged()
        }
    }

}