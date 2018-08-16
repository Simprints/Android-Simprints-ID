package com.simprints.id.activities.longConsent

import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView

interface LongConsentContract {

    interface View : BaseView<Presenter> {

        fun setLongConsentText(text: String)

    }

    interface Presenter : BasePresenter {

    }
}

