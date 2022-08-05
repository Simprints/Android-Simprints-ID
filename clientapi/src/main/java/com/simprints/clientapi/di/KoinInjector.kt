package com.simprints.clientapi.di

import android.content.Context
import android.content.SharedPreferences
import com.simprints.clientapi.activities.commcare.CommCareAction
import com.simprints.clientapi.activities.commcare.CommCareContract
import com.simprints.clientapi.activities.commcare.CommCarePresenter
import com.simprints.clientapi.activities.errors.ErrorContract
import com.simprints.clientapi.activities.errors.ErrorPresenter
import com.simprints.clientapi.activities.libsimprints.LibSimprintsAction
import com.simprints.clientapi.activities.libsimprints.LibSimprintsContract
import com.simprints.clientapi.activities.libsimprints.LibSimprintsPresenter
import com.simprints.clientapi.activities.odk.OdkAction
import com.simprints.clientapi.activities.odk.OdkContract
import com.simprints.clientapi.activities.odk.OdkPresenter
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManager
import com.simprints.clientapi.controllers.core.eventData.ClientApiSessionEventsManagerImpl
import com.simprints.clientapi.data.sharedpreferences.SharedPreferencesManager
import com.simprints.clientapi.data.sharedpreferences.SharedPreferencesManagerImpl
import com.simprints.clientapi.identity.CommCareGuidSelectionNotifier
import com.simprints.clientapi.identity.OdkGuidSelectionNotifier
import com.simprints.clientapi.tools.ClientApiTimeHelper
import com.simprints.clientapi.tools.ClientApiTimeHelperImpl
import com.simprints.core.tools.json.JsonHelper
import com.simprints.id.Application
import com.simprints.id.di.AppComponent
import com.simprints.id.orchestrator.cache.HotCache
import com.simprints.id.orchestrator.cache.HotCacheImpl
import com.simprints.id.orchestrator.cache.StepEncoder
import com.simprints.id.orchestrator.cache.StepEncoderImpl
import com.simprints.infra.security.keyprovider.EncryptedSharedPreferencesBuilderImpl
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
        module {
            defineBuildersForDomainManagers()
            defineBuildersForPresenters()
            defineBuildersForGuidSelectionNotifiers()
            defineBuilderForRootManager()
            factory { JsonHelper }
        }

    private fun Module.defineBuildersForDomainManagers() {
        factory<ClientApiTimeHelper> { ClientApiTimeHelperImpl(get()) }
        factory<SharedPreferencesManager> { SharedPreferencesManagerImpl(androidContext(), get()) }
        buildClientApiSessionEventsManager()
    }

    private fun Module.buildClientApiSessionEventsManager() {
        factory<StepEncoder> { StepEncoderImpl() }
        factory<SharedPreferences> {
            EncryptedSharedPreferencesBuilderImpl(androidContext()).buildEncryptedSharedPreferences()
        }
        factory<HotCache> { HotCacheImpl(get(), get()) }
        factory<ClientApiSessionEventsManager> {
            ClientApiSessionEventsManagerImpl(
                get(),
                get(),
                get(),
                get()
            )
        }
    }

    private fun Module.defineBuildersForPresenters() {
        factory<ErrorContract.Presenter> { (view: ErrorContract.View) ->
            ErrorPresenter(view, get())
        }
        factory<LibSimprintsContract.Presenter> { (view: LibSimprintsContract.View, action: LibSimprintsAction) ->
            LibSimprintsPresenter(view, action, get(), get(), get(), get(), get(), get())
        }
        factory<OdkContract.Presenter> { (view: OdkContract.View, action: OdkAction) ->
            OdkPresenter(view, action, get(), get(), get())
        }
        factory<CommCareContract.Presenter> { (view: CommCareContract.View, action: CommCareAction) ->
            CommCarePresenter(
                view,
                action,
                get(),
                get(),
                get(),
                get(),
                get(),
                get()
            )
        }
    }

    private fun Module.defineBuildersForGuidSelectionNotifiers() {
        factory { (context: Context) -> OdkGuidSelectionNotifier(context) }
        factory { (context: Context) -> CommCareGuidSelectionNotifier(context) }
    }

    private fun Module.defineBuilderForRootManager() {
        factory { androidContext().applicationContext as Application }
        factory { get<Application>().component }
        factory { get<AppComponent>().getRootManager() }
    }

}

