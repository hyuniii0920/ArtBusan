package com.example.artbusan

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsTracker {

    private const val USER_PROPERTY_PREFERRED_LANGUAGE = "preferred_language"

    private const val PARAM_ARTWORK_ID = "artwork_id"
    private const val PARAM_CATEGORY = "category"
    private const val PARAM_DISTRICT = "district"
    private const val PARAM_ENTRY_POINT = "entry_point"
    private const val PARAM_GRANTED = "granted"
    private const val PARAM_MUSEUM_ID = "museum_id"
    private const val PARAM_PREVIOUS_LANGUAGE = "previous_language"
    private const val PARAM_RESULT = "result"
    private const val PARAM_SELECTED_LANGUAGE = "selected_language"
    private const val PARAM_SOURCE = "source"
    private const val PARAM_STATE = "state"
    private const val PARAM_TARGET = "target"

    private const val EVENT_AR_MODE_START = "ar_mode_start"
    private const val EVENT_AR_VIEWER_OPEN = "ar_viewer_open"
    private const val EVENT_ARTWORK_LOAD_RESULT = "artwork_load_result"
    private const val EVENT_ARTWORK_SHEET_TOGGLE = "artwork_sheet_toggle"
    private const val EVENT_CAMERA_PERMISSION_RESULT = "camera_permission_result"
    private const val EVENT_DRAWER_MENU_SELECT = "drawer_menu_select"
    private const val EVENT_LANGUAGE_CHANGE = "language_change"
    private const val EVENT_MUSEUM_FILTER_SELECT = "museum_filter_select"
    private const val EVENT_MUSEUM_SELECT = "museum_select"
    private const val EVENT_QR_SCAN_RESULT = "qr_scan_result"
    private const val EVENT_QR_SCAN_START = "qr_scan_start"
    private const val EVENT_TOUR_CREATE_CLICK = "tour_create_click"
    private const val EVENT_TTS_PLAY = "tts_play"

    fun setPreferredLanguage(context: Context, language: String) {
        analytics(context).setUserProperty(USER_PROPERTY_PREFERRED_LANGUAGE, language)
    }

    fun logScreenView(context: Context, screenName: String, screenClass: String) {
        analytics(context).logEvent(
            FirebaseAnalytics.Event.SCREEN_VIEW,
            bundleOf(
                FirebaseAnalytics.Param.SCREEN_NAME to screenName,
                FirebaseAnalytics.Param.SCREEN_CLASS to screenClass
            )
        )
    }

    fun logMuseumFilterSelect(context: Context, district: String) {
        log(context, EVENT_MUSEUM_FILTER_SELECT, PARAM_DISTRICT to district)
    }

    fun logMuseumSelect(context: Context, museumId: Int, category: String, district: String) {
        log(
            context,
            EVENT_MUSEUM_SELECT,
            PARAM_MUSEUM_ID to museumId,
            PARAM_CATEGORY to category,
            PARAM_DISTRICT to district
        )
    }

    fun logDrawerMenuSelect(context: Context, target: String) {
        log(context, EVENT_DRAWER_MENU_SELECT, PARAM_TARGET to target)
    }

    fun logTourCreateClick(context: Context, source: String) {
        log(context, EVENT_TOUR_CREATE_CLICK, PARAM_SOURCE to source)
    }

    fun logLanguageChange(context: Context, previousLanguage: String, selectedLanguage: String) {
        setPreferredLanguage(context, selectedLanguage)
        log(
            context,
            EVENT_LANGUAGE_CHANGE,
            PARAM_PREVIOUS_LANGUAGE to previousLanguage,
            PARAM_SELECTED_LANGUAGE to selectedLanguage
        )
    }

    fun logArViewerOpen(
        context: Context,
        entryPoint: String,
        museumId: Int? = null,
        category: String? = null,
        district: String? = null
    ) {
        log(
            context,
            EVENT_AR_VIEWER_OPEN,
            PARAM_ENTRY_POINT to entryPoint,
            PARAM_MUSEUM_ID to museumId,
            PARAM_CATEGORY to category,
            PARAM_DISTRICT to district
        )
    }

    fun logCameraPermissionResult(context: Context, granted: Boolean) {
        log(context, EVENT_CAMERA_PERMISSION_RESULT, PARAM_GRANTED to granted)
    }

    fun logQrScanStart(context: Context, source: String) {
        log(context, EVENT_QR_SCAN_START, PARAM_SOURCE to source)
    }

    fun logQrScanResult(context: Context, result: String, artworkId: Int? = null) {
        log(context, EVENT_QR_SCAN_RESULT, PARAM_RESULT to result, PARAM_ARTWORK_ID to artworkId)
    }

    fun logArtworkLoadResult(context: Context, result: String, artworkId: Int) {
        log(context, EVENT_ARTWORK_LOAD_RESULT, PARAM_RESULT to result, PARAM_ARTWORK_ID to artworkId)
    }

    fun logArtworkSheetToggle(context: Context, state: String, artworkId: Int?) {
        log(context, EVENT_ARTWORK_SHEET_TOGGLE, PARAM_STATE to state, PARAM_ARTWORK_ID to artworkId)
    }

    fun logTtsPlay(context: Context, artworkId: Int) {
        log(context, EVENT_TTS_PLAY, PARAM_ARTWORK_ID to artworkId)
    }

    fun logArModeStart(context: Context, artworkId: Int) {
        log(context, EVENT_AR_MODE_START, PARAM_ARTWORK_ID to artworkId)
    }

    private fun log(context: Context, eventName: String, vararg params: Pair<String, Any?>) {
        analytics(context).logEvent(eventName, bundleOf(*params))
    }

    private fun bundleOf(vararg params: Pair<String, Any?>): Bundle {
        return Bundle().apply {
            params.forEach { (key, value) ->
                when (value) {
                    null -> Unit
                    is Boolean -> putString(key, value.toString())
                    is Int -> putLong(key, value.toLong())
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Float -> putDouble(key, value.toDouble())
                    else -> putString(key, value.toString())
                }
            }
        }
    }

    private fun analytics(context: Context): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(context.applicationContext)
    }
}
