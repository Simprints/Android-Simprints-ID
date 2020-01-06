package com.simprints.clientapi.di

import android.content.Context
import com.simprints.clientapi.activities.commcare.CommCareContract
import com.simprints.clientapi.activities.commcare.CommCarePresenter
import com.simprints.clientapi.activities.errors.ErrorContract
import com.simprints.clientapi.activities.errors.ErrorPresenter
import com.simprints.clientapi.activities.libsimprints.LibSimprintsContract
import com.simprints.clientapi.activities.libsimprints.LibSimprintsPresenter
import com.simprints.clientapi.activities.odk.OdkContract
import com.simprints.clientapi.activities.odk.OdkPresenter
import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManager
import com.simprints.clientapi.controllers.core.crashreport.ClientApiCrashReportManagerImpl
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManagerImpl
import com.simprints.clientapi.data.sharedpreferences.SharedPreferencesManager
import com.simprints.clientapi.data.sharedpreferences.SharedPreferencesManagerImpl
import com.simprints.clientapi.identity.CommCareGuidSelectionNotifier
import com.simprints.clientapi.identity.OdkGuidSelectionNotifier
import com.simprints.clientapi.tools.ClientApiTimeHelper
import com.simprints.clientapi.tools.ClientApiTimeHelperImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module

object KoinInjector {

    private var koinModule: Module? = null

    fun loadClientApiKoinModules() {
        if (koinModule == null) {
            val module = buildKoinModule()
            loadKoinModules(module)
            koinModule = module
        }
    }

    fun unloadClientApiKoinModules() {
        koinModule?.let {
            unloadKoinModules(it)
            koinModule = null
        }
    }

    private fun buildKoinModule() =
        module(override = true) {
            defineBuildersForDomainManagers()
            defineBuildersForPresenters()
            defineBuildersForGuidSelectionNotifiers()
        }

    private fun Module.defineBuildersForDomainManagers() {
        factory<ClientApiSessionEventsManager> { ClientApiSessionEventsManagerImpl(get(), get()) }
        factory<ClientApiCrashReportManager> { ClientApiCrashReportManagerImpl(get()) }
        factory<ClientApiTimeHelper> { ClientApiTimeHelperImpl(get()) }
        factory<SharedPreferencesManager> { SharedPreferencesManagerImpl(androidContext()) }
    }

    private fun Module.defineBuildersForPresenters() {
        factory<ErrorContract.Presenter> { (view: ErrorContract.View) ->
            ErrorPresenter(view, get())
        }
        factory<LibSimprintsContract.Presenter> { (view: LibSimprintsContract.View, action: String?) ->
            LibSimprintsPresenter(view, action, get(), get())
        }
        factory<OdkContract.Presenter> { (view: OdkContract.View, action: String?) ->
            OdkPresenter(view, action, get(), get())
        }
        factory<CommCareContract.Presenter> { (view: CommCareContract.View, action: String?) ->
            CommCarePresenter(view, action, get(), get(), get())
        }
    }

    private fun Module.defineBuildersForGuidSelectionNotifiers() {
        factory { (context: Context) -> OdkGuidSelectionNotifier(context) }
        factory { (context: Context) -> CommCareGuidSelectionNotifier(context) }
    }

}

