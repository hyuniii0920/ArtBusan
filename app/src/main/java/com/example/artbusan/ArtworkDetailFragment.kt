package com.example.artbusan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.artbusan.data.MuseumDatabase
import kotlinx.coroutines.launch

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

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            findNavController().popBackStack()
        }

        view.findViewById<View>(R.id.btnDetailAr).setOnClickListener {
            Toast.makeText(requireContext(), "AR 실행", Toast.LENGTH_SHORT).show()
        }

        val museumId = arguments?.getInt("id") ?: -1
        if (museumId == -1) {
            // 이전 방식 호환: title 기반 기본값 표시
            val title = arguments?.getString("title") ?: ""
            val category = arguments?.getString("category") ?: ""
            val location = arguments?.getString("location") ?: ""
            view.findViewById<TextView>(R.id.tvDetailTitle).text = title
            view.findViewById<TextView>(R.id.tvDetailCategory).text = category
            view.findViewById<TextView>(R.id.tvDetailLocation).text = location
            return
        }

        val dao = MuseumDatabase.getInstance(requireContext()).museumDao()
        viewLifecycleOwner.lifecycleScope.launch {
            val museum = dao.getById(museumId) ?: return@launch
            view.findViewById<TextView>(R.id.tvDetailTitle).text = museum.title
            view.findViewById<TextView>(R.id.tvDetailCategory).text = museum.category
            view.findViewById<TextView>(R.id.tvDetailLocation).text = museum.location
            view.findViewById<TextView>(R.id.tvDetailHours).text = museum.hours
            view.findViewById<TextView>(R.id.tvDetailFee).text = museum.fee
            view.findViewById<TextView>(R.id.tvDetailPhone).text = museum.phone
            view.findViewById<TextView>(R.id.tvDetailDescription).text = museum.description
        }
    }
}
