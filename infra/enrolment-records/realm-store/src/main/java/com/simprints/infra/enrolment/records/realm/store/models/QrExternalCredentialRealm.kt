package com.simprints.infra.enrolment.records.realm.store.models

import androidx.annotation.Keep
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

@Keep
class QrExternalCredentialRealm : RealmObject {
    @PrimaryKey
    var data: String = ""
    var subjectId: String = ""
}
