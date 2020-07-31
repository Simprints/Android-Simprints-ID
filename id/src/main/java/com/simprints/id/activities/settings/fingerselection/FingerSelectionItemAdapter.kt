package com.simprints.id.activities.settings.fingerselection

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.recyclerview.widget.RecyclerView
import com.simprints.id.R
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import kotlinx.android.synthetic.main.item_finger_selection.view.*

class FingerSelectionItemAdapter(private val context: Context) :
    RecyclerView.Adapter<FingerSelectionItemAdapter.FingerSelectionItemViewHolder>() {

    var items: MutableList<FingerSelectionItem> = mutableListOf(
        FingerSelectionItem(FingerIdentifier.LEFT_THUMB, 2),
        FingerSelectionItem(FingerIdentifier.LEFT_INDEX_FINGER, 4)
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FingerSelectionItemViewHolder =
        FingerSelectionItemViewHolder(
            LayoutInflater.from(context)
                .inflate(R.layout.item_finger_selection, parent, false)
        )

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(viewHolder: FingerSelectionItemViewHolder, position: Int) {
        val itemPosition = position
        viewHolder.fingerSpinner.adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, FingerIdentifier.values())
        viewHolder.quantitySpinner.adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayOf(1, 2, 3, 4, 5))

        viewHolder.fingerSpinner.setSelection(FingerIdentifier.values().indexOf(items[itemPosition].finger))
        viewHolder.quantitySpinner.setSelection(arrayOf(1, 2, 3, 4, 5).indexOf(items[itemPosition].quantity))

        viewHolder.fingerSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                items[itemPosition].finger = FingerIdentifier.values()[position]
            }
        }
        viewHolder.quantitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                items[itemPosition].quantity = arrayOf(1, 2, 3, 4, 5)[position]
            }
        }
        viewHolder.deleteButton.setOnClickListener {
            items.removeAt(itemPosition)
            notifyDataSetChanged()
        }
    }

    class FingerSelectionItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val grip: ImageView = view.gripFingerSelectionImageView
        val fingerSpinner: Spinner = view.fingerSelectionSpinner
        val quantitySpinner: Spinner = view.fingerQuantitySpinner
        val deleteButton: ImageView = view.deleteFingerSelectionImageView
    }

    data class FingerSelectionItem(var finger: FingerIdentifier, var quantity: Int)
}
