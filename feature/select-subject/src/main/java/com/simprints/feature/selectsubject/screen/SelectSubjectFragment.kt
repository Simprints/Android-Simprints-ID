package com.simprints.feature.selectsubject.screen

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.simprints.core.livedata.LiveDataEventWithContentObserver
import com.simprints.feature.selectsubject.R
import com.simprints.feature.selectsubject.SelectSubjectResult
import com.simprints.feature.selectsubject.databinding.FragmentSelectSubjectBinding
import com.simprints.feature.selectsubject.model.SubjectIdSaveResult
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ORCHESTRATION
import com.simprints.infra.logging.Simber
import com.simprints.infra.uibase.navigation.finishWithResult
import com.simprints.infra.uibase.view.applySystemBarInsets
import com.simprints.infra.uibase.viewbinding.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class SelectSubjectFragment : Fragment(R.layout.fragment_select_subject) {
    private val viewModel: SelectSubjectViewModel by viewModels()
    private val args: SelectSubjectFragmentArgs by navArgs()
    private val binding by viewBinding(FragmentSelectSubjectBinding::bind)
    private var dialog: Dialog? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        applySystemBarInsets(view)
        Simber.i("SelectSubjectFragment started", tag = ORCHESTRATION)

        viewModel.confirmIdentitySaveResultLiveData.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { saveResult ->
                processSaveResult(saveResult)
            }
        }
        viewModel.finish.observe(viewLifecycleOwner, LiveDataEventWithContentObserver {
            displayExternalCredentialSaveAnimation {
                binding.confirmationSentLayout.isVisible = true
                finishWithResult(isSubjectIdSaved = true)
            }
        })

        viewModel.saveGuidSelection(
            projectId = args.projectId,
            subjectId = args.subjectId,
            externalCredentialId = args.externalCredentialId
        )
    }

    override fun onDestroyView() {
        dialog?.dismiss()
        dialog = null
        super.onDestroyView()
    }

    private fun displayExternalCredentialSaveAnimation(onComplete: () -> Unit) {
        val saveCredentialProgressLayout = requireView().findViewById<View>(R.id.saveCredentialProgressLayout)
        saveCredentialProgressLayout.isVisible = true

        val progressBar = requireView().findViewById<LinearProgressIndicator>(R.id.saveProgressBar)
        val animator = ObjectAnimator.ofInt(progressBar, "progress", 0, 100)
        animator.duration = 1500

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onComplete()
            }
        })
        animator.start()
    }

    private fun processSaveResult(saveResult: SubjectIdSaveResult) {
        binding.processing.isVisible = false
        if (!saveResult.isSubjectIdSaved || !saveResult.shouldDisplaySaveCredentialDialog) {
            binding.confirmationSentLayout.isVisible = saveResult.isSubjectIdSaved
            finishWithResult(saveResult.isSubjectIdSaved)
            return
        } else {
            processExternalCredential(externalCred = args.externalCredentialId, externalCredImagePath = args.externalCredentialImagePath)
        }
    }

    private fun processExternalCredential(externalCred: String?, externalCredImagePath: String?) {
        if (externalCred == null) {
            finishWithResult(isSubjectIdSaved = true)
        } else {
            displayExternalCredentialPreviewDialog(
                externalCred = externalCred,
                externalCredImagePath = externalCredImagePath,
                onConfirm = {
                    dialog?.dismiss()
                    dialog = null
                    viewModel.saveExternalCredential(externalCred = externalCred, subjectId = args.subjectId)
                },
                onSkip = {
                    dialog?.dismiss()
                    dialog = null
                    finishWithResult(isSubjectIdSaved = true)
                },
            )
        }
    }

    private fun finishWithResult(isSubjectIdSaved: Boolean) {
        // [MS-985] Callout apps do not care about external credential saving result, they do not process it as of May 2025.
        // Refactor to pass the external credential save result if necessary.
        findNavController().finishWithResult(this, SelectSubjectResult(isSubjectIdSaved))
    }

    private fun displayExternalCredentialPreviewDialog(
        externalCred: String,
        externalCredImagePath: String?,
        onConfirm: () -> Unit,
        onSkip: () -> Unit
    ) {
        dialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_external_credential_preview, null)
        val confirmButton = view.findViewById<Button>(R.id.buttonConfirm)
        val skipButton = view.findViewById<Button>(R.id.buttonSkip)
        val credentialText = view.findViewById<TextView>(R.id.externalCredentialText)
        val imageCardPreview = view.findViewById<ImageView>(R.id.imageCardPreview)
        val confirmCheckbox = view.findViewById<CheckBox>(R.id.confirmCredentialCheckbox)

        credentialText.text = externalCred
        getImage(externalCredImagePath).let { image ->
            imageCardPreview.isVisible = image != null
            if (image != null) {
                imageCardPreview.setImageBitmap(image)
            }
        }

        confirmCheckbox.setOnCheckedChangeListener { _, isChecked ->
            confirmButton.isEnabled = isChecked
        }

        confirmButton.setOnClickListener { onConfirm() }
        skipButton.setOnClickListener { onSkip() }

        dialog?.setContentView(view)
        dialog?.setCancelable(false)
        dialog?.show()
        (dialog as? BottomSheetDialog)?.apply {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = false
        }

    }

    private fun getImage(path: String?): Bitmap? =
        try {
            path?.run(BitmapFactory::decodeFile)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Unable to get [$path] preview image: ${e.message}", Toast.LENGTH_LONG).show()
            Simber.e("Unable to get [$path] image", e)
            null
        }
}
