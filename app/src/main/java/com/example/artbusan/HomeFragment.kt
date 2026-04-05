package com.example.artbusan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Service icon click listeners
        view.findViewById<View>(R.id.serviceArPath).setOnClickListener {
            Toast.makeText(requireContext(), "AR 길찾기 실행", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.serviceMyTour).setOnClickListener {
            Toast.makeText(requireContext(), "나의 전시투어", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.serviceStamp).setOnClickListener {
            Toast.makeText(requireContext(), "스탬프 보기", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.serviceArCamera).setOnClickListener {
            Toast.makeText(requireContext(), "AR 카메라 실행", Toast.LENGTH_SHORT).show()
        }

        // AR Run button
        view.findViewById<View>(R.id.btnArRun).setOnClickListener {
            Toast.makeText(requireContext(), "AR 실행", Toast.LENGTH_SHORT).show()
        }

        // Tour tab switching
        val tabRecommend = view.findViewById<TextView>(R.id.tabRecommend)
        val tabMyTour = view.findViewById<TextView>(R.id.tabMyTour)

        tabRecommend.setOnClickListener {
            tabRecommend.setBackgroundResource(R.drawable.bg_tab_selected)
            tabRecommend.setTextColor(resources.getColor(android.R.color.white, null))
            tabMyTour.setBackgroundResource(R.drawable.bg_tab_unselected)
            tabMyTour.setTextColor(resources.getColor(R.color.text_secondary, null))
        }

        tabMyTour.setOnClickListener {
            tabMyTour.setBackgroundResource(R.drawable.bg_tab_selected)
            tabMyTour.setTextColor(resources.getColor(android.R.color.white, null))
            tabRecommend.setBackgroundResource(R.drawable.bg_tab_unselected)
            tabRecommend.setTextColor(resources.getColor(R.color.text_secondary, null))
        }
    }
}
