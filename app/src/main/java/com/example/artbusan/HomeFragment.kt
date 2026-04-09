package com.example.artbusan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment() {

    private val artworks = listOf(
        ArtworkItem("부산현대미술관", "해운대구", "해운대구 우동"),
        ArtworkItem("F1963", "수영구", "수영구 망미동"),
        ArtworkItem("부산시립미술관", "남구", "남구 대연동"),
        ArtworkItem("고은사진미술관", "해운대구", "해운대구 중동"),
        ArtworkItem("아르떼뮤지엄 부산", "영도구", "영도구 동삼동"),
        ArtworkItem("부산근대역사관", "중구", "중구 대청동"),
        ArtworkItem("민주공원", "중구", "중구 영주동"),
        ArtworkItem("금정문화회관", "금정구", "금정구 부곡동"),
    )

    private lateinit var adapter: ArtworkAdapter
    private val chips = mutableListOf<TextView>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // AR 실행 버튼
        view.findViewById<View>(R.id.btnArRun).setOnClickListener {
            Toast.makeText(requireContext(), "AR 실행", Toast.LENGTH_SHORT).show()
        }

        // RecyclerView
        adapter = ArtworkAdapter(artworks) { item ->
            val bundle = bundleOf(
                "title" to item.title,
                "category" to item.category,
                "location" to item.location
            )
            findNavController().navigate(R.id.action_home_to_detail, bundle)
        }
        view.findViewById<RecyclerView>(R.id.rvArtworks).apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@HomeFragment.adapter
        }

        // 카테고리 칩
        chips.addAll(listOf(
            view.findViewById(R.id.chipAll),
            view.findViewById(R.id.chipHaeundae),
            view.findViewById(R.id.chipSuyeong),
            view.findViewById(R.id.chipNam),
            view.findViewById(R.id.chipYeongdo),
            view.findViewById(R.id.chipJung),
            view.findViewById(R.id.chipDong),
            view.findViewById(R.id.chipBusanjin),
            view.findViewById(R.id.chipDongrae),
            view.findViewById(R.id.chipGeumjeong),
            view.findViewById(R.id.chipGijang)
        ))

        chips.forEach { chip ->
            chip.setOnClickListener { selectChip(chip) }
        }
    }

    private fun selectChip(selected: TextView) {
        chips.forEach { chip ->
            chip.setBackgroundResource(R.drawable.bg_chip_unselected)
            chip.setTextColor(resources.getColor(R.color.text_secondary, null))
        }
        selected.setBackgroundResource(R.drawable.bg_tab_selected)
        selected.setTextColor(resources.getColor(android.R.color.white, null))
    }
}
