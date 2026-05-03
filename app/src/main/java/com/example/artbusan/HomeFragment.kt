package com.example.artbusan

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.artbusan.data.MuseumDatabase
import com.example.artbusan.data.MuseumRepository
import com.example.artbusan.viewmodel.MuseumViewModel
import com.example.artbusan.viewmodel.MuseumViewModelFactory

class HomeFragment : Fragment() {

    private val viewModel: MuseumViewModel by viewModels {
        val db = MuseumDatabase.getInstance(requireContext())
        MuseumViewModelFactory(MuseumRepository(db.museumDao(), requireContext()))
    }

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

        view.findViewById<View>(R.id.btnArRun).setOnClickListener {
            startActivity(Intent(requireContext(), ArViewerActivity::class.java))
        }

        adapter = ArtworkAdapter { museum ->
            val bundle = bundleOf(
                "id" to museum.id,
                "title" to museum.title,
                "category" to museum.category,
                "location" to museum.location
            )
            findNavController().navigate(R.id.action_home_to_detail, bundle)
        }
        view.findViewById<RecyclerView>(R.id.rvArtworks).apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@HomeFragment.adapter
        }

        val chipDefs: List<Pair<Int, String?>> = listOf(
            R.id.chipAll to null,
            R.id.chipHaeundae to "해운대구",
            R.id.chipSuyeong to "수영구",
            R.id.chipNam to "남구",
            R.id.chipYeongdo to "영도구",
            R.id.chipJung to "중구",
            R.id.chipDong to "동구",
            R.id.chipBusanjin to "부산진구",
            R.id.chipDongrae to "동래구",
            R.id.chipGeumjeong to "금정구",
            R.id.chipGijang to "기장군"
        )

        chipDefs.forEach { (chipId, category) ->
            val chip = view.findViewById<TextView>(chipId)
            chips.add(chip)
            chip.setOnClickListener {
                selectChip(chip)
                viewModel.load(category)
            }
        }

        viewModel.museums.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        viewModel.load()
        selectChip(view.findViewById(R.id.chipAll))
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
