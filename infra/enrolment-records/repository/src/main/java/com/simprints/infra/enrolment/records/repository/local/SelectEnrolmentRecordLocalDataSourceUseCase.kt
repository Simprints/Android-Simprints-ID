package com.simprints.infra.enrolment.records.repository.local

import javax.inject.Inject

internal class SelectEnrolmentRecordLocalDataSourceUseCase @Inject constructor(
    private val roomDataSource: RoomEnrolmentRecordLocalDataSource,
    private val realmDataSource: RealmEnrolmentRecordLocalDataSource,
) {
    operator fun invoke(): EnrolmentRecordLocalDataSource {
        // Todo later we will add logic to select the data source
        return roomDataSource
    }
}
