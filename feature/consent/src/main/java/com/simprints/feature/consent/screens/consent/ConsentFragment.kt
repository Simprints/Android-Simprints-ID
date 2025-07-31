package com.simprints.feature.consent.screens.consent

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.addCallback
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.simprints.core.DispatcherIO
import com.simprints.core.livedata.LiveDataEventObserver
import com.simprints.feature.consent.ConsentParams
import com.simprints.feature.consent.R
import com.simprints.feature.consent.databinding.FragmentConsentBinding
import com.simprints.feature.consent.databinding.FragmentConsentOldBinding
import com.simprints.feature.consent.screens.consent.tempocr.BuildAnalyzerUseCase
import com.simprints.feature.consent.screens.consent.tempocr.CropBitmapUseCase
import com.simprints.feature.consent.screens.consent.tempocr.NormalizeBitmapToPreviewUseCase
import com.simprints.feature.consent.screens.consent.tempocr.OcrBoxMapperUseCase
import com.simprints.feature.consent.screens.consent.tempocr.OcrState
import com.simprints.feature.consent.screens.consent.tempocr.OcrUIModel
import com.simprints.feature.consent.screens.consent.tempocr.OcrViewModel
import com.simprints.feature.consent.screens.consent.tempocr.PerformOcrUseCase
import com.simprints.feature.exitform.ExitFormContract
import com.simprints.feature.exitform.ExitFormResult
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ORCHESTRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.listeners.OnTabSelectedListener
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.navigation.handleResult
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.navigation.navigationParams
import com.simprints.infra.uibase.view.applySystemBarInsets
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.getValue
import com.simprints.infra.resources.R as IDR

@AndroidEntryPoint
internal class ConsentFragment : Fragment(R.layout.fragment_consent) {
    private val binding by viewBinding(FragmentConsentBinding::bind)
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageAnalysis: ImageAnalysis
    private var imageCapture: ImageCapture? = null
    private var cameraControl: androidx.camera.core.CameraControl? = null
    private val viewModel by viewModels<OcrViewModel>()

    @Inject
    @DispatcherIO
    lateinit var ioDispatcher: CoroutineDispatcher

    @Inject
    lateinit var performOcrUseCase: PerformOcrUseCase

    @Inject
    lateinit var cropBitmapUseCase: CropBitmapUseCase

    @Inject
    lateinit var normalizeBitmapToPreviewUseCase: NormalizeBitmapToPreviewUseCase

    @Inject
    lateinit var buildAnalyzerUseCase: BuildAnalyzerUseCase

    @Inject
    lateinit var ocrBoxMapperUseCase: OcrBoxMapperUseCase

    private var overlay: View? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        applySystemBarInsets(view)
        Simber.i("ConsentFragment started", tag = ORCHESTRATION)

