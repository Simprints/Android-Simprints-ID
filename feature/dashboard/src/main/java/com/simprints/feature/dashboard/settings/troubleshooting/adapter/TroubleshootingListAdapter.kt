package com.simprints.feature.dashboard.settings.troubleshooting.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.feature.dashboard.databinding.ItemTroubleshootingListBinding


@ExcludedFromGeneratedTestCoverageReports("UI classes are not unit tested")
internal class TroubleshootingListAdapter(
    private val items: List<TroubleshootingItemViewData>,
    private val onMoreClick: (String) -> Unit = {},
) : RecyclerView.Adapter<TroubleshootingListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemTroubleshootingListBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    @ExcludedFromGeneratedTestCoverageReports("UI classes are not unit tested")
    inner class ViewHolder(
        private val binding: ItemTroubleshootingListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(event: TroubleshootingItemViewData) {
            binding.troubleshootingItemTitle.text = event.title
            binding.troubleshootingItemSubtitle.text = event.subtitle
            binding.troubleshootingItemBody.text = event.body

            binding.troubleshootingItemButton.isVisible = event.navigationId != null
            binding.troubleshootingItemButton.setOnClickListener {
                event.navigationId?.let(onMoreClick)
            }
        }
    }
}
