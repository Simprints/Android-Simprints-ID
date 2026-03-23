package com.simprints.infra.config.store.remote.models

import com.google.common.truth.Truth.*
import com.simprints.core.domain.tokenization.asTokenizableEncrypted
import com.simprints.infra.config.store.models.DeviceState
import com.simprints.infra.config.store.models.SelectDownSyncModules
import com.simprints.infra.config.store.models.UpSyncEnrolmentRecords
import org.junit.Test

class ApiDeviceStateTest {
    @Test
    fun `should map correctly the model`() {
        val apiDeviceState = ApiDeviceState(
            deviceId = "deviceId",
            isCompromised = true,
            mustUpSyncEnrolmentRecords = ApiUpSyncEnrolmentRecords(
                id = "upSyncEnrolmentRecordsId",
                subjectIds = listOf("enrolmentRecordId1", "enrolmentRecordId2"),
            ),
            mustUpdateDeviceConfiguration = ApiMustUpdateDeviceConfiguration(
                id = "mustUpdateDeviceConfigurationId",
                configuration = ApiMustUpdateDeviceConfiguration.ApiDeviceConfigurationUpdate(
                    downSyncModules = listOf("moduleId1", "moduleId2"),
                ),
            ),
        )
        val domainDeviveState = DeviceState(
            deviceId = "deviceId",
            isCompromised = true,
            recordsToUpSync = UpSyncEnrolmentRecords(
                id = "upSyncEnrolmentRecordsId",
                subjectIds = listOf("enrolmentRecordId1", "enrolmentRecordId2"),
            ),
            selectModules = SelectDownSyncModules(
                id = "mustUpdateDeviceConfigurationId",
                moduleIds = listOf("moduleId1".asTokenizableEncrypted(), "moduleId2".asTokenizableEncrypted()),
            ),
        )

        assertThat(apiDeviceState.toDomain()).isEqualTo(domainDeviveState)
    }
}
