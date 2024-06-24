package com.simprints.feature.dashboard.settings.fingerselection

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simprints.feature.dashboard.databinding.ItemFingerSelectionBinding
import com.simprints.feature.dashboard.databinding.HeaderSdkNameBinding
import com.simprints.infra.config.store.models.Finger
import com.simprints.infra.resources.R as IDR

internal class FingerSelectionItemAdapter(
    private val getItems: () -> List<FingerSelectionSection>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    override fun getItemCount(): Int = getItems().sumOf { it.items.size + 1 }

    override fun getItemViewType(position: Int): Int {
        var pos = 0
        for (section in getItems()) {
            if (position == pos) {
                return TYPE_HEADER
            }
            pos += section.items.size + 1
            if (position < pos) {
                return TYPE_ITEM
            }
        }
        throw IllegalArgumentException("Invalid position")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            val binding = HeaderSdkNameBinding.inflate(inflater, parent, false)
            HeaderViewHolder(parent.context, binding)
        } else {
            val binding = ItemFingerSelectionBinding.inflate(inflater, parent, false)
            return FingerSelectionItemViewHolder(parent.context, binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var totalItems = 0
        for (section in getItems()) {
            if (position == totalItems && holder is HeaderViewHolder) {
                holder.bind(section.sdkName)
                return
            } else if (position < totalItems + section.items.size + 1 && holder is FingerSelectionItemViewHolder) {
                holder.bind(section.items[position - totalItems - 1])
                return
            }
            totalItems += section.items.size + 1
        }
    }

    class HeaderViewHolder(
        val context: Context,
        binding: HeaderSdkNameBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private val textView = binding.headerText

        fun bind(sdkName: String) {
            textView.text = sdkName
        }
    }

    class FingerSelectionItemViewHolder(
        val context: Context,
        binding: ItemFingerSelectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val fingerNameTextView = binding.fingerNameTextView
        private val fingerQuantityTextView = binding.fingerQuantityTextView

        fun bind(item: FingerSelectionItem) {
            fingerNameTextView.text = item.finger.toString(context)
            fingerQuantityTextView.text = item.quantity.toString()
        }
    }
}

fun Finger.toString(context: Context) =
    context.getString(
        when (this) {
            Finger.LEFT_THUMB -> IDR.string.fingerprint_capture_finger_l_1
            Finger.LEFT_INDEX_FINGER -> IDR.string.fingerprint_capture_finger_l_2
            Finger.LEFT_3RD_FINGER -> IDR.string.fingerprint_capture_finger_l_3
            Finger.LEFT_4TH_FINGER -> IDR.string.fingerprint_capture_finger_l_4
            Finger.LEFT_5TH_FINGER -> IDR.string.fingerprint_capture_finger_l_5
            Finger.RIGHT_THUMB -> IDR.string.fingerprint_capture_finger_r_1
            Finger.RIGHT_INDEX_FINGER -> IDR.string.fingerprint_capture_finger_r_2
            Finger.RIGHT_3RD_FINGER -> IDR.string.fingerprint_capture_finger_r_3
            Finger.RIGHT_4TH_FINGER -> IDR.string.fingerprint_capture_finger_r_4
            Finger.RIGHT_5TH_FINGER -> IDR.string.fingerprint_capture_finger_r_5
        }
    )
