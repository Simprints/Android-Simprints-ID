package com.simprints.id.activities.dashboard

import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView

interface DashboardContract {

    interface View : BaseView<Presenter> {

    }

    interface Presenter : BasePresenter {

    }
}

