package com.example.shantunu.redbusassgn.repository

import com.example.shantunu.redbusassgn.apiModels.FullModel
import kotlinx.coroutines.Deferred
import retrofit2.http.GET

interface SearchResultsRepo {
    @GET("/test/generated.json")
    fun getAllData() : Deferred<FullModel>
}