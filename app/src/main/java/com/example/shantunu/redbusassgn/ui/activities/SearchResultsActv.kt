package com.example.shantunu.redbusassgn.ui.activities

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import butterknife.ButterKnife
import com.example.shantunu.redbusassgn.R
import com.example.shantunu.redbusassgn.Utils
import com.example.shantunu.redbusassgn.apiModels.Inventory
import com.example.shantunu.redbusassgn.ui.adapter.RvDurationAdapter
import com.example.shantunu.redbusassgn.ui.adapter.RvFareFilterAdapter
import com.example.shantunu.redbusassgn.ui.adapter.RvSearchResultsAdapter
import com.example.shantunu.redbusassgn.viewModel.GetAllResultsViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_search_results_actv.*
import kotlinx.android.synthetic.main.bottom_sheet_filter.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import androidx.recyclerview.widget.RecyclerView
import it.sephiroth.android.library.xtooltip.Tooltip
import com.example.shantunu.redbusassgn.R.id.textView
import android.R.attr.fadeDuration





class SearchResultsActv : AppCompatActivity(), CoroutineScope {
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
        ButterKnife.bind(this)
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
                paginate()

                types.clear()
                types.putAll(fullModel.busType)
                travels.clear()
                travels.putAll(fullModel.travels)
                rvDataAdapter?.notifyDataSetChanged()
                paginateRecyclerView()
            } ?: kotlin.run {
                hideProgressBar()
                showConnectivityDialog()
            }
        })
        getDataViewModel?.getAllResults()
    }

    private fun paginateRecyclerView() {
        rvData.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    paginate()
                }
            }
        })
    }

    private fun paginate() {
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
        searchResults[searchResults.size - 1].maxSize = listForPagination.size - 1
        Handler().postDelayed(Runnable {  rvDataAdapter?.notifyDataSetChanged() }, 1000)
    }

    private fun hideProgressBar() {
        Handler().postDelayed(Runnable { progressBar.visibility = View.GONE }, 500)
    }

    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
        setProgressMax(progressBar, 1000)
        setProgressAnimate(progressBar, 10)
    }

    private fun setProgressMax(pb: ProgressBar, max: Int) {
        pb.max = max * 100
    }

    private fun setProgressAnimate(pb: ProgressBar, progressTo: Int) {
        val animation = ObjectAnimator.ofInt(pb, "progress", pb.progress, progressTo * 100)
        animation.duration = 500
        animation.interpolator = DecelerateInterpolator()
        animation.start()
    }

    private fun showConnectivityDialog() {
        Toast.makeText(this, "Please check internet connection !", Toast.LENGTH_LONG).show()
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
        paginate()
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
}
