package com.simprints.clientapi.activities.libsimprints

import android.os.Bundle
import com.simprints.clientapi.activities.baserequest.RequestActivity


class LibSimprintsActivity : RequestActivity(), LibSimprintsContract.View {

    override lateinit var presenter: LibSimprintsContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = LibSimprintsPresenter(this, intent.action).apply { start() }
    }

}
