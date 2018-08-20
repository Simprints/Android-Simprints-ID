package com.simprints.id.activities.checkLogin.openedByIntent

import com.simprints.id.activities.BasePresenter
import com.simprints.id.activities.BaseView
import com.simprints.id.activities.checkLogin.CheckLoginContract
import com.simprints.id.data.analytics.eventData.models.events.ConnectivitySnapshotEvent
import com.simprints.id.data.analytics.eventData.models.session.SessionEvents
import com.simprints.id.session.callout.Callout

interface CheckLoginFromIntentContract {

    interface View : BaseView<Presenter>, CheckLoginContract.View {
        fun openLoginActivity(legacyApiKey: String)
        fun openLaunchActivity()

        fun checkCallingAppIsFromKnownSource()
        fun parseCallout(): Callout
        fun finishCheckLoginFromIntentActivity()
        fun buildConnectionEvent(sessionEvents: SessionEvents): ConnectivitySnapshotEvent
    }

    interface Presenter : BasePresenter {
        fun setup()
        fun handleActivityResult(requestCode: Int, resultCode: Int, returnCallout: Callout)
    }
}
