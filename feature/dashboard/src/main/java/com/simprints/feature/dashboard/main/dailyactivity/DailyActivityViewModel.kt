package com.simprints.feature.dashboard.main.dailyactivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class DailyActivityViewModel @Inject constructor(
    private val recentUserActivityManager: RecentUserActivityManager,
    private val timeHelper: TimeHelper,
) : ViewModel() {
    val dailyActivity: LiveData<DashboardDailyActivityState>
        get() = _dailyActivity
    private val _dailyActivity = MutableLiveData<DashboardDailyActivityState>()

    init {
        load()
    }

    fun getCurrentDateAsString(): String = timeHelper.getCurrentDateAsString()

    fun load() = viewModelScope.launch {
        val userActivity = recentUserActivityManager.getRecentUserActivity()
        val state = DashboardDailyActivityState(
            userActivity.enrolmentsToday,
            userActivity.identificationsToday,
            userActivity.verificationsToday,
        )
        _dailyActivity.postValue(state)
    }
}
