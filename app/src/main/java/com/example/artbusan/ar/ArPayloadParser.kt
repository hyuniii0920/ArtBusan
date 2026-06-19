package com.example.artbusan.ar

object ArPayloadParser {

    private val markerNamePattern = Regex("""^work:(\d+)$""", RegexOption.IGNORE_CASE)
    private val queryIdPattern = Regex("""(?:[?&])id=(\d+)(?:$|[&#])""", RegexOption.IGNORE_CASE)
    private val pathIdPattern = Regex("""(?:^|[:/])(?:work|artwork|venue)/(\d+)(?:$|[/?#])""", RegexOption.IGNORE_CASE)
    private val numberPattern = Regex("""^\d+$""")

    fun parseArtworkId(rawValue: String?): Int? {
        val value = rawValue?.trim()?.takeIf { it.isNotBlank() } ?: return null

        markerNamePattern.find(value)?.let { return it.groupValues[1].toPositiveIntOrNull() }
        queryIdPattern.find(value)?.let { return it.groupValues[1].toPositiveIntOrNull() }
        pathIdPattern.find(value)?.let { return it.groupValues[1].toPositiveIntOrNull() }

        if (numberPattern.matches(value)) {
            return value.toPositiveIntOrNull()
        }

        return null
    }

    private fun String.toPositiveIntOrNull(): Int? {
        return toIntOrNull()?.takeIf { it > 0 }
    }
}
