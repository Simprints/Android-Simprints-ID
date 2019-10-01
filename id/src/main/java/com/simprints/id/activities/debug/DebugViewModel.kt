package com.simprints.id.activities.debug

import androidx.lifecycle.MutableLiveData
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.di.AppComponent
import javax.inject.Inject

class DebugViewModel(component: AppComponent) : DebugContract.Presenter {

    @Inject lateinit var preferencesManager: PreferencesManager
    //@Inject lateinit var localDbManager: LocalDbManager

    override val stateLiveData: MutableLiveData<DebugActivity.State> = MutableLiveData()

    init {
        component.inject(this)
    }

    val state = DebugActivity.State()

    override fun refresh() {
//        state.nPeople.clear()
//        GlobalScope.launch {
//            localDbManager.loadPeopleFromLocalRx().map { person ->
//                val subScope = state.nPeople.keys.findLast { person.projectId == it.projectId && person.userId == it.userId && person.moduleId == it.moduleId }
//                subScope?.let {
//                    state.nPeople[it] = (state.nPeople[it] ?: 0) + 1
//                }?: state.nPeople.put(SubSyncScope(person.projectId, person.userId, person.moduleId), 1)
//                person
//            }.buffer(500)
//            .map {
//                stateLiveData.postValue(state)
//                it}
//            .subscribeBy(onError = { it.printStackTrace() }, onComplete = { })
//        }
    }
}
