package com.example.artbusan.network

import com.example.artbusan.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ArtarApiClient {
    val service: ArtarApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.ARTAR_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ArtarApiService::class.java)
    }
}
