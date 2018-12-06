package com.simprints.id.activities.about

import androidx.lifecycle.MutableLiveData
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class DebugViewModel(component: AppComponent) : DebugContract.Presenter {

    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var localDbManager: LocalDbManager
    @Inject lateinit var syncScopeBuilder: SyncScopesBuilder

    override val stateLiveData: MutableLiveData<DebugActivity.State> = MutableLiveData()

    init {
        component.inject(this)
    }

    val state = DebugActivity.State()

    override fun refresh() {
        state.nPeople.clear()
        GlobalScope.launch {
            localDbManager.loadPeopleFromLocalRx().map { person ->
                val subScope = state.nPeople.keys.findLast { person.projectId == it.projectId && person.userId == it.userId && person.moduleId == it.moduleId }
                subScope?.let {
                    state.nPeople[it] = (state.nPeople[it] ?: 0) + 1
                }?: state.nPeople.put(SubSyncScope(person.projectId, person.userId, person.moduleId), 1)
                person
            }.buffer(500)
            .map {
                stateLiveData.postValue(state)
                it}
            .subscribeBy(onError = { it.printStackTrace() }, onComplete = { })
        }
    }
}
