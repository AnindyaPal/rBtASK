package com.example.shantunu.redbusassgn.ui.activities

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.shantunu.redbusassgn.R
import com.example.shantunu.redbusassgn.Utils
import com.example.shantunu.redbusassgn.Utils.Companion.getDialog
import com.example.shantunu.redbusassgn.Utils.Companion.hideProgressBar
import com.example.shantunu.redbusassgn.Utils.Companion.isDisplayedOnce
import com.example.shantunu.redbusassgn.Utils.Companion.showProgressBar
import com.example.shantunu.redbusassgn.Utils.Companion.showToolTip
import com.example.shantunu.redbusassgn.apiModels.Inventory
import com.example.shantunu.redbusassgn.ui.adapter.RvDurationAdapter
import com.example.shantunu.redbusassgn.ui.adapter.RvFareFilterAdapter
import com.example.shantunu.redbusassgn.ui.adapter.RvSearchResultsAdapter
import com.example.shantunu.redbusassgn.ui.scrollListeners.HidingScrollListener
import com.example.shantunu.redbusassgn.viewModel.GetAllResultsViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import it.sephiroth.android.library.xtooltip.Tooltip
import kotlinx.android.synthetic.main.activity_search_results_actv.*
import kotlinx.android.synthetic.main.bottom_sheet_filter.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext

class SearchResultsActv : AppCompatActivity(), CoroutineScope , LifecycleOwner {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val job : Job = Job()

    val searchResults : MutableList<Inventory> = mutableListOf()
    val searchResultsCopy : MutableList<Inventory> = mutableListOf()
    var listForPagination : MutableList<Inventory> = mutableListOf()

    val types : LinkedHashMap<String, String> = LinkedHashMap()
    val travels : LinkedHashMap<String, String> = LinkedHashMap()
    var rvDataAdapter : RvSearchResultsAdapter ? =  null

    var getDataViewModel : GetAllResultsViewModel ?= null

    var farePosition : Int = 0
    var durationPosition : Int = 0

    var isSortByFare : Boolean = false
    var isSortByDuration : Boolean = false

