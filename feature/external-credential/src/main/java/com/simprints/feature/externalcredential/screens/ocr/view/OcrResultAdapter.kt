package com.simprints.feature.externalcredential.screens.ocr.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.mlkit.vision.text.Text
import com.simprints.feature.externalcredential.R

class OcrResultAdapter(
    private val items: List<OcrResultItem>,
    private val onLineClicked: (List<Text.Line>) -> Unit
) : RecyclerView.Adapter<OcrResultAdapter.TwoTextViewHolder>() {

    inner class TwoTextViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ocrId: TextView = view.findViewById(R.id.ocrId)
        val ocrText: TextView = view.findViewById(R.id.ocrText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TwoTextViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ocr_result, parent, false)
        return TwoTextViewHolder(view)
    }

    override fun onBindViewHolder(holder: TwoTextViewHolder, position: Int) {
        val item = items[position]
        holder.ocrId.text = item.id
        holder.ocrText.text = item.text
        holder.ocrText.setOnClickListener {
            onLineClicked(items[position].lines)
        }
    }

    override fun getItemCount(): Int = items.size
}

data class OcrResultItem(
    val id: String,
    val text: String,
    val lines: List<Text.Line> = emptyList()
)
