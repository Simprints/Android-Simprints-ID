package com.simprints.id.activities.debug

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simprints.id.R
import com.simprints.id.data.db.local.room.DownSyncStatus
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.text.DateFormat
import java.util.*

class RoomDownStatusRecycleViewAdapter(val items: MutableList<RoomDownStatusViewModel>, val context: Context) : RecyclerView.Adapter<RoomDownStatusViewHolder>() {

    override fun getItemCount(): Int = items.size
    private val dateFormat: DateFormat by lazy {
        DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, Locale.getDefault())
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomDownStatusViewHolder {
        return RoomDownStatusViewHolder(LayoutInflater.from(context).inflate(R.layout.activity_debug_room_down_status, parent, false))
    }

    override fun onBindViewHolder(holder: RoomDownStatusViewHolder, position: Int) {
        val vm = items[position]
        holder.projectText.text = vm.downSyncStatus.projectId
        holder.userText.text = vm.downSyncStatus.userId
        holder.moduleText.text = vm.downSyncStatus.moduleId
        holder.countText.text = "${vm.downSyncStatus.totalToDownload}"
        holder.lastPatient.text = "${vm.downSyncStatus.lastPatientUpdatedAt}"
        holder.timestamp.text = vm.downSyncStatus.lastSyncTime?.let { dateFormat.format(it) }
        holder.timestampTimestamp.text = vm.downSyncStatus.lastPatientUpdatedAt?.let { dateFormat.format(it) }
        holder.deleteButton.onClick {
            vm.action(vm.downSyncStatus)
        }
    }
}

class RoomDownStatusViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val projectText:TextView = view.findViewById(R.id.debug_down_status_project)
    val userText:TextView = view.findViewById(R.id.debug_down_status_user)
    val moduleText:TextView = view.findViewById(R.id.debug_down_status_module)
    val countText:TextView = view.findViewById(R.id.debug_down_status_count)
    val lastPatient:TextView = view.findViewById(R.id.debug_down_status_lastPatient)
    val timestampTimestamp:TextView = view.findViewById(R.id.debug_down_status_lastPatient_timestamp)
    val timestamp:TextView = view.findViewById(R.id.debug_down_status_timestamp)
    val deleteButton: Button = view.findViewById(R.id.debug_down_state_button_delete)

}

data class RoomDownStatusViewModel(val downSyncStatus: DownSyncStatus, val action: (downSyncStatus: DownSyncStatus) -> Unit)
