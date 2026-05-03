package com.example.artbusan.network

import com.google.gson.annotations.SerializedName

data class ArtworkResponse(
    val id: Int,
    val title: String,
    val artist: String? = null,
    @SerializedName("summary_description")
    val summaryDescription: String? = null,
    @SerializedName("short_description")
    val shortDescription: String? = null,
    @SerializedName("detail_description")
    val detailDescription: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("image_url")
    val imageUrl: String? = null,
    @SerializedName("ar_asset_url")
    val arAssetUrl: String? = null
)

fun ArtworkResponse.toArtworkExperience(): ArtworkExperience {
    val summary = summaryDescription ?: shortDescription ?: description ?: "작품 설명이 아직 등록되지 않았습니다."
    val detail = detailDescription ?: description ?: summary

    return ArtworkExperience(
        id = id,
        title = title,
        artist = artist ?: "Unknown Artist",
        summaryDescription = summary,
        detailDescription = detail,
        imageUrl = imageUrl.orEmpty(),
        arAssetUrl = arAssetUrl
    )
}
