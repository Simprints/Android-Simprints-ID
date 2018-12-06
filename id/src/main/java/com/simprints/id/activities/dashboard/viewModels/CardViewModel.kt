package com.simprints.id.activities.dashboard.viewModels

import androidx.lifecycle.ViewModel

open class CardViewModel(
    open val type: DashboardCardType,
    open val position: Int
) : ViewModel() {
    open fun stopObservers() {}
}
