package com.example.shantunu.redbusassgn

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.example.shantunu.redbusassgn.apiModels.EachRange
import com.example.shantunu.redbusassgn.apiModels.Inventory
import it.sephiroth.android.library.xtooltip.Tooltip
import kotlinx.android.synthetic.main.activity_search_results_actv.*
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

class Utils {

    companion object {
        fun getTime(dateStr: String): String {
//        val date = LocalDate.parse("9999-12-31")
            val df = SimpleDateFormat("yyyy-dd-yy'T'hh:mm:ss")
            return SimpleDateFormat("hh:mm aa").format(df.parse(dateStr))
        }

        fun getReachesLocationIn(minutes: Long): String {
            val mins = minutes / 60
            val secs = minutes % 60
            return if (minutes >= 60) {
                mins.toString() + " mins " + secs + " secs"
            } else {
                secs.toString() + " seconds"
            }
        }

        fun getEndTime(duration: Long, startTime: String): String {
            val df = SimpleDateFormat("yyyy-dd-yy'T'hh:mm:ss")
            val date = df.parse(startTime).time + TimeUnit.MINUTES.toMillis(duration)
            return SimpleDateFormat("hh:mm aa").format(date)
        }

        fun getFareRange(farePostion: Int): MutableList<EachRange> {
            val fareRange = mutableListOf<EachRange>()
            when(farePostion) {
                0 -> {
                    fareRange.add(EachRange("None", true))
                    fareRange.add(EachRange("0 - 500", false))
                    fareRange.add(EachRange("500 - 1k", false))
                    fareRange.add(EachRange("1k - 1.5", false))
                }
                1 -> {
                    fareRange.add(EachRange("None", false))
                    fareRange.add(EachRange("0 - 500", true))
                    fareRange.add(EachRange("500 - 1k", false))
                    fareRange.add(EachRange("1k - 1.5", false))
                }
                2 -> {
                    fareRange.add(EachRange("None", false))
                    fareRange.add(EachRange("0 - 500", false))
                    fareRange.add(EachRange("500 - 1k", true))
                    fareRange.add(EachRange("1k - 1.5", false))
                }
                3 -> {
                    fareRange.add(EachRange("None", false))
                    fareRange.add(EachRange("0 - 500", false))
                    fareRange.add(EachRange("500 - 1k", false))
                    fareRange.add(EachRange("1k - 1.5", true))
                }
            }
            return fareRange
        }

        fun getDurationRange(durationPosition: Int): MutableList<EachRange> {
            val durationRange = mutableListOf<EachRange>()
            when(durationPosition) {
                0 -> {
                    durationRange.add(EachRange("None", true))
                    durationRange.add(EachRange("0 - 5", false))
                    durationRange.add(EachRange("5 - 10", false))
                    durationRange.add(EachRange("10 - 15", false))
                }
                1 -> {
                    durationRange.add(EachRange("None", false))
                    durationRange.add(EachRange("0 - 5", true))
                    durationRange.add(EachRange("5 - 10", false))
                    durationRange.add(EachRange("10 - 15", false))
                }
                2 -> {
                    durationRange.add(EachRange("None", false))
                    durationRange.add(EachRange("0 - 5", false))
                    durationRange.add(EachRange("5 - 10", true))
                    durationRange.add(EachRange("10 - 15", false))
                }
                3 -> {
                    durationRange.add(EachRange("None", false))
                    durationRange.add(EachRange("0 - 5", false))
                    durationRange.add(EachRange("5 - 10", false))
                    durationRange.add(EachRange("10 - 15", true))
                }
            }
            return durationRange
        }

        fun performFilter(farePostion: Int, durationPosition: Int, searchResultsCopy: MutableList<Inventory>): MutableList<Inventory> {

            var startFare = 0
            var endFare = 0
            var startDuration : Long = 0
            var endDuration : Long = 0

            when(farePostion){
                0-> {
                    startFare = 0
                    endFare = 10000
                }
                1-> {
                    startFare = 0
                    endFare = 500
                }
                2 -> {
                    startFare = 500
                    endFare = 1000
                }
                3 -> {
                    startFare = 1000
                    endFare = 1500
                }
            }

            when(durationPosition){
                0 -> {
                    startDuration = 0
                    endDuration = 10000
                }
                1 -> {
                    startDuration = 0
                    endDuration = 300
                }
                2 -> {
                    startDuration = 300
                    endDuration = 600
                }
                3 -> {
                    startDuration = 600
                    endDuration = 900
                }
            }

            return filterAction(startFare, endFare, startDuration, endDuration, searchResultsCopy)
        }

        fun filterAction(startFare : Int, endFare : Int, startDuration : Long,
                         endDuration : Long, searchResultsCopy : MutableList<Inventory>): MutableList<Inventory>{

            val inventories : MutableList<Inventory> = mutableListOf()
            for (inventory in searchResultsCopy) {
                if (inventory.netFare in startFare..endFare && inventory.duration in startDuration..endDuration)
                    inventories.add(inventory)
            }
            return inventories
        }

        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }

        fun getDialog(context: Context, resourceId : Int): Dialog {
            val dialog = Dialog(context)
            dialog.setContentView(resourceId)

            if (dialog.window != null) {
                dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.window!!.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            return dialog
        }

        fun showProgressBar(progressBar : ProgressBar) {
            progressBar.visibility = View.VISIBLE
        }

        fun hideProgressBar(progressBar : ProgressBar) {
            Handler().postDelayed(kotlinx.coroutines.Runnable { progressBar.visibility = View.GONE }, 500)
        }

        fun showToolTip(view: View, message: String, gravity : Tooltip.Gravity, context : Context) {
            val tooltip = Tooltip.Builder(context)
                .anchor(view, 0, 0, true)
                .text(message)
                .arrow(true)
                .floatingAnimation(Tooltip.Animation.DEFAULT)
                .showDuration(2000)
                .fadeDuration(300)
                .overlay(true)
                .create()

            tooltip
                .doOnFailure {  }
                .show(view, gravity, true)
        }

        fun getInventoryListWithNetFare(inventoryList : MutableList<Inventory>) : MutableList<Inventory> {
            for (inventory in inventoryList) {
                inventory.netFare = inventory.seats?.baseFare?.minus(inventory.seats?.discount!!)
            }
            return inventoryList
        }

        fun isDisplayedOnce(tooltipShown : Int): Boolean {
            return tooltipShown > 0
        }
    }
}