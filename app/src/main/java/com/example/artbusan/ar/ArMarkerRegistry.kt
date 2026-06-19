package com.example.artbusan.ar

import androidx.annotation.DrawableRes
import com.example.artbusan.R

data class ArMarker(
    val name: String,
    val artworkId: Int,
    @param:DrawableRes val markerDrawableRes: Int,
    val markerWidthMeters: Float
)

object ArMarkerRegistry {

    private const val DEMO_MARKER_WIDTH_METERS = 0.21f

    private val markers = listOf(
        ArMarker(
            name = "work:1",
            artworkId = 1,
            markerDrawableRes = R.drawable.test_poster,
            markerWidthMeters = DEMO_MARKER_WIDTH_METERS
        )
    )

    fun all(): List<ArMarker> = markers

    fun findByName(name: String?): ArMarker? {
        return markers.firstOrNull { it.name.equals(name?.trim(), ignoreCase = true) }
    }

    fun findByArtworkId(artworkId: Int): ArMarker? {
        return markers.firstOrNull { it.artworkId == artworkId }
    }
}
