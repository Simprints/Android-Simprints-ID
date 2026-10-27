package com.simprints.feature.troubleshooting.applogs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.simprints.feature.troubleshooting.databinding.AppLogHeaderBinding

/**
 * The top of the App logs screen: device details, problem summary, search and filters.
 * It is the first row of the list, so it scrolls together with the log entries.
 */
internal class AppLogHeaderAdapter(
    private val onQueryChanged: (String) -> Unit,
    private val onGroupsChanged: (Set<String>) -> Unit,
) : RecyclerView.Adapter<AppLogHeaderAdapter.ViewHolder>() {
    private var deviceInfo = ""
    private var summary = ""

    fun setDeviceInfo(text: String) {
        deviceInfo = text
        notifyItemChanged(0)
    }

    fun setSummary(text: String) {
        summary = text
        notifyItemChanged(0)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val binding = AppLogHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // Listeners are set once here, so re-binding the header does not reset what the user typed or selected
        binding.appLogsSearch.doAfterTextChanged { onQueryChanged(it?.toString().orEmpty()) }
        binding.appLogsGroupChips.setOnCheckedStateChangeListener { group, checkedIds ->
            onGroupsChanged(checkedIds.mapNotNullTo(mutableSetOf()) { group.findViewById<Chip>(it)?.tag as? String })
        }
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        holder.binding.appLogsDeviceInfo.text = deviceInfo
        holder.binding.appLogsSummary.text = summary
    }

    override fun getItemCount(): Int = 1

    class ViewHolder(
        val binding: AppLogHeaderBinding,
    ) : RecyclerView.ViewHolder(binding.root)
}
