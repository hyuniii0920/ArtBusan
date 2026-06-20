package com.example.artbusan

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_settings, container, false)

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

        view.findViewById<View>(R.id.settingNotification).setOnClickListener {
            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
            }
            startActivity(intent)
        }

        view.findViewById<View>(R.id.settingAppInfo).setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.app_info_title))
                .setMessage(getString(R.string.app_info_message))
                .setPositiveButton(getString(R.string.dialog_ok), null)
                .show()
        }

        view.findViewById<View>(R.id.settingVersion).setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.version_info_title))
                .setMessage(getString(R.string.version_message))
                .setPositiveButton(getString(R.string.dialog_ok), null)
                .show()
        }
    }
}
