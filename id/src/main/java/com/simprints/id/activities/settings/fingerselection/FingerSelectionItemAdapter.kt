package com.simprints.id.activities.settings.fingerselection

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.recyclerview.widget.RecyclerView
import com.simprints.id.R
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
        viewHolder.fingerSpinner.adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, orderedFingers())
        viewHolder.quantitySpinner.adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, QUANTITY_OPTIONS)

        viewHolder.fingerSpinner.setSelection(orderedFingers().indexOf(viewModel.items.value?.get(position)?.finger))
        viewHolder.quantitySpinner.setSelection(QUANTITY_OPTIONS.indexOf(viewModel.items.value?.get(position)?.quantity))

        viewHolder.fingerSpinner.onItemSelectedWithPosition { viewModel.changeFingerSelection(position, it) }
        viewHolder.quantitySpinner.onItemSelectedWithPosition { viewModel.changeQuantitySelection(position, it) }

        viewHolder.deleteButton.setOnClickListener { viewModel.removeItem(position) }
    }

    class FingerSelectionItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val grip: ImageView = view.gripFingerSelectionImageView
        val fingerSpinner: Spinner = view.fingerSelectionSpinner
        val quantitySpinner: Spinner = view.fingerQuantitySpinner
        val deleteButton: ImageView = view.deleteFingerSelectionImageView
    }
}
