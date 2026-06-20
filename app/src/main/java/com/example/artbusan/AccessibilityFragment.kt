package com.example.artbusan

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class AccessibilityFragment : Fragment() {

    companion object {
        private const val PREFS = "artbusan_prefs"
        private const val KEY_LARGE_TEXT = "pref_large_text"
        private const val KEY_HIGH_CONTRAST = "pref_high_contrast"
        private const val KEY_VIBRATION = "pref_vibration"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_accessibility, container, false)

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

        val prefs = requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        val switchLargeText = view.findViewById<SwitchCompat>(R.id.switchLargeText)
        val switchHighContrast = view.findViewById<SwitchCompat>(R.id.switchHighContrast)
        val switchVibration = view.findViewById<SwitchCompat>(R.id.switchVibration)

        // 저장된 상태 복원
        switchLargeText.isChecked = prefs.getBoolean(KEY_LARGE_TEXT, false)
        switchHighContrast.isChecked = prefs.getBoolean(KEY_HIGH_CONTRAST, false)
        switchVibration.isChecked = prefs.getBoolean(KEY_VIBRATION, false)

        switchLargeText.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_LARGE_TEXT, isChecked).apply()
        }

        switchHighContrast.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_HIGH_CONTRAST, isChecked).apply()
        }

        switchVibration.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(KEY_VIBRATION, isChecked).apply()
            if (isChecked) vibrate()
        }
    }

    private fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = requireContext().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }
}
