package com.simprints.feature.troubleshooting.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.feature.troubleshooting.databinding.ItemTroubleshootingListBinding
import com.simprints.infra.uibase.system.Clipboard

@ExcludedFromGeneratedTestCoverageReports("UI classes are not unit tested")
internal class TroubleshootingListAdapter(
    private val items: List<TroubleshootingItemViewData>,
    private val onMoreClick: (String) -> Unit = {},
) : RecyclerView.Adapter<TroubleshootingListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemTroubleshootingListBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    @ExcludedFromGeneratedTestCoverageReports("UI classes are not unit tested")
    inner class ViewHolder(
        private val binding: ItemTroubleshootingListBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TroubleshootingItemViewData) {
            binding.troubleshootingItemTitle.text = item.title
            binding.troubleshootingItemSubtitle.text = item.subtitle
            binding.troubleshootingItemBody.text = item.body

            binding.troubleshootingItemButton.isVisible = item.navigationId != null
            binding.troubleshootingItemButton.setOnClickListener {
                item.navigationId?.let(onMoreClick)
            }

            binding.troubleshootingItemCopy.setOnClickListener {
                val context = binding.root.context
                Clipboard.copyToClipboard(context, "${item.title}\n${item.subtitle}\n${item.body}")
                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
