package com.simprints.id.secure

import com.simprints.id.services.securitystate.SecurityStateScheduler
import com.simprints.id.services.sync.SyncManager
import com.simprints.id.services.sync.events.master.EventSyncManager
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.Simber
import com.simprints.infra.login.LoginManager
import com.simprints.infra.login.domain.models.Token
import com.simprints.infra.network.SimNetwork
import com.simprints.infra.recent.user.activity.RecentUserActivityManager
import javax.inject.Inject

// TODO move into its own module
class SignerManagerImpl @Inject constructor(
    private val configManager: ConfigManager,
    private val loginManager: LoginManager,
    private val eventSyncManager: EventSyncManager,
    private val syncManager: SyncManager,
    private val securityStateScheduler: SecurityStateScheduler,
    private val recentUserActivityManager: RecentUserActivityManager,
    private val simNetwork: SimNetwork,
) : SignerManager, com.simprints.feature.dashboard.settings.about.SignerManager {

    override suspend fun signIn(projectId: String, userId: String, token: Token) {
        loginManager.signIn(token)
        loginManager.storeCredentials(projectId, userId)
        configManager.refreshProject(projectId)
        securityStateScheduler.scheduleSecurityStateCheck()
    }

    override suspend fun signOut() {
        //TODO: move peopleUpSyncMaster to SyncScheduler and call .pause in CheckLoginPresenter.checkSignedInOrThrow
        //If you user clears the data (then doesn't call signout), workers still stay scheduled.
        securityStateScheduler.cancelSecurityStateCheck()
        loginManager.cleanCredentials()
        loginManager.signOut()
        syncManager.cancelBackgroundSyncs()
        eventSyncManager.deleteSyncInfo()
        simNetwork.resetApiBaseUrl()
        configManager.clearData()
        recentUserActivityManager.clearRecentActivity()

        Simber.tag(LoggingConstants.CrashReportTag.LOGOUT.name).i("Signed out")
    }

}
