package com.example.artbusan.ar

import com.example.artbusan.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ArMarkerRegistryTest {

    @Test
    fun demoMarkerMapsToArtworkOne() {
        val marker = ArMarkerRegistry.findByName("work:1")

        assertNotNull(marker)
        assertEquals(1, marker?.artworkId)
        assertEquals(R.drawable.test_poster, marker?.markerDrawableRes)
        assertEquals(0.21f, marker?.markerWidthMeters ?: 0f, 0.0001f)
    }

    @Test
    fun findByArtworkIdReturnsDemoMarker() {
        val marker = ArMarkerRegistry.findByArtworkId(1)

        assertNotNull(marker)
        assertEquals("work:1", marker?.name)
    }
}
