package com.simprints.feature.externalcredential.view

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.feature.externalcredential.R
import com.simprints.feature.externalcredential.databinding.DialogScannedCredentialBinding
import com.simprints.feature.externalcredential.databinding.ItemScannedImageBinding
import com.simprints.feature.externalcredential.ext.getCredentialFieldTitle
import com.simprints.feature.externalcredential.ext.getCredentialTypeString
import com.simprints.feature.externalcredential.screens.search.model.ScannedCredential
import com.simprints.infra.resources.R as IDR

@ExcludedFromGeneratedTestCoverageReports("UI class")
class ScannedCredentialDialog(
    context: Context,
    private val credential: ScannedCredential,
    private val displayedCredential: TokenizableString.Raw,
    private val onConfirm: () -> Unit,
    private val onSkip: () -> Unit,
) : BottomSheetDialog(context) {
    private val binding: DialogScannedCredentialBinding =
        DialogScannedCredentialBinding.inflate(LayoutInflater.from(context))

    init {
        setContentView(binding.root)
        setCancelable(false)
        setCanceledOnTouchOutside(false)

        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isDraggable = false
        behavior.skipCollapsed = true

        initViews()
        setupRecyclerView()
    }

    private fun initViews() = with(binding) {
        val credentialType = credential.credentialType
        val credential = context.resources.getCredentialTypeString(credentialType)
        val credentialField = context.resources.getCredentialFieldTitle(credentialType)

        documentField.text = credentialField
        title.text = context.getString(IDR.string.mfid_add_document_title, credential)
        credentialValue.text = displayedCredential.value
        confirmCredentialCheckbox.text =
            context.getString(IDR.string.mfid_confirmation_checkbox_text, credentialField)
        confirmCredentialCheckbox.setOnCheckedChangeListener { _, isChecked ->
            buttonConfirm.isEnabled = isChecked
        }
        buttonSkip.setOnClickListener { onSkip() }
        buttonConfirm.setOnClickListener { onConfirm() }
    }

    private fun setupRecyclerView() = with(binding) {
        val imagePaths = buildList {
            credential.zoomedCredentialImagePath?.let { add(it) }
            this@ScannedCredentialDialog.credential.documentImagePath?.let { add(it) }
        }

        if (imagePaths.isEmpty()) {
            documentsRecyclerView.isVisible = false
        } else {
            documentsRecyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            documentsRecyclerView.adapter = ImageAdapter(imagePaths)
            documentsRecyclerView.overScrollMode = View.OVER_SCROLL_NEVER
            documentsRecyclerView.isVisible = true
        }
    }

    @ExcludedFromGeneratedTestCoverageReports("UI class")
    private inner class ImageAdapter(
        private val imagePaths: List<String>,
    ) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): ImageViewHolder {
            val view = LayoutInflater
                .from(parent.context)
                .inflate(R.layout.item_scanned_image, parent, false)
            return ImageViewHolder(view)
        }

        override fun onBindViewHolder(
            holder: ImageViewHolder,
            position: Int,
        ) {
            holder.bind(imagePaths[position], position)
        }

        override fun getItemCount() = imagePaths.size

        @ExcludedFromGeneratedTestCoverageReports("UI class")
        inner class ImageViewHolder(
            itemView: View,
        ) : RecyclerView.ViewHolder(itemView) {
            private val binding = ItemScannedImageBinding.bind(itemView)

            fun bind(
                imagePath: String,
                position: Int,
            ) = with(binding) {
                val bitmap = BitmapFactory.decodeFile(imagePath)
                documentImage.setImageBitmap(bitmap)

                itemView.post {
                    val recyclerWidth = (itemView.parent as? RecyclerView)?.width ?: 0
                    if (recyclerWidth > 0) {
                        val imageWidth = (recyclerWidth * 0.8).toInt()
                        val imageHeight = (imageWidth * 10 / 16)
                        val layoutParams = documentImage.layoutParams
                        layoutParams.width = imageWidth
                        layoutParams.height = imageHeight
                        documentImage.layoutParams = layoutParams
                    }
                }

                val params = itemView.layoutParams as ViewGroup.MarginLayoutParams
                if (position == imagePaths.size - 1) {
                    params.marginEnd = context.resources.getDimensionPixelSize(IDR.dimen.margin_large)
                }
                itemView.layoutParams = params
            }
        }
    }
}
