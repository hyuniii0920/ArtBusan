package com.example.artbusan.network

import retrofit2.http.GET
import retrofit2.http.Path

interface ArtarApiService {
    @GET("works/{id}")
    suspend fun getArtwork(@Path("id") id: Int): ArtworkResponse
}
