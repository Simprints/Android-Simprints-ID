package com.simprints.feature.externalcredential.screens.select.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.externalcredential.ExternalCredentialType
import com.simprints.feature.externalcredential.R
import com.simprints.feature.externalcredential.databinding.ItemDocumentBinding
import com.simprints.infra.resources.R as IDR

@ExcludedFromGeneratedTestCoverageReports("UI classes are not unit tested")
internal class ExternalCredentialTypeAdapter(
    private val items: List<ExternalCredentialType>,
    private val onClick: (ExternalCredentialType) -> Unit = {}
) : RecyclerView.Adapter<ExternalCredentialTypeAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder = ViewHolder(ItemDocumentBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemDocumentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(credentialType: ExternalCredentialType) {
            val c = binding.root.context
            val credentialTypeText = when (credentialType) {
                ExternalCredentialType.NHISCard -> IDR.string.mfid_type_nhis_card
                ExternalCredentialType.GhanaIdCard -> IDR.string.mfid_type_ghana_id_card
                ExternalCredentialType.QRCode -> IDR.string.mfid_type_qr_code
            }.run(c::getString)
            val text = c.resources.getQuantityString(IDR.plurals.mfid_scan_action, 1, credentialTypeText)
            val image = when (credentialType) {
                ExternalCredentialType.NHISCard -> R.drawable.ghana_nhis_card
                ExternalCredentialType.GhanaIdCard -> R.drawable.ghana_id_card
                ExternalCredentialType.QRCode -> R.drawable.qr_code
            }
            binding.documentText.text = text
            binding.documentImage.setImageResource(image)
            binding.root.setOnClickListener { onClick(credentialType) }
        }
    }
}
