package com.example.artbusan.ar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ArPayloadParserTest {

    @Test
    fun parseArtworkId_acceptsMarkerName() {
        assertEquals(1, ArPayloadParser.parseArtworkId("work:1"))
    }

    @Test
    fun parseArtworkId_acceptsArtarUri() {
        assertEquals(1, ArPayloadParser.parseArtworkId("artar://work/1"))
        assertEquals(2, ArPayloadParser.parseArtworkId("artar://work/2"))
    }

    @Test
    fun parseArtworkId_acceptsUrlQueryId() {
        assertEquals(1, ArPayloadParser.parseArtworkId("https://example.com/artwork?id=1&utm=demo"))
    }

    @Test
    fun parseArtworkId_acceptsPlainNumber() {
        assertEquals(1, ArPayloadParser.parseArtworkId("1"))
    }

    @Test
    fun parseArtworkId_rejectsInvalidValues() {
        assertNull(ArPayloadParser.parseArtworkId("invalid"))
        assertNull(ArPayloadParser.parseArtworkId("work:0"))
        assertNull(ArPayloadParser.parseArtworkId(null))
    }
}