        initCardViewFinder()
        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera()
        binding.buttonCapture.setOnClickListener {
            when (viewModel.uiModelState.value) {
                is OcrUIModel.ScanningInProgress -> viewModel.stopOcr()
                else -> viewModel.startOcr()
            }
        }
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.uiModelState.observe(viewLifecycleOwner) { state ->
            when (state) {
                OcrUIModel.NotScanning -> renderNotScanning()
                is OcrUIModel.ScanningInProgress -> renderScanningInProgress(state)
                is OcrUIModel.Success -> renderSuccessScan(state)
            }
        }
        viewModel.ocrState.observe(viewLifecycleOwner) { state ->
            if (state == null) return@observe
            when (state) {
                is OcrState.Running -> startOcr(state)
                OcrState.Stopped -> finishOcr()
            }
        }
    }

    private fun finishOcr() {
        imageAnalysis.clearAnalyzer()
        removeOCRBoundingBox()
    }

    private fun startOcr(state: OcrState.Running) {
        buildAnalyzerUseCase(everyNthFrame = state.everyNthFrame) { bitmap: Bitmap, rotationDegrees: Int ->
            lifecycleScope.launch(ioDispatcher) {
                val (previewWidth, previewHeight) = withContext(Dispatchers.Main) {
                    binding.preview.width to binding.preview.height
                }
                val cutoutRect = withContext(Dispatchers.Main) {
                    getBoundsRelativeTo(parent = binding.preview, child = binding.viewfinderOverlay)
                }
                val normalizedBitmap = normalizeBitmapToPreviewUseCase(
                    inputBitmap = bitmap,
                    rotationDegrees = rotationDegrees,
                    previewViewWidth = previewWidth,
                    previewViewHeight = previewHeight
                )
                val cropped = cropBitmapUseCase(bitmap = normalizedBitmap, cutoutRect = cutoutRect)
                val ocrResult = performOcrUseCase(cropped) { readoutString ->
                    with(readoutString.trim().replace(" ", "")) {
                        isDigitsOnly() && length == 8
                    } // NHIS membership
                }
                if (ocrResult != null) {
                    withContext(Dispatchers.Main) {
                        val normalizedText = ocrResult.text.trim().replace(" ", "")
                        viewModel.addOcrReadout(text = normalizedText, ocrResult.boundingBox)
                    }
                }
            }
        }.also {
            imageAnalysis.setAnalyzer(cameraExecutor, it)
        }
    }

    private fun renderNotScanning() {
        binding.buttonCapture.text = "Start"
        binding.viewfinderMask.backgroundColor = "#88000000"
    }

    private fun renderSuccessScan(state: OcrUIModel.Success) {
        renderNotScanning()
        binding.ocrText.text = "${state.readoutValue} (Done in ${state.totalFramesProcessed} reads)"
        viewModel.stopOcr()
    }

    private fun renderScanningInProgress(state: OcrUIModel.ScanningInProgress) {
        binding.buttonCapture.text = "Stop"
        binding.viewfinderMask.backgroundColor = "#88aaaaaa"
        binding.ocrText.text = "${state.readoutValue} (${state.successfulReadouts}/${state.totalReadoutsRequired} reads)"
        drawOCRBoundingBox(state.rawBox)
    }

    private fun removeOCRBoundingBox() {
        overlay?.let { binding.root.removeView(it) }
        overlay = null
    }

    private fun drawOCRBoundingBox(boundingBox: Rect) = {
        overlay?.let { binding.root.removeView(it) }
        val location = IntArray(2)
        binding.cardViewLayout.getLocationInWindow(location)
        val statusBarHeight = resources.getIdentifier("status_bar_height", "dimen", "android")
            .takeIf { it > 0 }
            ?.let { resources.getDimensionPixelSize(it) } ?: 0
        val screenRect = ocrBoxMapperUseCase(boundingBox, location[0], location[1] - statusBarHeight)

        overlay = object : View(context) {
            override fun onDraw(canvas: Canvas) {
                super.onDraw(canvas)
                val paint = Paint().apply {
                    color = Color.RED
                    style = Paint.Style.STROKE
                    strokeWidth = 4f
                }
                canvas.drawRect(screenRect, paint)
            }
        }.apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        binding.root.addView(overlay)
    }


    private fun getBoundsRelativeTo(parent: View, child: View): Rect {
        val childLocation = IntArray(2)
        val parentLocation = IntArray(2)
        child.getLocationOnScreen(childLocation)
        parent.getLocationOnScreen(parentLocation)

        val offsetX = childLocation[0] - parentLocation[0]
        val offsetY = childLocation[1] - parentLocation[1]

        return Rect(
            offsetX,
            offsetY,
            offsetX + child.width,
            offsetY + child.height
        )
    }


    private fun initCardViewFinder() = binding.preview.post {
        val overlay = binding.cardViewLayout
        val root = binding.root

        val overlayLocation = IntArray(2)
        val rootLocation = IntArray(2)

        overlay.getLocationOnScreen(overlayLocation)
        root.getLocationOnScreen(rootLocation)

        val x = (overlayLocation[0] - rootLocation[0]).toFloat()
        val y = (overlayLocation[1] - rootLocation[1]).toFloat()

        val width = overlay.width.toFloat()
        val height = overlay.height.toFloat()

        binding.viewfinderMask.cutoutRect = RectF(x, y, x + width, y + height)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .build().also {
                    it.setSurfaceProvider(binding.preview.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9) // Match preview
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            imageAnalysis = ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageCapture, imageAnalysis
                )
                cameraControl = camera.cameraControl
            } catch (e: Exception) {
                Simber.e("Camera binding failed in OCR", e)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }
}

