package com.example.artbusan.ar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class MockArtworkRegistryTest {

    @Test
    fun workOneReturnsLocalMockArtwork() {
        val artwork = MockArtworkRegistry.findById(1)

        assertNotNull(artwork)
        assertEquals("테스트용 작품 1", artwork?.title)
        assertEquals("테스트용 작품입니다.", artwork?.summaryDescription)
    }

    @Test
    fun workTwoReturnsLocalMockArtwork() {
        val artwork = MockArtworkRegistry.findById(2)

        assertNotNull(artwork)
        assertEquals("테스트용 작품 2", artwork?.title)
        assertEquals("테스트용 작품입니다.", artwork?.summaryDescription)
    }

    @Test
    fun otherArtworkIdsAreNotIntercepted() {
        assertNull(MockArtworkRegistry.findById(3))
    }
}
