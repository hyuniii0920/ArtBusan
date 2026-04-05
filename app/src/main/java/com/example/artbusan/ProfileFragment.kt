package com.example.artbusan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.settingNotification).setOnClickListener {
            Toast.makeText(requireContext(), "알림 설정", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.settingLanguage).setOnClickListener {
            Toast.makeText(requireContext(), "언어 설정", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.settingAppInfo).setOnClickListener {
            Toast.makeText(requireContext(), "앱 정보 v1.0.0", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.settingLogout).setOnClickListener {
            Toast.makeText(requireContext(), "로그아웃", Toast.LENGTH_SHORT).show()
        }
    }
}
