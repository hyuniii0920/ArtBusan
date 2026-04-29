package com.example.artbusan

import android.content.Context

fun translateCategory(context: Context, category: String): String {
    val resId = when (category) {
        "해운대구" -> R.string.chip_haeundae
        "수영구"   -> R.string.chip_suyeong
        "남구"     -> R.string.chip_nam
        "영도구"   -> R.string.chip_yeongdo
        "중구"     -> R.string.chip_jung
        "동구"     -> R.string.chip_dong
        "부산진구" -> R.string.chip_busanjin
        "동래구"   -> R.string.chip_dongrae
        "금정구"   -> R.string.chip_geumjeong
        "기장군"   -> R.string.chip_gijang
        else       -> return category
    }
    return context.getString(resId)
}
