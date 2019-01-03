package com.example.shantunu.redbusassgn.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.shantunu.redbusassgn.apiModels.FullModel
import com.example.shantunu.redbusassgn.networkRetrofit.RetrofitService
import com.example.shantunu.redbusassgn.repository.SearchResultsRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.coroutines.CoroutineContext

class GetAllResultsViewModel : ViewModel(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val job : Job = Job()
    val liveData : MutableLiveData<FullModel> = MutableLiveData()

    fun getAllResults(){
      launch { performGet() }
    }

    suspend fun performGet(){
        val  searchResultsRepo : SearchResultsRepo = RetrofitService().getService()
        try {
            val fullModel : FullModel = searchResultsRepo.getAllData().await()
            liveData.value = fullModel
        }
        catch (e : Exception){
            liveData.value = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}