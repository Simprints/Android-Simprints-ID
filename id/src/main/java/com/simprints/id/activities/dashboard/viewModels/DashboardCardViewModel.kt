package com.simprints.id.activities.dashboard.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class DashboardCardViewModel(override val type: DashboardCardType,
                                  override val position: Int, state: State) : CardViewModel(type, position) {

    val stateLiveData: MutableLiveData<State> = MutableLiveData()

    init {
        setState(state)
    }

    private fun setState(state: State) {
        stateLiveData.value = state
    }

    data class State(val imageRes: Int,
                     val title: String,
                     val description: String)
}
