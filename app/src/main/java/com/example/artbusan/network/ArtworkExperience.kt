package com.example.artbusan.network

data class ArtworkExperience(
    val id: Int,
    val title: String,
    val artist: String,
    val summaryDescription: String,
    val detailDescription: String,
    val imageUrl: String,
    val arAssetUrl: String?,
    val markerImageUrl: String? = null,
    val markerWidthMeters: Float? = null,
    val mediaUrl: String? = null
)
