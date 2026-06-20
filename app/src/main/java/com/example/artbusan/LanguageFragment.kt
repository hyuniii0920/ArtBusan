package com.example.artbusan

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.artbusan.data.MuseumDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LanguageFragment : Fragment() {

    companion object {
        private const val PREFS = "artbusan_prefs"
        private const val KEY_LANGUAGE = "selected_language"
    }

    private val langLabels = mapOf(
        "ko" to "한국어",
        "en" to "English",
        "ja" to "日本語",
        "zh" to "中文"
    )
    private val langNotes = mapOf(
        "ko" to "현재 언어로 서비스가 제공됩니다.",
        "en" to "Service is provided in the current language.",
        "ja" to "選択した言語でサービスが提供されます。",
        "zh" to "将以所选语言提供服务。"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_language, container, false)

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
        val saved = prefs.getString(KEY_LANGUAGE, "ko") ?: "ko"

        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroupLanguage)
        val tvSelected = view.findViewById<TextView>(R.id.tvSelectedLanguage)
        val tvNote = view.findViewById<TextView>(R.id.tvLangNote)

        val initId = when (saved) {
            "en" -> R.id.radioEnglish
            "ja" -> R.id.radioJapanese
            "zh" -> R.id.radioChinese
            else -> R.id.radioKorean
        }
        radioGroup.check(initId)
        tvSelected.text = langLabels[saved]
        tvNote.text = langNotes[saved]

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val code = when (checkedId) {
                R.id.radioEnglish -> "en"
                R.id.radioJapanese -> "ja"
                R.id.radioChinese -> "zh"
                else -> "ko"
            }
            if (code == saved) return@setOnCheckedChangeListener

            tvSelected.text = langLabels[code]
            tvNote.text = langNotes[code]

            prefs.edit().putString(KEY_LANGUAGE, code).apply()

            // DB 초기화 후 액티비티 재시작 → 새 언어로 재시드
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    MuseumDatabase.getInstance(requireContext()).museumDao().deleteAll()
                }
                requireActivity().recreate()
            }
        }
    }
}