    var tooltipShown = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_results_actv)
        initMembers()
    }

    private fun initMembers() {
        title = "Bus leaving in 1 hour"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        rvDataAdapter = RvSearchResultsAdapter(searchResults, this, types, travels)
        rvData.adapter = rvDataAdapter
        rvData.layoutManager = LinearLayoutManager(this)

        showProgressBar(progressBar)

        getDataViewModel = ViewModelProviders.of(this).get(GetAllResultsViewModel::class.java)
        getDataViewModel?.liveData?.observe(this, Observer { fullModel ->
            fullModel ?.let {

                hideProgressBar(progressBar)

                searchResultsCopy.clear()
                searchResultsCopy.addAll(Utils.getInventoryListWithNetFare(fullModel.inventory))

                listForPagination.clear()
                listForPagination.addAll(Utils.getInventoryListWithNetFare(fullModel.inventory))

                types.clear()
                types.putAll(fullModel.busType)
                travels.clear()
                travels.putAll(fullModel.travels)

                paginateAndNotifyAdapter()

            } ?: kotlin.run {
                hideProgressBar(progressBar)
                showSnackbar()
            }
        })
        getDataViewModel?.getAllResults()
        paginateRecyclerView()

    }

    private fun paginateRecyclerView() {
        rvData.addOnScrollListener(object : HidingScrollListener(){
            override fun onHide() {
                hideViews()
            }

            override fun onShow() {
                showViews()
            }

            override fun paginate() {
                paginateAndNotifyAdapter()
            }

        })
    }

    private fun showViews() {
        vDestinaltion.animate().translationY(0.0f).interpolator = DecelerateInterpolator(2.0f)
    }

    private fun hideViews() {
        vDestinaltion.animate().translationY((-vDestinaltion.height).toFloat()).interpolator = AccelerateInterpolator(2.0f)
    }

    private fun paginateAndNotifyAdapter() {

        var counter = 0
        for (i in searchResults.size until listForPagination.size) {
            if (counter <=9 ){
                searchResults.add(listForPagination[i])
                counter += 1
            }
            else {
                break
            }
        }
        if (searchResults.size == 0) {
            vEmptyView.visibility = View.VISIBLE
        } else {
            vEmptyView.visibility = View.GONE
            searchResults[searchResults.size - 1].maxSize = listForPagination.size - 1
        }

        Handler().postDelayed( { rvDataAdapter?.notifyDataSetChanged() }, 300 )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.filter -> {
                initBottomSheetFilter()
                val view = findViewById<View>(R.id.filter)
                if (!isDisplayedOnce(tooltipShown))
                    showToolTip(view, "Tap to filter", Tooltip.Gravity.BOTTOM, this)
            }
            R.id.sort -> {
                showSortDialog()
                val view = findViewById<View>(R.id.sort)
                if (!isDisplayedOnce(tooltipShown))
                    showToolTip(view, "Tap to sort", Tooltip.Gravity.BOTTOM, this)
            }
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private fun showSortDialog() {
        val dialogSort = getDialog(this, R.layout.sort_layout)
        val swFare = dialogSort.findViewById<View>(R.id.swFare) as Switch
        val swDuration = dialogSort.findViewById<View>(R.id.swDuration)as Switch
        val btnApplySort = dialogSort.findViewById<View>(R.id.btnSort)

        swFare.isChecked = isSortByFare
        swDuration.isChecked = isSortByDuration

        swFare.setOnCheckedChangeListener { _, b ->
            if (b) {
                isSortByFare = true
                swDuration.isChecked = false
            } else {
                isSortByFare = false
            }

        }
        swDuration.setOnCheckedChangeListener { _, b ->
            if (b) {
                isSortByDuration = true
                swFare.isChecked = false
            } else {
                isSortByDuration = false
            }
        }

        btnApplySort.setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(it.context, R.anim.button_click_shrink))
            dialogSort.dismiss()
            performSort()
            searchResults.clear()
            paginateAndNotifyAdapter()
        }

        dialogSort.show()
    }

    private fun performSort() {
        if (isSortByFare){
            listForPagination.sortWith(Comparator { o1, o2 -> o1.netFare?.compareTo(o2.netFare!!)!! })
        } else if (isSortByDuration){
            listForPagination.sortWith(kotlin.Comparator { o1, o2 -> o2.duration?.let { o1.duration?.compareTo(it) }!! })
        }
    }

    private fun initBottomSheetFilter() {

        val filterSheetBehavior = BottomSheetBehavior.from(bottomDialogFilter)
        rvFareFilter.adapter = RvFareFilterAdapter(Utils.getFareRange(farePosition)){ position: Int -> farePosition = position }
        rvFareFilter.layoutManager = LinearLayoutManager(this)

        rvDurationFilter.adapter = RvDurationAdapter(Utils.getDurationRange(durationPosition)) { position: Int -> durationPosition = position }
        rvDurationFilter.layoutManager = LinearLayoutManager(this)

        btnApplyFilter.setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(it.context, R.anim.button_click_shrink))
            filterSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            showProgressBar(progressBar)
            launch { performNormalFilter() }
        }

        val dialogCustomize = Utils.getDialog(this@SearchResultsActv, R.layout.custom_filter)
        setUpDialogCustomize(dialogCustomize)

        vCustomize.setOnClickListener {
            filterSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            it.startAnimation(AnimationUtils.loadAnimation(it.context, R.anim.button_click_shrink))

            dialogCustomize.show()
        }

        filterSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        filterSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(@NonNull bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    bg.visibility = View.GONE
                } else if (newState == BottomSheetBehavior.STATE_EXPANDED){

                    if (!isDisplayedOnce(tooltipShown))
                        showToolTip(vCustomize, "Customize filter options", Tooltip.Gravity.LEFT,this@SearchResultsActv)

                    vCustomize.startAnimation(AnimationUtils.loadAnimation(vCustomize.context, R.anim.rotation_anim))
                }
            }
            override fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {
                bg.visibility = View.VISIBLE
                bg.alpha = slideOffset
            }
        })
    }

    private fun setUpDialogCustomize(dialogCustomize: Dialog) {

        val etStartFare = dialogCustomize.findViewById<View>(R.id.etStartFareRange) as EditText
        val etEndFare = dialogCustomize.findViewById<View>(R.id.etEndFareRange)  as EditText
        val etStartDuration = dialogCustomize.findViewById<View>(R.id.etStartDurationRange) as EditText
        val etEndDuration = dialogCustomize.findViewById<View>(R.id.etEndDurationRange) as EditText
        val btnApply = dialogCustomize.findViewById<View>(R.id.btnSubmit) as Button

        btnApply.setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(it.context, R.anim.button_click_shrink))
            when {
                etStartFare.text.toString().isEmpty() -> etStartFare.error = "Start fare cannot be empty"
                etEndFare.text.toString().isEmpty() -> etEndFare.error = "End fare cannot be empty"
                etStartDuration.text.toString().isEmpty() -> etStartDuration.error = "Start hour cannot be empty"
                etEndDuration.text.toString().isEmpty() -> etEndDuration.error = "End hour cannot be empty"
                else -> {
                    dialogCustomize.dismiss()
                    val startFare = etStartFare.text.toString().toInt()
                    val endFare = etEndFare.text.toString().toInt()
                    val startDuration = etStartDuration.text.toString().toLong()
                    val endDuration = etEndDuration.text.toString().toLong()

                   launch {  performCustomFilter(startFare, endFare,
                       startDuration * 60 , endDuration * 60) }
                }
            }
        }
    }

    suspend fun performCustomFilter(startFare : Int , endFare : Int , startDuration : Long, endDuration : Long) { // For custom filtering
        showProgressBar(progressBar)

        withContext(Dispatchers.Default) { listForPagination = Utils.filterAction(startFare,
            endFare, startDuration, endDuration, searchResultsCopy) } // asynchronous sorting

        searchResults.clear() // clear the showing list data

        performSort()
        paginateAndNotifyAdapter()
        hideProgressBar(progressBar)
    }

    suspend fun performNormalFilter(){     // For filtering using check box

        withContext(Dispatchers.Default) { listForPagination = Utils.performFilter(farePosition,
            durationPosition, searchResultsCopy) } // asynchronous sorting

        searchResults.clear() // clear the current shown data

        performSort()
        paginateAndNotifyAdapter()
        hideProgressBar(progressBar)
    }

    override fun onBackPressed() {
        val filterSheetBehavior = BottomSheetBehavior.from(bottomDialogFilter)
        if (filterSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED){
            filterSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        else{
            super.onBackPressed()
        }
    }

    private fun showSnackbar() {
        val snackbar = Snackbar.make(vRoot, "Please check internet connection" , Snackbar.LENGTH_INDEFINITE )
        snackbar.setAction("RETRY") { networkCheckAndProcess() }
        snackbar.setActionTextColor(ResourcesCompat.getColor(resources, R.color.colorAccent, null))
        snackbar.setActionTextColor(ContextCompat.getColor(this@SearchResultsActv,R.color.green))
        snackbar.show()
    }

    private fun networkCheckAndProcess() {
        if (Utils.isNetworkAvailable(this@SearchResultsActv)) {
            showSnackbar()
            getDataViewModel?.getAllResults()
        } else {
            showSnackbar()
        }
    }
}
