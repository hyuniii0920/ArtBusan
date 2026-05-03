package com.example.artbusan.network

import android.content.Context
import com.example.artbusan.data.Museum
import com.example.artbusan.data.MuseumDatabase
import com.example.artbusan.data.MuseumRepository

class ArtworkExperienceRepository(context: Context) {

    private val localRepository = MuseumRepository(
        MuseumDatabase.getInstance(context).museumDao(),
        context
    )

    suspend fun getArtwork(id: Int): ArtworkExperience {
        return runCatching {
            ArtarApiClient.service.getArtwork(id).toArtworkExperience()
        }.getOrElse {
            localRepository.getById(id)?.toArtworkExperience() ?: throw it
        }
    }

    private fun Museum.toArtworkExperience(): ArtworkExperience {
        val summary = when (id) {
            1 -> "부산현대미술관의 대표 전시 공간으로, 자연과 도시를 연결하는 미디어아트 감상을 제공합니다."
            else -> description.split(".").firstOrNull()?.trim()?.takeIf { it.isNotBlank() }?.plus(".") ?: description
        }

        val detail = when (id) {
            1 -> "부산현대미술관은 낙동강 하구의 생태 환경과 현대 시각예술을 함께 경험할 수 있도록 기획된 공간입니다. 테스트용 QR 1번 작품은 관람객이 입장 직후 작품의 맥락을 짧게 이해하고, 상세보기에서 전시 배경과 감상 포인트를 이어서 확인하는 흐름을 보여주기 위해 준비되었습니다. 실제 운영 단계에서는 이 영역에 작가 소개, 작품의 제작 의도, 관람 동선 안내, AR 연계 포인트를 함께 담을 수 있습니다."
            else -> description
        }

        return ArtworkExperience(
            id = id,
            title = title,
            artist = "ArtAR Busan",
            summaryDescription = summary,
            detailDescription = detail,
            imageUrl = imageUrl,
            arAssetUrl = null
        )
    }
}
