package com.simprints.id.activities.about

import androidx.lifecycle.MutableLiveData
import com.simprints.id.activities.dashboard.viewModels.DashboardSyncCardViewModel
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import com.simprints.id.domain.Constants
import com.simprints.id.exceptions.safe.SimprintsException
import com.simprints.id.services.scheduledSync.peopleDownSync.controllers.SyncScopesBuilder
import com.simprints.id.services.scheduledSync.peopleDownSync.models.SubSyncScope
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
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
                }?: state.nPeople.put(SubSyncScope(Constants.GROUP.GLOBAL, person.projectId, person.userId, person.moduleId), 1)
                person
            }.buffer(500)
            .map {
                stateLiveData.postValue(state)
                it}
            .subscribeBy(onError = { it.printStackTrace() }, onComplete = { })
        }
    }
}
