package com.simprints.clientapi.activities.libsimprints

import com.simprints.clientapi.activities.BasePresenter
import com.simprints.clientapi.activities.BaseView
import com.simprints.clientapi.activities.baserequest.RequestContract


interface LibSimprintsContract {

    interface View : BaseView<Presenter>, RequestContract.RequestView

    interface Presenter : BasePresenter, RequestContract.Presenter

}
