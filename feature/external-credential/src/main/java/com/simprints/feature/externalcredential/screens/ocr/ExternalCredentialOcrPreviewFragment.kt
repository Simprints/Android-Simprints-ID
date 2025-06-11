package com.simprints.feature.externalcredential.screens.ocr

import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.addCallback
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.simprints.core.DispatcherIO
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.feature.externalcredential.R
import com.simprints.feature.externalcredential.databinding.FragmentExternalCredentialOcrPreviewBinding
import com.simprints.feature.externalcredential.screens.controller.ExternalCredentialViewModel
import com.simprints.feature.externalcredential.screens.ocr.model.OcrDocument
import com.simprints.feature.externalcredential.screens.ocr.model.OcrPreprocessData
import com.simprints.feature.externalcredential.screens.ocr.model.OcrPreviewState
import com.simprints.feature.externalcredential.screens.ocr.model.OcrScanParams
import com.simprints.feature.externalcredential.screens.ocr.viewmodel.OcrPreviewViewModel
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.navigateSafely
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

@AndroidEntryPoint
class ExternalCredentialOcrPreviewFragment :
    Fragment(R.layout.fragment_external_credential_ocr_preview) {
    private val args: ExternalCredentialOcrPreviewFragmentArgs by navArgs()
    private val binding by viewBinding(FragmentExternalCredentialOcrPreviewBinding::bind)
    private val flowViewModel: ExternalCredentialViewModel by activityViewModels()
    private val ocrViewModel: OcrPreviewViewModel by viewModels()
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private var dialog: Dialog? = null
    private var cameraControl: androidx.camera.core.CameraControl? = null
    private var torchEnabled = false

    @Inject
    @DispatcherIO
    lateinit var ioDispatcher: CoroutineDispatcher

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initCardViewFinder()
        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera()
        binding.buttonCapture.setOnClickListener {
            takePhoto()
        }
        observeState()
        validateArgs()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            flowViewModel.recapture()
        }
        binding.buttonFlash.isVisible = requireContext().packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
        binding.buttonFlash.setOnClickListener {
            cameraControl?.enableTorch(!torchEnabled)
            torchEnabled = !torchEnabled
            renderTorchIcon(torchEnabled)
        }
        renderTorchIcon(torchEnabled)
    }

    override fun onPause() {
        turnTorchOff()
        super.onPause()
    }

    override fun onDestroy() {
        deleteCachedPhoto(ocrViewModel.imageFileName)
        dialog?.dismiss()
        super.onDestroy()
    }

    private fun createRoundRippleBackground(colorResId: Int): RippleDrawable {
        val baseColor = ResourcesCompat.getColor(resources, colorResId, null)
        val shape = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(baseColor)
        }

        val rippleColor = ColorStateList.valueOf(
            ResourcesCompat.getColor(resources, android.R.color.darker_gray, null)
        )

        return RippleDrawable(rippleColor, shape, null)
    }

    private fun renderTorchIcon(isTorchEnabled: Boolean) {
        binding.buttonFlash.setImageResource(
            if (isTorchEnabled) R.drawable.ic_flashlight_on_24_white else R.drawable.ic_flashlight_on_24_grey
        )
        val bgColor = if (isTorchEnabled)
            com.simprints.infra.resources.R.color.simprints_orange
        else com.simprints.infra.resources.R.color.simprints_off_white
        binding.buttonFlash.background = createRoundRippleBackground(bgColor)
    }

    private fun validateArgs() {
        // In case custom document is provided for OCR it is important that at least one filed is used as the external credential
        // in the 'search & verify' flow.
        when (val ocrDocument = args.ocrParams.ocrDocument) {
            is OcrDocument.Custom -> {
                if (ocrDocument.fieldIds.none { it.isExternalCredentialId }) {
                    renderErrorState(
                        OcrPreviewState.Error(
                            """
                Dev error: no field in the arguments is specified as external credential ID. Make sure that the at least one argument is 
                set as an identifier for the external credential. 
                """.trim()
                        )
                    )
                }
            }

            else -> {
                /*do nothing, predefined OCR document types have the external credential field set*/
            }
        }
    }

    private fun saveImageAndProceed(image: Bitmap) {
        lifecycleScope.launch {
            try {
                val path = savePreprocessedImageToCache(image, requireContext())
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    navigateToOcrScanning(preprocessedImagePath = path)
                }
            } catch (e: Exception) {
                Simber.e("Cannot save OCR image and proceed", e)
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    renderErrorState(OcrPreviewState.Error("Error processing image: ${e.message}"))
                }
            }
        }
    }

    private fun observeState() {
        ocrViewModel.stateLiveData.observe(
            viewLifecycleOwner,
            LiveDataEventWithContentObserver { state ->
                binding.buttonFlash.isVisible = state is OcrPreviewState.Initial || state is OcrPreviewState.Error
                when (state) {
                    is OcrPreviewState.Error -> renderErrorState(state)
                    OcrPreviewState.Loading -> renderLoadingState(isLoading = true)
                    is OcrPreviewState.Success -> saveImageAndProceed(state.preprocessedImage)
                    OcrPreviewState.Initial -> renderInitialState()
                }
            })
    }

    private suspend fun savePreprocessedImageToCache(image: Bitmap, context: Context): String =
        withContext(ioDispatcher) {
            val imageFileName = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                .format(System.currentTimeMillis()) + "-preprocessed.jpg"
            val file = File(context.cacheDir, imageFileName)
            FileOutputStream(file).use { out ->
                image.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            return@withContext file.absolutePath
        }

    private fun renderInitialState() {
        dialog?.dismiss()
    }

    private fun renderErrorState(state: OcrPreviewState.Error) {
        renderLoadingState(false)
        dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_ocr_error, null)
        view.findViewById<View>(R.id.buttonClose).setOnClickListener { dialog?.dismiss() }
        view.findViewById<TextView>(R.id.errorMessage).text = state.message
        dialog?.setContentView(view)
        dialog?.show()
    }

    private fun renderLoadingState(isLoading: Boolean) {
        binding.loadingLayout.isVisible = isLoading
        binding.previewLayout.isVisible = !isLoading
    }

    private fun navigateToOcrScanning(preprocessedImagePath: String) {
        findNavController().navigateSafely(
            this,
            ExternalCredentialOcrPreviewFragmentDirections.actionExternalCredentialOcrPreviewToExternaCredentialOcrScan(
                ocrScanParams = OcrScanParams(
                    imagePath = preprocessedImagePath,
                    ocrParams = args.ocrParams
                )
            ),
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

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageCapture
                )
                cameraControl = camera.cameraControl
            } catch (e: Exception) {
                Simber.e("Camera binding failed in OCR", e)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        ocrViewModel.setLoadingState(isLoading = true)
        val imageCapture = imageCapture ?: return

        val imageFileName = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis()) + ".jpg"
        val photoFile = File(requireContext().cacheDir, imageFileName)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    turnTorchOff()
                    ocrViewModel.setLoadingState(isLoading = false)
                    Simber.e("Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    turnTorchOff()
                    val absolutePath = photoFile.absolutePath
                    val ocrPreprocessData = OcrPreprocessData(
                        previewViewWidthPx = binding.preview.width,
                        previewViewHeightPx = binding.preview.height,
                        cutoutRect = binding.viewfinderMask.cutoutRect!!
                    )
                    ocrViewModel.imageFileName = imageFileName
                    ocrViewModel.preprocessOcrImage(
                        image = BitmapFactory.decodeFile(absolutePath),
                        exif = ExifInterface(absolutePath),
                        ocrPreprocessData = ocrPreprocessData,
                        ocrDocument = args.ocrParams.ocrDocument
                    )
                }
            }
        )
    }

    private fun deleteCachedPhoto(fileName: String) {
        val photoFile = File(requireContext().cacheDir, fileName)
        if (photoFile.exists()) {
            val deleted = photoFile.delete()
            if (!deleted) {
                Simber.w("Failed to delete cached photo: ${photoFile.absolutePath}")
            }
        } else {
            Simber.w("Cached photo not found: ${photoFile.absolutePath}")
        }
    }

    private fun turnTorchOff() {
        cameraControl?.enableTorch(false)
        torchEnabled = false
        renderTorchIcon(false)
    }
}
