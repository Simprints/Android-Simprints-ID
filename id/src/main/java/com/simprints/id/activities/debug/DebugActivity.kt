package com.simprints.id.activities.about

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.R
import com.simprints.id.activities.debug.LocalDbRecycleViewAdapter
import com.simprints.id.activities.debug.LocalDbViewModel
import com.simprints.id.activities.debug.RoomDownStatusRecycleViewAdapter
import com.simprints.id.activities.debug.RoomDownStatusViewModel
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.room.DownSyncStatus
import com.simprints.id.data.db.local.room.getStatusId
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.services.scheduledSync.peopleDownSync.SyncStatusDatabase
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import kotlinx.android.synthetic.main.activity_debug.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.custom.async
import org.jetbrains.anko.doAsync
import java.text.DateFormat
import java.util.*
import javax.inject.Inject

class DebugActivity : AppCompatActivity() {

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var localDbManager: LocalDbManager
    @Inject lateinit var syncStatusDatabase: SyncStatusDatabase

    private lateinit var viewModel: DebugContract.Presenter

    private val listOfLocalDbViewModels = mutableListOf<LocalDbViewModel>()
    private val listOfLocalDownSyncStatus = mutableListOf<RoomDownStatusViewModel>()

    private lateinit var localDbViewAdapter: LocalDbRecycleViewAdapter
    private lateinit var roomDownSyncStatusAdapter: RoomDownStatusRecycleViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)
        if (!BuildConfig.DEBUG) {
            finish()
        }

        val component = (application as Application).component
        component.inject(this)

        setUpRecyclerViewForLocalDbInfo()
        setUpRecyclerViewForRoomInfo()

        viewModel = DebugViewModel(component)
        viewModel.stateLiveData.observe(this, updateLocalDbCountersUI)
        viewModel.refresh()
    }

    private fun setUpRecyclerViewForRoomInfo() {
        localDb_room_down_status.layoutManager = LinearLayoutManager(this)
        roomDownSyncStatusAdapter = RoomDownStatusRecycleViewAdapter(listOfLocalDownSyncStatus, this)
        localDb_room_down_status.adapter = roomDownSyncStatusAdapter
        syncStatusDatabase.downSyncDao.getDownSyncStatusLiveData().observe(this, updateRoomDownSyncStatusUI)
    }

    private fun setUpRecyclerViewForLocalDbInfo() {
        localDb_subscopes.layoutManager = LinearLayoutManager(this)
        localDbViewAdapter = LocalDbRecycleViewAdapter(listOfLocalDbViewModels, this)
        localDb_subscopes.adapter = localDbViewAdapter
    }

    private val updateLocalDbCountersUI: Observer<in State> = Observer { state ->
        runOnUiThread {
            listOfLocalDbViewModels.clear()
            listOfLocalDbViewModels.addAll(state.nPeople.map {
                LocalDbViewModel(it.key, it.value) { subScopeToDelete ->
                    GlobalScope.launch(Dispatchers.IO) { onDeleteSubModule(subScopeToDelete) }
                }
            })
            localDbViewAdapter.notifyDataSetChanged()
        }
    }

    private val updateRoomDownSyncStatusUI: Observer<in List<DownSyncStatus>?> = Observer {
        runOnUiThread {
            listOfLocalDownSyncStatus.clear()
            listOfLocalDownSyncStatus.addAll(it?.map { downStatus ->
                RoomDownStatusViewModel(downStatus) { downStatusToDelete ->
                    GlobalScope.launch(Dispatchers.IO) { onDeleteRoomInfo(downStatusToDelete) }
                }
            }?.toList() ?: arrayListOf())
            roomDownSyncStatusAdapter.notifyDataSetChanged()
        }
    }


    private fun onDeleteRoomInfo(downSyncStatus: DownSyncStatus) {
        syncStatusDatabase.downSyncDao.deleteDownSyncStatus(downSyncStatus.id)
        runOnUiThread {
            roomDownSyncStatusAdapter.notifyDataSetChanged()
        }
    }


    private fun onDeleteSubModule(subScopeToDelete: SubSyncScope) {
        localDbManager.deletePeopleFromLocal(subScopeToDelete).blockingAwait()
        viewModel.refresh()
    }

    class State {
        var nPeople: MutableMap<SubSyncScope, Int> = mutableMapOf()
    }
}
