package com.simprints.id.activities.dashboard.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class DashboardCardViewModel(state: State) : ViewModel() {

    val stateLiveData: MutableLiveData<State> = MutableLiveData()

    init {
        setState(state)
    }

    private fun setState(state: State) {
        stateLiveData.value = state
    }

    data class State(val type: DashboardCardType,
                     val position: Int,
                     val imageRes: Int,
                     val title: String,
                     val description: String)
}
