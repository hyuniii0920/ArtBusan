package com.example.artbusan

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.artbusan.ar.ArCoreImageRenderer
import com.example.artbusan.ar.ArMarker
import com.example.artbusan.ar.ArMarkerRegistry
import com.example.artbusan.ar.ArPayloadParser
import com.example.artbusan.ar.MockArtworkRegistry
import com.example.artbusan.network.ArtworkExperience
import com.example.artbusan.network.ArtworkExperienceRepository
import com.google.ar.core.ArCoreApk
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ArViewerActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private enum class ViewerState {
        Preparing,
        MarkerScanning,
        QrFallbackScanning,
        ArtworkLoaded,
        ArUnavailable,
        PermissionDenied
    }

    private enum class LoadOrigin {
        Marker,
        Qr
    }

    private lateinit var artworkRepository: ArtworkExperienceRepository
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var arRenderer: ArCoreImageRenderer
    private lateinit var previewView: PreviewView
    private lateinit var cameraExecutor: ExecutorService

    private lateinit var tvStatus: TextView
    private lateinit var tvArHint: TextView
    private lateinit var dimOverlay: View
    private lateinit var btnScanQr: TextView
    private lateinit var btnRescan: TextView
    private lateinit var bottomSheet: View
    private lateinit var tvPeekTitle: TextView
    private lateinit var tvPeekSummary: TextView
    private lateinit var btnSeeMore: TextView
    private lateinit var tvDetailTitle: TextView
    private lateinit var tvArtist: TextView
    private lateinit var tvDetailBody: TextView
    private lateinit var btnTts: TextView
    private lateinit var btnArMode: TextView
    private lateinit var detailGroup: View
    private lateinit var tvArModeBadge: TextView
    private lateinit var tvArModeGuide: TextView
    private lateinit var scanFrame: View
    private lateinit var preScanGuideGroup: View

    private val scanner by lazy {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )
    }

    private val cameraProviderFuture by lazy { ProcessCameraProvider.getInstance(this) }

    private var currentState = ViewerState.Preparing
    private var currentLoadOrigin: LoadOrigin? = null
    private var arSession: Session? = null
    private var userRequestedArInstall = true
    private var isDetailExpanded = false
    private var lastScannedValue: String? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var currentArtwork: ArtworkExperience? = null

    @Volatile
    private var scanningEnabled = false

    @Volatile
    private var processingScan = false

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        AnalyticsTracker.logCameraPermissionResult(this, granted)
        if (granted) {
            prepareArOrFallback()
        } else {
            setViewerState(ViewerState.PermissionDenied)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_viewer)

        artworkRepository = ArtworkExperienceRepository(applicationContext)
        cameraExecutor = Executors.newSingleThreadExecutor()

        bindViews()
        setupRenderer()
        setupSheet()
        setupActions()
        bindInitialUi()
        logOpenEvents()

        tts = TextToSpeech(this, this)
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
        if (!hasCameraPermission()) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            prepareArOrFallback()
        }
    }

    override fun onPause() {
        super.onPause()
        stopQrCamera()
        pauseArSession()
        glSurfaceView.onPause()
    }

    override fun onDestroy() {
        scanner.close()
        cameraExecutor.shutdown()
        tts?.stop()
        tts?.shutdown()
        arSession?.close()
        arSession = null
        super.onDestroy()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val preferred = ttsLocaleForCurrentConfiguration()
            var result = tts?.setLanguage(preferred)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                result = tts?.setLanguage(Locale.KOREAN)
            }
            isTtsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                result != TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    private fun bindViews() {
        findViewById<ImageButton>(R.id.btnArBack).setOnClickListener { finish() }
        glSurfaceView = findViewById(R.id.glArScene)
        previewView = findViewById(R.id.previewCamera)
        previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
        tvStatus = findViewById(R.id.tvArStatus)
        tvArHint = findViewById(R.id.tvArHint)
        dimOverlay = findViewById(R.id.dimOverlay)
        btnScanQr = findViewById(R.id.btnScanQr)
        btnRescan = findViewById(R.id.btnRescan)
        bottomSheet = findViewById(R.id.bottomSheetArtwork)
        tvPeekTitle = findViewById(R.id.tvPeekTitle)
        tvPeekSummary = findViewById(R.id.tvPeekSummary)
        btnSeeMore = findViewById(R.id.btnSeeMore)
        tvDetailTitle = findViewById(R.id.tvDetailTitleSheet)
        tvArtist = findViewById(R.id.tvArtist)
        tvDetailBody = findViewById(R.id.tvDetailBody)
        btnTts = findViewById(R.id.btnTts)
        btnArMode = findViewById(R.id.btnArMode)
        detailGroup = findViewById(R.id.layoutDetailGroup)
        tvArModeBadge = findViewById(R.id.tvArModeBadge)
        tvArModeGuide = findViewById(R.id.tvArModeGuide)
        scanFrame = findViewById(R.id.scanFrame)
        preScanGuideGroup = findViewById(R.id.preScanGuideGroup)
    }

    private fun setupRenderer() {
        arRenderer = ArCoreImageRenderer(
            displayRotationProvider = {
                previewView.display?.rotation ?: Surface.ROTATION_0
            },
            listener = object : ArCoreImageRenderer.Listener {
                override fun onMarkerDetected(marker: ArMarker) {
                    runOnUiThread { handleMarkerDetected(marker) }
                }

                override fun onMarkerTrackingLost(marker: ArMarker) {
                    runOnUiThread { handleMarkerTrackingLost(marker) }
                }

                override fun onArFrameError(error: Throwable) {
                    runOnUiThread { handleArFrameError(error) }
                }
            }
        )
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.preserveEGLContextOnPause = true
        glSurfaceView.setRenderer(arRenderer)
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }

    private fun setupSheet() {
        bottomSheet.isVisible = false
        detailGroup.isVisible = false
        updateSheetExpanded(false)
    }

    private fun setupActions() {
        btnScanQr.setOnClickListener {
            AnalyticsTracker.logQrFallbackOpen(this, currentState.name.lowercase(Locale.US))
            enterQrFallbackScanning("manual")
        }
        btnRescan.setOnClickListener {
            currentArtwork = null
            lastScannedValue = null
            bottomSheet.isVisible = false
            updateSheetExpanded(false)
            if (arSession != null && currentLoadOrigin != LoadOrigin.Qr) {
                startMarkerScanning("rescan")
            } else {
                enterQrFallbackScanning("rescan")
            }
        }
        btnSeeMore.setOnClickListener {
            val nextExpanded = !isDetailExpanded
            AnalyticsTracker.logArtworkSheetToggle(
                this,
                if (nextExpanded) "expanded" else "collapsed",
                currentArtwork?.id
            )
            updateSheetExpanded(nextExpanded)
        }
        btnTts.setOnClickListener { speakDetailDescription() }
        btnArMode.setOnClickListener { focusArMode() }
    }

    private fun bindInitialUi() {
        tvPeekTitle.text = getString(R.string.ar_initial_title)
        tvPeekSummary.text = getString(R.string.ar_initial_summary)
        btnScanQr.text = getString(R.string.ar_button_qr_fallback)
        btnRescan.text = getString(R.string.ar_button_rescan)
        btnTts.text = getString(R.string.ar_button_tts)
        btnArMode.text = getString(R.string.ar_button_ar_mode)
        tvArModeBadge.isVisible = false
        bottomSheet.isVisible = false
        detailGroup.isVisible = false
        previewView.isVisible = false
        glSurfaceView.isVisible = false
        scanFrame.isVisible = false
        dimOverlay.isVisible = true
        dimOverlay.alpha = 1f
        setViewerState(ViewerState.Preparing)
    }

    private fun prepareArOrFallback() {
        setViewerState(ViewerState.Preparing)
        stopQrCamera()

        val availability = ArCoreApk.getInstance().checkAvailability(this)
        AnalyticsTracker.logArCoreAvailability(this, availability.name)
        if (availability.isTransient) {
            glSurfaceView.postDelayed({ prepareArOrFallback() }, ARCORE_AVAILABILITY_RETRY_MS)
            return
        }

        if (!availability.isSupported) {
            enterArUnavailableFallback("unsupported")
            return
        }

        try {
            when (ArCoreApk.getInstance().requestInstall(this, userRequestedArInstall)) {
                ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                    userRequestedArInstall = false
                    tvStatus.text = getString(R.string.ar_status_preparing)
                    tvArHint.text = getString(R.string.ar_arcore_install_requested)
                    return
                }

                ArCoreApk.InstallStatus.INSTALLED -> {
                    userRequestedArInstall = false
                    createSessionIfNeeded()
                    startMarkerScanning("resume")
                }
            }
        } catch (error: UnavailableUserDeclinedInstallationException) {
            enterArUnavailableFallback("install_declined")
        } catch (error: UnavailableDeviceNotCompatibleException) {
            enterArUnavailableFallback("device_not_compatible")
        } catch (error: UnavailableArcoreNotInstalledException) {
            enterArUnavailableFallback("not_installed")
        } catch (error: UnavailableApkTooOldException) {
            enterArUnavailableFallback("apk_too_old")
        } catch (error: UnavailableSdkTooOldException) {
            enterArUnavailableFallback("sdk_too_old")
        } catch (error: RuntimeException) {
            enterArUnavailableFallback("runtime_error")
        }
    }

    private fun createSessionIfNeeded() {
        if (arSession != null) return

        val session = Session(this)
        val imageDatabase = AugmentedImageDatabase(session)
        ArMarkerRegistry.all().forEach { marker ->
            val bitmap = BitmapFactory.decodeResource(resources, marker.markerDrawableRes)
            if (bitmap != null) {
                imageDatabase.addImage(marker.name, bitmap, marker.markerWidthMeters)
                bitmap.recycle()
            }
        }
        val config = Config(session).apply {
            augmentedImageDatabase = imageDatabase
            focusMode = Config.FocusMode.AUTO
        }
        session.configure(config)
        arSession = session
    }

    private fun startMarkerScanning(source: String) {
        stopQrCamera()
        val session = arSession ?: run {
            enterArUnavailableFallback("session_missing")
            return
        }
        currentLoadOrigin = LoadOrigin.Marker
        currentArtwork = null
        arRenderer.resetTrackingState()

        try {
            session.resume()
        } catch (error: CameraNotAvailableException) {
            handleArFrameError(error)
            return
        }

        arRenderer.session = session
        glSurfaceView.isVisible = true
        previewView.isVisible = false
        scanFrame.isVisible = false
        dimOverlay.isVisible = true
        dimOverlay.alpha = 0.14f
        bottomSheet.isVisible = false
        preScanGuideGroup.isVisible = true
        setViewerState(ViewerState.MarkerScanning)
        AnalyticsTracker.logArMarkerScanStart(this, source)
    }

    private fun enterArUnavailableFallback(reason: String) {
        AnalyticsTracker.logQrFallbackOpen(this, reason)
        setViewerState(ViewerState.ArUnavailable)
        Toast.makeText(this, getString(R.string.ar_qr_fallback_started), Toast.LENGTH_SHORT).show()
        enterQrFallbackScanning(reason)
    }

    private fun enterQrFallbackScanning(source: String) {
        pauseArSession()
        currentLoadOrigin = LoadOrigin.Qr
        currentArtwork = null
        lastScannedValue = null
        bottomSheet.isVisible = false
        updateSheetExpanded(false)

        glSurfaceView.isVisible = false
        previewView.isVisible = true
        dimOverlay.isVisible = true
        dimOverlay.alpha = 0.12f
        preScanGuideGroup.isVisible = false
        scanFrame.isVisible = true
        scanningEnabled = true
        setViewerState(ViewerState.QrFallbackScanning)
        AnalyticsTracker.logQrScanStart(this, source)
        startCameraPreview()
    }

    private fun startCameraPreview() {
        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()
                val displayRotation = previewView.display?.rotation ?: Surface.ROTATION_0
                val preview = Preview.Builder()
                    .setTargetRotation(displayRotation)
                    .build()
                    .also { it.surfaceProvider = previewView.surfaceProvider }
                val analysis = ImageAnalysis.Builder()
                    .setTargetRotation(displayRotation)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analyzer ->
                        analyzer.setAnalyzer(cameraExecutor) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage == null || !scanningEnabled || processingScan) {
                                imageProxy.close()
                                return@setAnalyzer
                            }

                            processingScan = true
                            val inputImage = InputImage.fromMediaImage(
                                mediaImage,
                                imageProxy.imageInfo.rotationDegrees
                            )

                            scanner.process(inputImage)
                                .addOnSuccessListener { barcodes ->
                                    handleBarcodeResult(barcodes)
                                }
                                .addOnCompleteListener {
                                    processingScan = false
                                    imageProxy.close()
                                }
                        }
                    }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
            },
            ContextCompat.getMainExecutor(this)
        )
    }

    private fun stopQrCamera() {
        scanningEnabled = false
        if (cameraProviderFuture.isDone) {
            runCatching { cameraProviderFuture.get().unbindAll() }
        }
    }

    private fun pauseArSession() {
        arRenderer.session = null
        runCatching { arSession?.pause() }
    }

    private fun handleMarkerDetected(marker: ArMarker) {
        if (currentState != ViewerState.MarkerScanning && currentState != ViewerState.ArtworkLoaded) return

        AnalyticsTracker.logArMarkerDetected(this, marker.artworkId)
        currentLoadOrigin = LoadOrigin.Marker
        loadArtwork(marker.artworkId, LoadOrigin.Marker)
    }

    private fun handleMarkerTrackingLost(marker: ArMarker) {
        AnalyticsTracker.logArMarkerLost(this, marker.artworkId)
        if (currentLoadOrigin == LoadOrigin.Marker) {
            preScanGuideGroup.isVisible = true
            tvArModeBadge.isVisible = true
            tvArModeBadge.text = getString(R.string.ar_status_marker_scan)
            tvArModeGuide.text = getString(R.string.ar_guide_marker_lost)
            btnScanQr.text = getString(R.string.ar_button_qr_fallback)
        }
    }

    private fun handleArFrameError(error: Throwable) {
        if (currentState == ViewerState.QrFallbackScanning || currentState == ViewerState.PermissionDenied) return

        val reason = when (error) {
            is CameraNotAvailableException -> "camera_not_available"
            else -> "ar_frame_error"
        }
        enterArUnavailableFallback(reason)
    }

    private fun handleBarcodeResult(barcodes: List<Barcode>) {
        val rawValue = barcodes.firstNotNullOfOrNull {
            it.rawValue?.trim()?.takeIf(String::isNotBlank)
        } ?: return

        if (rawValue == lastScannedValue) {
            return
        }

        val artworkId = ArPayloadParser.parseArtworkId(rawValue)
        if (artworkId == null) {
            AnalyticsTracker.logQrScanResult(this, "invalid")
            lastScannedValue = rawValue
            tvStatus.text = getString(R.string.ar_status_invalid_qr)
            tvArHint.text = getString(R.string.ar_hint_invalid_qr)
            Toast.makeText(this, getString(R.string.ar_hint_invalid_qr), Toast.LENGTH_SHORT).show()
            return
        }

        AnalyticsTracker.logQrScanResult(this, "valid", artworkId)
        lastScannedValue = rawValue
        scanningEnabled = false
        loadArtwork(artworkId, LoadOrigin.Qr)
    }

    private fun loadArtwork(artworkId: Int, origin: LoadOrigin) {
        tvStatus.text = getString(R.string.ar_status_artwork_loaded, artworkId)
        tvArHint.text = getString(R.string.ar_hint_loading_artwork)

        MockArtworkRegistry.findById(artworkId)?.let { mockArtwork ->
            AnalyticsTracker.logArtworkLoadResult(this, "mock_success", artworkId)
            currentArtwork = mockArtwork
            currentLoadOrigin = origin
            bindArtwork(mockArtwork)
            return
        }

        lifecycleScope.launch {
            runCatching {
                artworkRepository.getArtwork(artworkId)
            }.onSuccess { artwork ->
                AnalyticsTracker.logArtworkLoadResult(this@ArViewerActivity, "success", artworkId)
                currentArtwork = artwork
                currentLoadOrigin = origin
                bindArtwork(artwork)
            }.onFailure {
                AnalyticsTracker.logArtworkLoadResult(this@ArViewerActivity, "failure", artworkId)
                tvStatus.text = getString(R.string.ar_status_load_failed)
                tvArHint.text = getString(R.string.ar_hint_load_failed)
                Toast.makeText(this@ArViewerActivity, getString(R.string.ar_hint_load_failed), Toast.LENGTH_SHORT).show()
                lastScannedValue = null
                if (origin == LoadOrigin.Qr) {
                    scanningEnabled = true
                }
            }
        }
    }

    private fun bindArtwork(artwork: ArtworkExperience) {
        setViewerState(ViewerState.ArtworkLoaded)
        tvStatus.text = getString(R.string.ar_status_artwork_loaded, artwork.id)
        tvArHint.text = getString(R.string.ar_hint_artwork_loaded)

        tvPeekTitle.text = artwork.title
        tvPeekSummary.text = artwork.summaryDescription
        tvDetailTitle.text = artwork.title
        tvArtist.text = getString(R.string.ar_artist_label, artwork.artist)
        tvDetailBody.text = artwork.detailDescription
        tvArModeGuide.text = getString(R.string.ar_guide_artwork_loaded)

        scanFrame.isVisible = false
        dimOverlay.isVisible = true
        dimOverlay.alpha = if (currentLoadOrigin == LoadOrigin.Marker) 0.08f else 0.22f
        bottomSheet.isVisible = true
        bottomSheet.bringToFront()
        bottomSheet.requestLayout()
        tvArModeBadge.isVisible = currentLoadOrigin == LoadOrigin.Marker
        tvArModeBadge.text = getString(R.string.ar_status_marker_scan)
        updateSheetExpanded(false)
    }

    private fun speakDetailDescription() {
        val artwork = currentArtwork ?: return
        if (!isTtsReady) {
            Toast.makeText(this, getString(R.string.ar_tts_not_ready), Toast.LENGTH_SHORT).show()
            return
        }

        tts?.stop()
        AnalyticsTracker.logTtsPlay(this, artwork.id)
        tts?.speak(artwork.detailDescription, TextToSpeech.QUEUE_FLUSH, null, "artar-detail-${artwork.id}")
    }

    private fun focusArMode() {
        val artwork = currentArtwork ?: run {
            Toast.makeText(this, getString(R.string.ar_scan_artwork_first), Toast.LENGTH_SHORT).show()
            return
        }

        AnalyticsTracker.logArModeStart(this, artwork.id)
        updateSheetExpanded(false)
        tvArModeBadge.isVisible = true
        tvArModeBadge.text = getString(R.string.ar_status_marker_scan)
        tvArModeGuide.text = getString(R.string.ar_hint_artwork_loaded)
        tvStatus.text = getString(R.string.ar_status_artwork_loaded, artwork.id)
    }

    private fun updateSheetExpanded(expanded: Boolean) {
        isDetailExpanded = expanded
        detailGroup.isVisible = expanded
        btnSeeMore.text = if (expanded) {
            getString(R.string.ar_button_summary)
        } else {
            getString(R.string.ar_button_detail)
        }
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = if (expanded) {
            resources.displayMetrics.heightPixels -
                resources.getDimensionPixelSize(R.dimen.bottom_sheet_expanded_top_offset)
        } else {
            ViewGroup.LayoutParams.WRAP_CONTENT
        }
        bottomSheet.layoutParams = layoutParams
        bottomSheet.requestLayout()
    }

    private fun setViewerState(state: ViewerState) {
        currentState = state
        when (state) {
            ViewerState.Preparing -> {
                tvStatus.text = getString(R.string.ar_status_preparing)
                tvArHint.text = getString(R.string.ar_hint_preparing)
                tvArModeGuide.text = getString(R.string.ar_guide_marker_scan)
                tvArModeBadge.isVisible = false
                preScanGuideGroup.isVisible = true
                btnScanQr.text = getString(R.string.ar_button_qr_fallback)
            }

            ViewerState.MarkerScanning -> {
                tvStatus.text = getString(R.string.ar_status_marker_scan)
                tvArHint.text = getString(R.string.ar_hint_marker_scan)
                tvArModeGuide.text = getString(R.string.ar_guide_marker_scan)
                tvArModeBadge.isVisible = true
                tvArModeBadge.text = getString(R.string.ar_status_marker_scan)
                preScanGuideGroup.isVisible = true
                btnScanQr.text = getString(R.string.ar_button_qr_fallback)
            }

            ViewerState.QrFallbackScanning -> {
                tvStatus.text = getString(R.string.ar_status_qr_scan)
                tvArHint.text = getString(R.string.ar_hint_qr_scan)
                tvArModeGuide.text = getString(R.string.ar_guide_qr_scan)
                tvArModeBadge.isVisible = false
            }

            ViewerState.ArtworkLoaded -> {
                tvArHint.text = getString(R.string.ar_hint_artwork_loaded)
                tvArModeGuide.text = getString(R.string.ar_guide_artwork_loaded)
                preScanGuideGroup.isVisible = false
            }

            ViewerState.ArUnavailable -> {
                tvStatus.text = getString(R.string.ar_status_ar_unavailable)
                tvArHint.text = getString(R.string.ar_hint_ar_unavailable)
                tvArModeGuide.text = getString(R.string.ar_guide_qr_scan)
                tvArModeBadge.isVisible = false
                preScanGuideGroup.isVisible = true
                btnScanQr.text = getString(R.string.ar_button_qr_fallback)
            }

            ViewerState.PermissionDenied -> {
                stopQrCamera()
                pauseArSession()
                glSurfaceView.isVisible = false
                previewView.isVisible = false
                scanFrame.isVisible = false
                dimOverlay.isVisible = true
                dimOverlay.alpha = 1f
                bottomSheet.isVisible = false
                tvStatus.text = getString(R.string.ar_status_permission_denied)
                tvArHint.text = getString(R.string.ar_hint_permission_denied)
                tvArModeGuide.text = getString(R.string.ar_privacy_notice)
                tvArModeBadge.isVisible = false
                preScanGuideGroup.isVisible = true
            }
        }
    }

    private fun logOpenEvents() {
        val museumId = intent.getIntExtra(EXTRA_MUSEUM_ID, -1).takeIf { it != -1 }
        val category = intent.getStringExtra(EXTRA_CATEGORY)
        val location = intent.getStringExtra(EXTRA_LOCATION)
        val entryPoint = if (museumId == null) "home" else "artwork_detail"

        AnalyticsTracker.logScreenView(this, "ar_viewer", "ArViewerActivity")
        AnalyticsTracker.logArViewerOpen(
            context = this,
            entryPoint = entryPoint,
            museumId = museumId,
            category = category,
            district = location
        )
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun ttsLocaleForCurrentConfiguration(): Locale {
        val locale = resources.configuration.locales[0]
        return when (locale.language) {
            Locale.ENGLISH.language -> Locale.ENGLISH
            Locale.JAPANESE.language -> Locale.JAPANESE
            Locale.CHINESE.language -> Locale.CHINESE
            else -> Locale.KOREAN
        }
    }

    companion object {
        const val EXTRA_MUSEUM_ID = "extra_museum_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_CATEGORY = "extra_category"
        const val EXTRA_LOCATION = "extra_location"
        private const val ARCORE_AVAILABILITY_RETRY_MS = 300L
    }
}
