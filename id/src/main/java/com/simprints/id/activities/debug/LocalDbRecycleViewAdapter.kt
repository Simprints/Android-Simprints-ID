package com.simprints.id.activities.debug

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simprints.id.R
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import org.jetbrains.anko.sdk27.coroutines.onClick

class LocalDbRecycleViewAdapter(val items: MutableList<LocalDbViewModel>, val context: Context) : RecyclerView.Adapter<LocalDbViewHolder>() {

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalDbViewHolder {
        return LocalDbViewHolder(LayoutInflater.from(context).inflate(R.layout.activity_debug_local_db_item, parent, false))
    }

    override fun onBindViewHolder(holder: LocalDbViewHolder, position: Int) {
        val localDbViewModel= items[position]
        holder.projectText.text = localDbViewModel.subScope.projectId
        holder.userText.text = localDbViewModel.subScope.userId
        holder.moduleText.text = localDbViewModel.subScope.moduleId
        holder.countText.text = "${localDbViewModel.count}"
        holder.deleteButton.onClick {
            localDbViewModel.action(localDbViewModel.subScope)
        }
    }
}

class LocalDbViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val projectText:TextView = view.findViewById(R.id.debug_local_db_project)
    val userText:TextView = view.findViewById(R.id.debug_local_db_user)
    val moduleText:TextView = view.findViewById(R.id.debug_local_db_model)
    val countText:TextView = view.findViewById(R.id.debug_local_db_count)
    val deleteButton:Button = view.findViewById(R.id.debug_local_db_delete)

}

data class LocalDbViewModel(val subScope: SubSyncScope, val count: Int, val action: (subScope: SubSyncScope) -> Unit)
