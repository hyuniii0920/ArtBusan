package com.example.artbusan

import android.Manifest
import android.content.pm.PackageManager
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
import com.example.artbusan.network.ArtworkExperience
import com.example.artbusan.network.ArtworkExperienceRepository
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ArViewerActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var artworkRepository: ArtworkExperienceRepository
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

    private var isDetailExpanded = false

    private val scanner by lazy {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )
    }

    private val cameraProviderFuture by lazy { ProcessCameraProvider.getInstance(this) }

    @Volatile
    private var scanningEnabled = false

    @Volatile
    private var processingScan = false

    private var lastScannedValue: String? = null
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var currentArtwork: ArtworkExperience? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startCameraPreview()
        } else {
            tvStatus.text = "CAMERA BLOCKED"
            tvArHint.text = "카메라 권한이 필요합니다."
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_viewer)

        artworkRepository = ArtworkExperienceRepository(applicationContext)
        cameraExecutor = Executors.newSingleThreadExecutor()

        bindViews()
        setupSheet()
        setupActions()
        bindInitialUi()

        tts = TextToSpeech(this, this)
    }

    override fun onResume() {
        super.onResume()
        if (!hasCameraPermission()) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            startCameraPreview()
        }
    }

    override fun onPause() {
        super.onPause()
        if (cameraProviderFuture.isDone) {
            cameraProviderFuture.get().unbindAll()
        }
    }

    override fun onDestroy() {
        scanner.close()
        cameraExecutor.shutdown()
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.KOREAN)
            isTtsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                result != TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    private fun bindViews() {
        findViewById<ImageButton>(R.id.btnArBack).setOnClickListener { finish() }
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

    private fun setupSheet() {
        bottomSheet.isVisible = false
        detailGroup.isVisible = false
        updateSheetExpanded(false)
    }

    private fun setupActions() {
        btnScanQr.setOnClickListener {
            currentArtwork = null
            lastScannedValue = null
            bottomSheet.isVisible = false
            updateSheetExpanded(false)
            enterScanningMode()
            scanningEnabled = true
            tvStatus.text = "SCANNING QR"
            tvArHint.text = "카메라를 QR 코드에 맞추면 하단에 작품 설명이 바로 나타납니다."
        }
        btnRescan.setOnClickListener {
            currentArtwork = null
            lastScannedValue = null
            bottomSheet.isVisible = false
            updateSheetExpanded(false)
            enterScanningMode()
            scanningEnabled = true
            tvStatus.text = "SCANNING QR"
            tvArHint.text = "다른 QR을 계속 스캔할 수 있습니다."
        }
        btnSeeMore.setOnClickListener {
            updateSheetExpanded(!isDetailExpanded)
        }
        btnTts.setOnClickListener { speakDetailDescription() }
        btnArMode.setOnClickListener { enterArMode() }
    }

    private fun bindInitialUi() {
        tvStatus.text = "QR READY"
        tvArHint.text = "AR 실행 후 먼저 QR 스캔 안내를 확인하고, 스캔 시작을 누르면 카메라 스캔 화면으로 전환됩니다."
        tvPeekTitle.text = "작품을 스캔해 주세요"
        tvPeekSummary.text = "artar://work/1 같은 QR을 읽으면 하단 설명창과 상세보기를 확인할 수 있습니다."
        tvArModeBadge.isVisible = false
        tvArModeGuide.text = "작품을 스캔하면 요약 설명이 먼저 보이고, 상세보기로 전체 설명과 TTS를 확인할 수 있습니다."
        bottomSheet.isVisible = false
        detailGroup.isVisible = false
        preScanGuideGroup.isVisible = true
        scanFrame.isVisible = false
        previewView.isVisible = false
        dimOverlay.alpha = 1f
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

    private fun handleBarcodeResult(barcodes: List<Barcode>) {
        val rawValue = barcodes.firstNotNullOfOrNull {
            it.rawValue?.trim()?.takeIf(String::isNotBlank)
        } ?: return

        if (rawValue == lastScannedValue) {
            return
        }

        val artworkId = parseArtworkId(rawValue)
        if (artworkId == null) {
            tvStatus.text = "INVALID QR"
            tvArHint.text = "지원 형식: artar://work/102"
            return
        }

        lastScannedValue = rawValue
        scanningEnabled = false
        loadArtwork(artworkId)
    }

    private fun loadArtwork(artworkId: Int) {
        tvStatus.text = "LOADING #$artworkId"
        tvArHint.text = "작품 정보를 불러오는 중입니다."

        lifecycleScope.launch {
            runCatching {
                artworkRepository.getArtwork(artworkId)
            }.onSuccess { artwork ->
                currentArtwork = artwork
                bindArtwork(artwork)
            }.onFailure {
                tvStatus.text = "LOAD FAILED"
                tvArHint.text = "작품 정보를 가져오지 못했습니다. 다시 스캔해 주세요."
                Toast.makeText(this@ArViewerActivity, "작품 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                lastScannedValue = null
                scanningEnabled = true
            }
        }
    }

    private fun bindArtwork(artwork: ArtworkExperience) {
        tvStatus.text = "WORK ${artwork.id}"
        tvArHint.text = "카메라는 켜진 상태입니다. 아래 요약 설명을 보다가 필요하면 상세보기로 확장하세요."

        tvPeekTitle.text = artwork.title
        tvPeekSummary.text = artwork.summaryDescription
        tvDetailTitle.text = artwork.title
        tvArtist.text = "작가: ${artwork.artist}"
        tvDetailBody.text = artwork.detailDescription
        tvArModeGuide.text = "카메라를 유지한 채 요약 설명을 볼 수 있고, 상세보기를 누르면 긴 설명이 펼쳐집니다."

        previewView.isVisible = true
        scanFrame.isVisible = false
        dimOverlay.isVisible = true
        dimOverlay.alpha = 0.22f
        bottomSheet.isVisible = true
        bottomSheet.bringToFront()
        bottomSheet.requestLayout()
        tvArModeBadge.isVisible = false
        updateSheetExpanded(false)
    }

    private fun speakDetailDescription() {
        val artwork = currentArtwork ?: return
        if (!isTtsReady) {
            Toast.makeText(this, "TTS가 아직 준비되지 않았습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        tts?.stop()
        tts?.speak(artwork.detailDescription, TextToSpeech.QUEUE_FLUSH, null, "artar-detail")
    }

    private fun enterArMode() {
        val artwork = currentArtwork ?: run {
            Toast.makeText(this, "먼저 작품 QR을 스캔해 주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        updateSheetExpanded(false)
        tvArModeBadge.isVisible = true
        tvArModeBadge.text = "AR MODE"
        tvArModeGuide.text = "${artwork.title} 작품의 AR 모드가 활성화되었습니다. 현재는 설명창을 접은 상태에서 AR 전환 UX까지 연결되어 있습니다."
        tvStatus.text = "AR ACTIVE"
    }

    private fun enterScanningMode() {
        previewView.isVisible = true
        dimOverlay.isVisible = true
        dimOverlay.alpha = 0.12f
        preScanGuideGroup.isVisible = false
        scanFrame.isVisible = true
    }

    private fun updateSheetExpanded(expanded: Boolean) {
        isDetailExpanded = expanded
        detailGroup.isVisible = expanded
        btnSeeMore.text = if (expanded) "요약으로 보기" else "상세보기"
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

    private fun parseArtworkId(rawValue: String): Int? {
        Regex("""^artar://work/(\d+)$""").find(rawValue)?.let {
            return it.groupValues[1].toIntOrNull()
        }
        Regex("""[?&]id=(\d+)""").find(rawValue)?.let {
            return it.groupValues[1].toIntOrNull()
        }
        Regex("""(?:work|artwork|venue)/(\d+)""").find(rawValue)?.let {
            return it.groupValues[1].toIntOrNull()
        }
        return rawValue.toIntOrNull()
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val EXTRA_MUSEUM_ID = "extra_museum_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_CATEGORY = "extra_category"
        const val EXTRA_LOCATION = "extra_location"
    }
}
