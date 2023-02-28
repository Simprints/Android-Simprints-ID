package com.simprints.feature.dashboard.settings.fingerselection

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simprints.feature.dashboard.databinding.ItemFingerSelectionBinding
import com.simprints.infra.config.domain.models.Finger
import com.simprints.infra.resources.R as IDR

internal class FingerSelectionItemAdapter(
    private val getItems: () -> List<FingerSelectionItem>,
) :
    RecyclerView.Adapter<FingerSelectionItemAdapter.FingerSelectionItemViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FingerSelectionItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemFingerSelectionBinding.inflate(inflater, parent, false)
        return FingerSelectionItemViewHolder(
            parent.context,
            getItems,
            binding
        )
    }

    override fun getItemCount(): Int = getItems().size

    override fun onBindViewHolder(viewHolder: FingerSelectionItemViewHolder, position: Int) {
        viewHolder.bind()
    }

    class FingerSelectionItemViewHolder(
        val context: Context,
        private val getItems: () -> List<FingerSelectionItem>,
        binding: ItemFingerSelectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val fingerNameTextView = binding.fingerNameTextView
        private val fingerQuantityTextView = binding.fingerQuantityTextView

        fun bind() {
            fingerNameTextView.text = getItems()[adapterPosition].finger.toString()
            fingerQuantityTextView.text = getItems()[adapterPosition].quantity.toString()
        }
    }
}

fun Finger.toString(context: Context) =
    context.getString(
        when (this) {
            Finger.LEFT_THUMB -> IDR.string.l_1_finger_name
            Finger.LEFT_INDEX_FINGER -> IDR.string.l_2_finger_name
            Finger.LEFT_3RD_FINGER -> IDR.string.l_3_finger_name
            Finger.LEFT_4TH_FINGER -> IDR.string.l_4_finger_name
            Finger.LEFT_5TH_FINGER -> IDR.string.l_5_finger_name
            Finger.RIGHT_THUMB -> IDR.string.r_1_finger_name
            Finger.RIGHT_INDEX_FINGER -> IDR.string.r_2_finger_name
            Finger.RIGHT_3RD_FINGER -> IDR.string.r_3_finger_name
            Finger.RIGHT_4TH_FINGER -> IDR.string.r_4_finger_name
            Finger.RIGHT_5TH_FINGER -> IDR.string.r_5_finger_name
        }
    )
