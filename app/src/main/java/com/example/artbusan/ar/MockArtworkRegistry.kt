package com.example.artbusan.ar

import com.example.artbusan.network.ArtworkExperience

object MockArtworkRegistry {

    private val artworks = mapOf(
        1 to ArtworkExperience(
            id = 1,
            title = "테스트용 작품 1",
            artist = "ArtAR Busan Demo",
            summaryDescription = "테스트용 작품입니다.",
            detailDescription = "artar://work/1 QR 동작을 확인하기 위한 첫 번째 mock 작품입니다. 서버 연결 없이 앱에 하드코딩된 데이터가 AR 카메라 화면 위 작품 패널에 표시됩니다.",
            imageUrl = "",
            arAssetUrl = null
        ),
        2 to ArtworkExperience(
            id = 2,
            title = "테스트용 작품 2",
            artist = "ArtAR Busan Demo",
            summaryDescription = "테스트용 작품입니다.",
            detailDescription = "artar://work/2 QR 동작을 확인하기 위한 두 번째 mock 작품입니다. 서버 연결 없이 앱에 하드코딩된 데이터가 AR 카메라 화면 위 작품 패널에 표시됩니다.",
            imageUrl = "",
            arAssetUrl = null
        )
    )

    fun findById(artworkId: Int): ArtworkExperience? = artworks[artworkId]
}
