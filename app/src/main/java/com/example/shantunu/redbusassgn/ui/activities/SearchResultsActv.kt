package com.example.shantunu.redbusassgn.ui.activities

import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
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

        showProgressBar()

        getDataViewModel = ViewModelProviders.of(this).get(GetAllResultsViewModel::class.java)
        getDataViewModel?.liveData?.observe(this, Observer { fullModel ->
            fullModel ?.let {
                hideProgressBar()
                searchResults.clear()

                searchResultsCopy.clear()
                searchResultsCopy.addAll(fullModel.inventory)

                listForPagination.clear()
                listForPagination.addAll(fullModel.inventory)
                paginateAndNotifyAdapter()

                types.clear()
                types.putAll(fullModel.busType)
                travels.clear()
                travels.putAll(fullModel.travels)

                paginateRecyclerView()
            } ?: kotlin.run {
                hideProgressBar()
                showSnackbar()
            }
        })
        getDataViewModel?.getAllResults()
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
            Handler().postDelayed(Runnable {  vEmptyView.visibility = View.VISIBLE }, 500)
        } else {
            vEmptyView.visibility = View.GONE
            searchResults[searchResults.size - 1].maxSize = listForPagination.size - 1
        }
        Handler().postDelayed(Runnable {  rvDataAdapter?.notifyDataSetChanged() }, 500)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.filter -> {
                val view = findViewById<View>(R.id.filter)
                initBottomSheetFilter()
                val tooltip = Tooltip.Builder(this@SearchResultsActv)
                    .anchor(view, 0, 0, true)
                    .text("Tap to apply filter")
                    .arrow(true)
                    .floatingAnimation(Tooltip.Animation.DEFAULT)
                    .showDuration(2000)
                    .fadeDuration(300)
                    .overlay(true)
                    .create()

                tooltip
                    .doOnFailure {  }
                    .show(view, Tooltip.Gravity.BOTTOM, true)
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

    private fun initBottomSheetFilter() {
        val filterSheetBehavior = BottomSheetBehavior.from(bottomDialogFilter)
        rvFareFilter.adapter = RvFareFilterAdapter(Utils.getFareRange(farePosition)){ position: Int -> farePosition = position }
        rvFareFilter.layoutManager = LinearLayoutManager(this)

        rvDurationFilter.adapter = RvDurationAdapter(Utils.getDurationRange(durationPosition)) { position: Int -> durationPosition = position }
        rvDurationFilter.layoutManager = LinearLayoutManager(this)

        btnApplyFilter.setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(it.context, R.anim.button_click_shrink))
            filterSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            showProgressBar()
            launch { performFilter() }
        }

        filterSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        filterSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(@NonNull bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    bg.visibility = View.GONE
                }
            }
            override fun onSlide(@NonNull bottomSheet: View, slideOffset: Float) {
                bg.visibility = View.VISIBLE
                bg.alpha = slideOffset
            }
        })
    }

    suspend fun performFilter(){
        searchResults.clear()
        withContext(Dispatchers.Default) { listForPagination = Utils.getFullFilteredInventory(farePosition, durationPosition, searchResultsCopy) }
        paginateAndNotifyAdapter()
        hideProgressBar()
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

    private fun hideProgressBar() {
        Handler().postDelayed(Runnable { progressBar.visibility = View.GONE }, 500)
    }

    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
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
