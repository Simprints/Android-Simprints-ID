package com.simprints.feature.externalcredential.screens.ocr.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.mlkit.vision.text.Text
import com.simprints.feature.externalcredential.R

class OcrBlockAdapter(
    private val items: List<OcrBlockItem>,
    private val onBlockClicked: (OcrBlockItem) -> Unit,
    private val onLineClicked: (Text.Line) -> Unit,
    private val onUseLineButtonClicked: (Text.Line) -> Unit
) : RecyclerView.Adapter<OcrBlockAdapter.TwoTextViewHolder>() {

    inner class TwoTextViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ocrBlockContainer: View = view.findViewById(R.id.ocrBlockContainer)
        val ocrId: TextView = view.findViewById(R.id.ocrId)
        val linesRecyclerView: RecyclerView = view.findViewById(R.id.linesRecyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TwoTextViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ocr_block, parent, false)
        return TwoTextViewHolder(view)
    }

    override fun onBindViewHolder(holder: TwoTextViewHolder, position: Int) {
        val item = items[position]
        holder.ocrId.text = item.id
        holder.ocrBlockContainer.setOnClickListener {
            onBlockClicked(item)
        }
        renderLines(holder.linesRecyclerView, lines = item.lines)
    }

    override fun getItemCount(): Int = items.size

    private fun renderLines(recyclerView: RecyclerView, lines: List<Text.Line>) {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        recyclerView.adapter = OcrLineAdapter(
            items = lines,
            onLineClicked = { line -> onLineClicked(line) },
            onUseLineButtonClicked = { line -> onUseLineButtonClicked(line) },
        )
    }
}

private class OcrLineAdapter(
    private val items: List<Text.Line>,
    private val onLineClicked: (Text.Line) -> Unit,
    private val onUseLineButtonClicked: (Text.Line) -> Unit
) : RecyclerView.Adapter<OcrLineAdapter.OcrLineViewHolder>() {

    inner class OcrLineViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ocrLine: TextView = view.findViewById(R.id.ocrLine)
        val useButton: Button = view.findViewById(R.id.buttonUseOcrLine)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OcrLineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ocr_line, parent, false)
        return OcrLineViewHolder(view)
    }

    override fun onBindViewHolder(holder: OcrLineViewHolder, position: Int) {
        val item = items[position]
        holder.ocrLine.text = item.text
        holder.ocrLine.setOnClickListener {
            onLineClicked(items[position])
        }
        holder.useButton.setOnClickListener {
            onUseLineButtonClicked(items[position])
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

}

data class OcrBlockItem(
    val id: String,
    val text: String,
    val lines: List<Text.Line> = emptyList()
)
