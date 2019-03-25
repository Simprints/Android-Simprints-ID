package com.simprints.fingerprint.di

import com.simprints.id.tools.TimeHelper
import com.simprints.id.Application
import com.simprints.id.data.analytics.eventdata.controllers.domain.SessionEventsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.local.LocalDbManager
import com.simprints.id.data.db.local.room.SyncStatusDatabase
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.db.remote.people.RemotePeopleManager
import com.simprints.id.data.db.remote.project.RemoteProjectManager
import com.simprints.id.data.loginInfo.LoginInfoManager
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.secure.SecureDataManager
import com.simprints.id.di.AppModule
import com.simprints.id.di.PreferencesModule
import com.simprints.id.di.SerializerModule
import com.simprints.id.services.scheduledSync.peopleUpsync.PeopleUpSyncMaster

class FingerprintsComponentBuilder {

    companion object {

        private var component: FingerprintsComponent? = null

        //StopShip
        @JvmStatic
        fun getComponent(app: Application): FingerprintsComponent =
            component?.let {
                it
            } ?: buildComponent(app).also { component = it }

        private fun buildComponent(app: Application): FingerprintsComponent =
            DaggerFingerprintsComponent
                .builder()
                .appModule(object : AppModule(app) {

                    override fun provideDbManager(
                        localDbManager: LocalDbManager,
                        remoteDbManager: RemoteDbManager,
                        secureDataManager: SecureDataManager,
                        loginInfoManager: LoginInfoManager,
                        preferencesManager: PreferencesManager,
                        sessionEventsManager: SessionEventsManager,
                        remotePeopleManager: RemotePeopleManager,
                        remoteProjectManager: RemoteProjectManager,
                        timeHelper: TimeHelper,
                        peopleUpSyncMaster: PeopleUpSyncMaster,
                        database: SyncStatusDatabase): DbManager {

                        return super.provideDbManager(
                            localDbManager,
                            remoteDbManager,
                            secureDataManager,
                            loginInfoManager,
                            preferencesManager,
                            sessionEventsManager,
                            remotePeopleManager,
                            remoteProjectManager,
                            timeHelper,
                            peopleUpSyncMaster, database).also { it.initialiseDb() }
                    }
                })
                .fingerprintModule(FingerprintModule())
                .preferencesModule(PreferencesModule())
                .serializerModule(SerializerModule())
                .build()

    }
}
