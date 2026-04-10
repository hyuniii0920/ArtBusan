package com.example.artbusan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class ArtworkDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_artwork_detail, container, false)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<View>(R.id.fabCreate)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<View>(R.id.fabCreate)?.visibility = View.VISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = arguments?.getString("title") ?: ""
        val category = arguments?.getString("category") ?: ""
        val location = arguments?.getString("location") ?: ""

        view.findViewById<TextView>(R.id.tvDetailTitle).text = title
        view.findViewById<TextView>(R.id.tvDetailCategory).text = category
        view.findViewById<TextView>(R.id.tvDetailLocation).text = location

        val (hours, fee, phone, description) = getMuseumInfo(title)
        view.findViewById<TextView>(R.id.tvDetailHours).text = hours
        view.findViewById<TextView>(R.id.tvDetailFee).text = fee
        view.findViewById<TextView>(R.id.tvDetailPhone).text = phone
        view.findViewById<TextView>(R.id.tvDetailDescription).text = description

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }

        view.findViewById<View>(R.id.btnDetailAr).setOnClickListener {
            Toast.makeText(requireContext(), "AR 실행", Toast.LENGTH_SHORT).show()
        }
    }

    private data class MuseumInfo(
        val hours: String,
        val fee: String,
        val phone: String,
        val description: String
    )

    private fun getMuseumInfo(title: String): MuseumInfo = when (title) {
        "부산현대미술관" -> MuseumInfo(
            "10:00 – 18:00 (월요일 휴관)",
            "무료 관람",
            "051-278-2345",
            "부산의 현대미술을 대표하는 공간으로, 국내외 현대미술 작품을 상설 및 기획 전시합니다. 을숙도 생태공원과 맞닿아 있어 자연과 예술을 동시에 즐길 수 있습니다."
        )
        "F1963" -> MuseumInfo(
            "10:00 – 19:00 (연중무휴)",
            "무료 (일부 전시 유료)",
            "051-756-1963",
            "고려제강 수영공장을 리모델링한 복합문화공간입니다. 도서관, 전시장, 카페, 공연장 등 다양한 문화 시설을 갖추고 있습니다."
        )
        "부산시립미술관" -> MuseumInfo(
            "10:00 – 18:00 (월요일 휴관)",
            "성인 1,000원 / 청소년 600원",
            "051-744-2602",
            "부산의 대표 시립미술관으로, 근현대 미술 작품을 소장·전시합니다. 다양한 기획 전시와 교육 프로그램을 운영하며 부산 미술 문화의 중심지 역할을 합니다."
        )
        "고은사진미술관" -> MuseumInfo(
            "10:00 – 18:00 (월요일 휴관)",
            "성인 5,000원 / 학생 3,000원",
            "051-746-0055",
            "국내 최초의 사진 전문 미술관으로, 국내외 사진 작가들의 작품을 전시합니다. 사진 예술의 역사와 현대적 트렌드를 탐색할 수 있는 공간입니다."
        )
        "아르떼뮤지엄 부산" -> MuseumInfo(
            "10:00 – 20:00 (연중무휴)",
            "성인 17,000원 / 아동 13,000원",
            "1800-1467",
            "몰입형 미디어 아트를 경험할 수 있는 공간으로, 빛과 소리를 활용한 대형 인터랙티브 전시를 선보입니다. 자연을 모티프로 한 환상적인 디지털 아트 세계를 만날 수 있습니다."
        )
        "부산근대역사관" -> MuseumInfo(
            "09:00 – 18:00 (월요일 휴관)",
            "무료 관람",
            "051-253-3845",
            "부산의 근대 역사를 담은 박물관으로, 개항기부터 현대까지의 부산 역사와 문화를 전시합니다. 일제강점기 동양척식주식회사 부산지점 건물을 활용하였습니다."
        )
        "민주공원" -> MuseumInfo(
            "09:00 – 18:00 (월요일 휴관)",
            "무료 관람",
            "051-790-7400",
            "부산 민주화운동의 역사를 기리는 공원으로, 민주항쟁기념관과 야외 공원으로 구성되어 있습니다. 부산의 민주화 운동 역사를 체험하고 배울 수 있습니다."
        )
        "금정문화회관" -> MuseumInfo(
            "09:00 – 18:00 (월요일 휴관)",
            "전시별 상이",
            "051-519-5252",
            "금정구의 대표 문화 공간으로, 전시, 공연, 교육 등 다양한 문화 프로그램을 운영합니다. 지역 작가들의 작품을 발굴·소개하는 기획 전시를 정기적으로 진행합니다."
        )
        else -> MuseumInfo(
            "10:00 – 18:00",
            "무료 관람",
            "051-000-0000",
            "부산을 대표하는 문화예술 공간입니다. 다양한 전시와 프로그램을 통해 예술을 가까이에서 경험하세요."
        )
    }
}