@AndroidEntryPoint
internal class ConsentFragment_old : Fragment(R.layout.fragment_consent_old) {
    private val args: ConsentParams by navigationParams()
    private val binding by viewBinding(FragmentConsentOldBinding::bind)
    private val viewModel by viewModels<ConsentViewModel>()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        applySystemBarInsets(view)
        Simber.i("ConsentFragment started", tag = ORCHESTRATION)

        binding.consentPrivacyNotice.paintFlags = binding.consentPrivacyNotice.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        binding.consentTextHolderView.movementMethod = ScrollingMovementMethod()

        handleClicks()
        observeState()

        findNavController().handleResult<ExitFormResult>(
            viewLifecycleOwner,
            R.id.consentFragment,
            ExitFormContract.DESTINATION,
        ) { viewModel.handleExitFormResponse(it) }

        viewModel.loadConfiguration(args.type)
    }

    private fun handleClicks() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            viewModel.declineClicked(getCurrentConsentTab())
        }

        binding.consentAcceptButton.setOnClickListener {
            viewModel.acceptClicked(getCurrentConsentTab())
        }
        binding.consentDeclineButton.setOnClickListener {
            viewModel.declineClicked(getCurrentConsentTab())
        }
        binding.consentPrivacyNotice.setOnClickListener { openPrivacyNotice() }
    }

    private fun observeState() {
        viewModel.viewState.observe(viewLifecycleOwner) { state ->
            if (state != null) updateUiWithState(state)
        }
        viewModel.showExitForm.observe(
            viewLifecycleOwner,
            LiveDataEventObserver {
                findNavController().navigateSafely(
                    currentFragment = this,
                    actionId = R.id.action_consentFragment_to_refusalFragment,
                )
            },
        )
        viewModel.returnConsentResult.observe(viewLifecycleOwner) { isApproved ->
            isApproved
                .getContentIfNotHandled()
                ?.let { findNavController().finishWithResult(this, it) }
        }
    }

    private fun openPrivacyNotice() {
        findNavController().navigateSafely(
            this,
            ConsentFragmentDirections.actionConsentFragmentToPrivacyNoticeFragment(),
        )
    }

    private fun updateUiWithState(state: ConsentViewState) {
        binding.consentLogo.isVisible = state.showLogo

        val generalText = state.consentTextBuilder?.assembleText(requireActivity()).orEmpty()
        val parentText = state.parentalTextBuilder?.assembleText(requireActivity()).orEmpty()

        // setup initial text to general consent
        binding.consentTextHolderView.text = generalText

        with(binding.consentTabHost) {
            // Fully reset tab state
            removeAllTabs()
            clearOnTabSelectedListeners()
            addTab(newTab().setText(IDR.string.consent_general_title), GENERAL_CONSENT_TAB)
            if (state.showParentalConsent) {
                addParentalConsentTab(generalText, parentText)
            }
            getTabAt(state.selectedTab)?.select()
        }
    }

    private fun TabLayout.addParentalConsentTab(
        generalConsentText: String,
        parentalConsentText: String,
    ) {
        addTab(newTab().setText(IDR.string.consent_parental_title), PARENTAL_CONSENT_TAB)
        addOnTabSelectedListener(
            OnTabSelectedListener { tab ->
                val position = tab.position
                val (consentText, tabIndex) = when (position) {
                    PARENTAL_CONSENT_TAB -> parentalConsentText to PARENTAL_CONSENT_TAB
                    else -> generalConsentText to GENERAL_CONSENT_TAB
                }
                binding.consentTextHolderView.text = consentText
                viewModel.setSelectedTab(tabIndex)
            },
        )
    }

    private fun getCurrentConsentTab() = when (binding.consentTabHost.selectedTabPosition) {
        GENERAL_CONSENT_TAB -> ConsentTab.INDIVIDUAL
        PARENTAL_CONSENT_TAB -> ConsentTab.PARENTAL
        else -> throw IllegalStateException("Invalid consent tab selected")
    }

    companion object {
        private const val GENERAL_CONSENT_TAB = 0
        private const val PARENTAL_CONSENT_TAB = 1
    }
}
