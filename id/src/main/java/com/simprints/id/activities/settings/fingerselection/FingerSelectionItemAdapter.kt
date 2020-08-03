package com.simprints.id.activities.settings.fingerselection

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckedTextView
import android.widget.ImageView
import android.widget.Spinner
import androidx.recyclerview.widget.RecyclerView
import com.simprints.id.R
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import com.simprints.id.data.db.subject.domain.FingerIdentifier.*
import com.simprints.id.tools.extensions.onItemSelectedWithPosition
import kotlinx.android.synthetic.main.item_finger_selection.view.*

class FingerSelectionItemAdapter(private val context: Context,
                                 private val viewModel: FingerSelectionViewModel) :
    RecyclerView.Adapter<FingerSelectionItemAdapter.FingerSelectionItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FingerSelectionItemViewHolder =
        FingerSelectionItemViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.item_finger_selection, parent, false)
        )

    override fun getItemCount(): Int = viewModel.items.value?.size ?: 0

    override fun onBindViewHolder(viewHolder: FingerSelectionItemViewHolder, position: Int) {
        viewHolder.fingerSpinner.adapter = FingerIdAdapter(context)
        viewHolder.quantitySpinner.adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, QUANTITY_OPTIONS)

        viewHolder.fingerSpinner.setSelection(orderedFingers().indexOf(viewModel.items.value?.get(position)?.finger))
        viewHolder.quantitySpinner.setSelection(QUANTITY_OPTIONS.indexOf(viewModel.items.value?.get(position)?.quantity))

        viewHolder.fingerSpinner.onItemSelectedWithPosition { viewModel.changeFingerSelection(position, it) }
        viewHolder.quantitySpinner.onItemSelectedWithPosition { viewModel.changeQuantitySelection(position, it) }

        if (viewModel.items.value?.get(position)?.removable == true) {
            viewHolder.deleteButton.setOnClickListener { viewModel.removeItem(position) }
        } else {
            viewHolder.deleteButton.visibility = View.INVISIBLE
            viewHolder.fingerSpinner.isEnabled = false
            viewHolder.fingerSpinner.isClickable = false
        }
    }

    class FingerSelectionItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val grip: ImageView = view.gripFingerSelectionImageView
        val fingerSpinner: Spinner = view.fingerSelectionSpinner
        val quantitySpinner: Spinner = view.fingerQuantitySpinner
        val deleteButton: ImageView = view.deleteFingerSelectionImageView
    }
}

class FingerIdAdapter(context: Context) : ArrayAdapter<FingerIdentifier>(context, 0, orderedFingers()) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
        (convertView as? CheckedTextView? ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_spinner_dropdown_item, parent, false) as CheckedTextView)
            .apply { text = getItem(position)?.toString(context) }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View =
        (convertView as? CheckedTextView? ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_spinner_dropdown_item, parent, false) as CheckedTextView)
            .apply { text = getItem(position)?.toString(context) }
}

fun FingerIdentifier.toString(context: Context) =
    context.getString(when (this) {
        LEFT_THUMB -> R.string.l_1_finger_name
        LEFT_INDEX_FINGER -> R.string.l_2_finger_name
        LEFT_3RD_FINGER -> R.string.l_3_finger_name
        LEFT_4TH_FINGER -> R.string.l_4_finger_name
        LEFT_5TH_FINGER -> R.string.l_5_finger_name
        RIGHT_THUMB -> R.string.r_1_finger_name
        RIGHT_INDEX_FINGER -> R.string.r_2_finger_name
        RIGHT_3RD_FINGER -> R.string.r_3_finger_name
        RIGHT_4TH_FINGER -> R.string.r_4_finger_name
        RIGHT_5TH_FINGER -> R.string.r_5_finger_name
    })
