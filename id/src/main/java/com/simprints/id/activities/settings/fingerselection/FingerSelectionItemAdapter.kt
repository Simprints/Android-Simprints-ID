package com.simprints.id.activities.settings.fingerselection

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckedTextView
import android.widget.ImageView
import android.widget.Spinner
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import com.simprints.id.data.db.subject.domain.FingerIdentifier.*
import com.simprints.id.databinding.ItemFingerSelectionBinding
import com.simprints.id.tools.extensions.disableLongPress
import com.simprints.id.tools.extensions.onItemSelectedWithPosition
import com.simprints.infraresources.R as IDR
import com.simprints.infra.config.domain.models.Finger

class FingerSelectionItemAdapter(
    private val itemTouchHelper: ItemTouchHelper,
    private val getItems: () -> List<FingerSelectionItem>,
    private val onFingerSelectionChanged: (itemIndex: Int, finger: Finger) -> Unit,
    private val onQuantitySelectionChanged: (itemIndex: Int, quantity: Int) -> Unit,
    private val removeItem: (itemIndex: Int) -> Unit
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
            itemTouchHelper,
            getItems,
            onFingerSelectionChanged,
            onQuantitySelectionChanged,
            removeItem,
            binding
        )
    }

    override fun getItemCount(): Int = getItems().size

    override fun onBindViewHolder(viewHolder: FingerSelectionItemViewHolder, position: Int) {
        viewHolder.bind()
    }

    class FingerSelectionItemViewHolder(
        val context: Context,
        private val itemTouchHelper: ItemTouchHelper,
        private val getItems: () -> List<FingerSelectionItem>,
        private val onFingerSelectionChanged: (itemIndex: Int, finger: Finger) -> Unit,
        private val onQuantitySelectionChanged: (itemIndex: Int, quantity: Int) -> Unit,
        private val removeItem: (itemIndex: Int) -> Unit,
        binding: ItemFingerSelectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val grip: ImageView = binding.gripFingerSelectionImageView
        private val fingerSpinner: Spinner = binding.fingerSelectionSpinner
        private val quantitySpinner: Spinner = binding.fingerQuantitySpinner
        private val deleteButton: ImageView = binding.deleteFingerSelectionImageView

        private val fingerIdAdapter = FingerIdAdapter(context, getItems)
        private val quantityAdapter =
            ArrayAdapter(context, android.R.layout.simple_list_item_1, QUANTITY_OPTIONS)

        @SuppressLint("ClickableViewAccessibility")
        fun bind() {
            fingerSpinner.adapter = fingerIdAdapter
            quantitySpinner.adapter = quantityAdapter

            fingerSpinner.setSelection(ORDERED_FINGERS.indexOf(getItems()[adapterPosition].finger))
            quantitySpinner.setSelection(QUANTITY_OPTIONS.indexOf(getItems()[adapterPosition].quantity))

            fingerSpinner.disableLongPress()
            quantitySpinner.disableLongPress()

            fingerSpinner.onItemSelectedWithPosition { position ->
                fingerIdAdapter.getItem(position)
                    ?.let { onFingerSelectionChanged(adapterPosition, it) }
            }
            quantitySpinner.onItemSelectedWithPosition { position ->
                quantityAdapter.getItem(position)
                    ?.let { onQuantitySelectionChanged(adapterPosition, it) }
            }

            if (getItems().get(adapterPosition).removable) {
                deleteButton.setOnClickListener { removeItem(adapterPosition) }
                deleteButton.visibility = View.VISIBLE
                fingerSpinner.isEnabled = true
                fingerSpinner.isClickable = true
            } else {
                deleteButton.visibility = View.INVISIBLE
                fingerSpinner.isEnabled = false
                fingerSpinner.isClickable = false
            }

            grip.setOnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) itemTouchHelper.startDrag(this)
                true
            }
        }
    }
}

class FingerIdAdapter(context: Context, private val getItems: () -> List<FingerSelectionItem>) :
    ArrayAdapter<Finger>(context, 0, ORDERED_FINGERS) {

    override fun isEnabled(position: Int): Boolean = isFingerAvailable(position)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
        (convertView as? CheckedTextView? ?: LayoutInflater.from(context)
            .inflate(
                android.R.layout.simple_spinner_dropdown_item,
                parent,
                false
            ) as CheckedTextView)
            .apply { text = getItem(position)?.toString(context) }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View =
        (convertView as? CheckedTextView? ?: LayoutInflater.from(context)
            .inflate(
                android.R.layout.simple_spinner_dropdown_item,
                parent,
                false
            ) as CheckedTextView)
            .apply {
                text = getItem(position)?.toString(context)
                setTextColor(if (isFingerAvailable(position)) Color.BLACK else Color.LTGRAY)
            }

    private fun isFingerAvailable(position: Int) =
        !getItems().map { it.finger }.contains(getItem(position))
}

fun Finger.toString(context: Context) =
    context.getString(
        when (this) {
            Finger.LEFT_THUMB -> R.string.l_1_finger_name
            Finger.LEFT_INDEX_FINGER -> R.string.l_2_finger_name
            Finger.LEFT_3RD_FINGER -> R.string.l_3_finger_name
            Finger.LEFT_4TH_FINGER -> R.string.l_4_finger_name
            Finger.LEFT_5TH_FINGER -> R.string.l_5_finger_name
            Finger.RIGHT_THUMB -> R.string.r_1_finger_name
            Finger.RIGHT_INDEX_FINGER -> R.string.r_2_finger_name
            Finger.RIGHT_3RD_FINGER -> R.string.r_3_finger_name
            Finger.RIGHT_4TH_FINGER -> R.string.r_4_finger_name
            Finger.RIGHT_5TH_FINGER -> R.string.r_5_finger_name
        }
    )
