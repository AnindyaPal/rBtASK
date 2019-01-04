package com.example.shantunu.redbusassgn.ui.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
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
import com.example.shantunu.redbusassgn.viewModel.GetAllResultsViewModel
import com.example.shantunu.redbusassgn.ui.adapter.RvDurationAdapter
import com.example.shantunu.redbusassgn.ui.adapter.RvFareFilterAdapter
import com.example.shantunu.redbusassgn.ui.adapter.RvSearchResultsAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_search_results_actv.*
import kotlinx.android.synthetic.main.bottom_sheet_filter.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import android.widget.ProgressBar
import android.view.animation.DecelerateInterpolator
import android.animation.ObjectAnimator

class SearchResultsActv : AppCompatActivity(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val job : Job = Job()

    val searchResults : MutableList<Inventory> = mutableListOf()
    val searchResultsCopy : MutableList<Inventory> = mutableListOf()

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
                searchResults.addAll(fullModel.inventory)
                searchResultsCopy.clear()
                searchResultsCopy.addAll(fullModel.inventory)
                types.clear()
                types.putAll(fullModel.busType)
                travels.clear()
                travels.putAll(fullModel.travels)
                rvDataAdapter?.notifyDataSetChanged()
            } ?: kotlin.run {
                hideProgressBar()
                showConnectivityDialog()
            }
        })
        getDataViewModel?.getAllResults()
    }

    private fun hideProgressBar() {
        progressBar.visibility = View.GONE
    }

    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
        setProgressMax(progressBar, 1000)
        setProgressAnimate(progressBar, 100)
    }

    private fun setProgressMax(pb: ProgressBar, max: Int) {
        pb.max = max * 100
    }

    private fun setProgressAnimate(pb: ProgressBar, progressTo: Int) {
        val animation = ObjectAnimator.ofInt(pb, "progress", pb.progress, progressTo * 100)
        animation.duration = 100
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
                initBottomSheetFilter()
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
        withContext(Dispatchers.Default) {searchResults.addAll(Utils.getFullFilteredInventory(farePosition, durationPosition, searchResultsCopy))}
        rvDataAdapter?.notifyDataSetChanged()
        hideProgressBar()
    }
}
