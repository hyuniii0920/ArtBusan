package com.example.artbusan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ExploreFragment : Fragment() {

    private val dummyArtworks = listOf(
        ArtworkItem("금동미륵보살반가사유상", "조각", "3층 · 불교조각실"),
        ArtworkItem("청자상감운학문매병", "공예", "3층 · 고려도자실"),
        ArtworkItem("혼천의 및 혼천시계", "유물", "2층 · 조선시대실"),
        ArtworkItem("인왕제색도", "회화", "2층 · 회화실"),
        ArtworkItem("반가사유상", "조각", "3층 · 불교조각실"),
        ArtworkItem("청자투각칠보문향로", "공예", "3층 · 고려도자실"),
        ArtworkItem("신윤복 미인도", "회화", "2층 · 회화실"),
        ArtworkItem("동궐도", "회화", "2층 · 회화실"),
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_explore, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvArtworks)
        rv.layoutManager = GridLayoutManager(requireContext(), 2)
        rv.adapter = ArtworkAdapter(dummyArtworks)

        // Chip click listeners
        val chips = listOf(
            view.findViewById<TextView>(R.id.chipAll),
            view.findViewById<TextView>(R.id.chipPainting),
            view.findViewById<TextView>(R.id.chipSculpture),
            view.findViewById<TextView>(R.id.chipCraft),
            view.findViewById<TextView>(R.id.chipRelic),
            view.findViewById<TextView>(R.id.chipSpecial)
        )

        chips.forEach { chip ->
            chip.setOnClickListener {
                chips.forEach { c ->
                    c.setBackgroundResource(R.drawable.bg_chip_unselected)
                    c.setTextColor(resources.getColor(R.color.text_secondary, null))
                }
                chip.setBackgroundResource(R.drawable.bg_tab_selected)
                chip.setTextColor(resources.getColor(android.R.color.white, null))
            }
        }
    }
}
