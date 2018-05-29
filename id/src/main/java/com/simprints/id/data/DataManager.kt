package com.simprints.id.data

import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.remote.enums.VERIFY_GUID_EXISTS_RESULT
import com.simprints.id.data.prefs.PreferencesManager
import com.simprints.id.data.prefs.loginInfo.LoginInfoManager
import com.simprints.id.domain.Constants
import com.simprints.libcommon.Person
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Verification
import io.reactivex.Completable

interface DataManager : PreferencesManager, DbManager, LoginInfoManager {

    val analytics: AnalyticsManager

    // Data transfer
    fun saveIdentification(probe: Person, matchSize: Int, matches: List<Identification>)
    fun updateIdentification(projectId: String, selectedGuid: String)

    fun saveVerification(probe: Person, match: Verification?, guidExistsResult: VERIFY_GUID_EXISTS_RESULT)

    fun saveRefusalForm(refusalForm: RefusalForm)

    fun saveSession()

    fun recoverRealmDb(group: Constants.GROUP): Completable
}
