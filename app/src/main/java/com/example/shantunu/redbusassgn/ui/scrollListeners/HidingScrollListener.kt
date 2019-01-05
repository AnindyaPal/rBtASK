package com.example.shantunu.redbusassgn.ui.scrollListeners

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

abstract class HidingScrollListener : RecyclerView.OnScrollListener() {
    private var scrolledDistance = 0
    private var controlsVisible = true

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        val firstVisibleItem = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        if (firstVisibleItem == 0) {
            if(!controlsVisible) {
                onShow()
                controlsVisible = true
            }
        } else {
            if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
                onHide()
                controlsVisible = false
                scrolledDistance = 0
            } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
                onShow()
                controlsVisible = true
                scrolledDistance = 0
            }
        }

        if((controlsVisible && dy>0) || (!controlsVisible && dy<0)) {
            scrolledDistance += dy
        }

        if (!recyclerView.canScrollVertically(1)) {
            paginate()
        }
    }

    abstract fun onHide()
    abstract fun onShow()
    abstract fun paginate()

    companion object {
        private const val HIDE_THRESHOLD = 40
    }

}