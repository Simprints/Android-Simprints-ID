package com.simprints.fingerprint.infra.scanner.v2.scanner

import com.simprints.fingerprint.infra.scanner.v2.channel.MainMessageChannel
import com.simprints.fingerprint.infra.scanner.v2.channel.RootMessageChannel
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.commands.GetUn20ExtendedAppVersionCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.models.Un20ExtendedAppVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.un20.responses.GetUn20ExtendedAppVersionResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.commands.GetStmExtendedFirmwareVersionCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.StmExtendedFirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.responses.GetStmExtendedFirmwareVersionResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.GetCypressExtendedVersionCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.GetCypressVersionCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.GetExtendedVersionCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.GetHardwareVersionCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.GetVersionCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.commands.SetExtendedVersionCommand
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.CypressExtendedFirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.CypressFirmwareVersion
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.ExtendedVersionInformation
import com.simprints.fingerprint.infra.scanner.v2.domain.root.models.ScannerInformation
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.GetCypressExtendedVersionResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.GetCypressVersionResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.GetExtendedVersionResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.GetHardwareVersionResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.GetVersionResponse
import com.simprints.fingerprint.infra.scanner.v2.domain.root.responses.SetVersionResponse
import javax.inject.Inject

class ScannerExtendedInfoReaderHelper @Inject constructor(
    private val mainMessageChannel: MainMessageChannel,
    private val rootMessageChannel: RootMessageChannel,
) {
    suspend fun readScannerInfo(): ScannerInformation {
        val cypressVersion = getCypressVersion()
        val isLegacyApi =
            cypressVersion.apiMajorVersion <= CYPRESS_HIGHEST_LEGACY_API_MAJOR_VERSION &&
                cypressVersion.apiMinorVersion <= CYPRESS_HIGHEST_LEGACY_API_MINOR_VERSION
        return readScannerInfoBasedOnApiVersion(isLegacyApi)
    }

    private suspend fun readScannerInfoBasedOnApiVersion(isLegacyApi: Boolean): ScannerInformation = if (isLegacyApi) {
        getScannerInformationWithLegacyApi()
    } else {
        getScannerInformationWithNewApi()
    }

    suspend fun getCypressVersion(): CypressFirmwareVersion = rootMessageChannel
        .sendCommandAndReceiveResponse<GetCypressVersionResponse>(
            GetCypressVersionCommand(),
        ).version

    suspend fun getCypressExtendedVersion(): CypressExtendedFirmwareVersion = rootMessageChannel
        .sendCommandAndReceiveResponse<GetCypressExtendedVersionResponse>(
            GetCypressExtendedVersionCommand(),
        ).version

    suspend fun getStmExtendedFirmwareVersion(): StmExtendedFirmwareVersion = mainMessageChannel
        .sendCommandAndReceiveResponse<GetStmExtendedFirmwareVersionResponse>(
            GetStmExtendedFirmwareVersionCommand(),
        ).stmFirmwareVersion

    suspend fun getUn20ExtendedAppVersion(): Un20ExtendedAppVersion = mainMessageChannel
        .sendCommandAndReceiveResponse<GetUn20ExtendedAppVersionResponse>(
            GetUn20ExtendedAppVersionCommand(),
        ).un20AppVersion

    suspend fun setExtendedVersionInformation(versionInformation: ExtendedVersionInformation): SetVersionResponse =
        rootMessageChannel.sendCommandAndReceiveResponse(
            SetExtendedVersionCommand(versionInformation),
        )

    private suspend fun getScannerInformationWithLegacyApi(): ScannerInformation {
        val legacyUnifiedVersion =
            rootMessageChannel.sendCommandAndReceiveResponse<GetVersionResponse>(
                GetVersionCommand(),
            )

        val extendedVersionInfo = legacyUnifiedVersion.version.toExtendedVersionInfo()

        return ScannerInformation(
            hardwareVersion = DEFAULT_HARDWARE_VERSION,
            firmwareVersions = extendedVersionInfo,
        )
    }

    private suspend fun getScannerInformationWithNewApi(): ScannerInformation {
        val (unifiedVersion, hardwareVersion) = getExtendedVersionInfoAndHardwareVersionInfo()
        val mergedUnifiedVersion =
            validateUnifiedVersionOrMergeWithOldVersion(unifiedVersion.version)

        return ScannerInformation(
            hardwareVersion = hardwareVersion.version.versionIdentifier,
            firmwareVersions = mergedUnifiedVersion,
        )
    }

    private suspend fun validateUnifiedVersionOrMergeWithOldVersion(
        unifiedVersion: ExtendedVersionInformation,
    ): ExtendedVersionInformation {
        val requiresOldApiValues = listOf(
            unifiedVersion.cypressFirmwareVersion.versionAsString,
            unifiedVersion.stmFirmwareVersion.versionAsString,
            unifiedVersion.un20AppVersion.versionAsString,
        ).any { it.isEmpty() }

        var mergedScannerInfo = unifiedVersion

        if (requiresOldApiValues) {
            val oldUnifiedVersionInformation = getScannerInformationWithLegacyApi().firmwareVersions

            if (unifiedVersion.cypressFirmwareVersion.versionAsString.isEmpty()) {
                mergedScannerInfo = mergedScannerInfo.copy(
                    cypressFirmwareVersion = oldUnifiedVersionInformation.cypressFirmwareVersion,
                )
            }

            if (unifiedVersion.stmFirmwareVersion.versionAsString.isEmpty()) {
                mergedScannerInfo = mergedScannerInfo.copy(
                    stmFirmwareVersion = oldUnifiedVersionInformation.stmFirmwareVersion,
                )
            }

            if (unifiedVersion.un20AppVersion.versionAsString.isEmpty()) {
                mergedScannerInfo = mergedScannerInfo.copy(
                    un20AppVersion = oldUnifiedVersionInformation.un20AppVersion,
                )
            }
        }

        return mergedScannerInfo
    }

    private suspend fun getExtendedVersionInfoAndHardwareVersionInfo(): Pair<GetExtendedVersionResponse, GetHardwareVersionResponse> {
        val unifiedVersion = getExtendedVersionInfo()
        val hardwareVersionResponse = getHardwareVersionInfo()
        return Pair(unifiedVersion, hardwareVersionResponse)
    }

    private suspend fun getHardwareVersionInfo(): GetHardwareVersionResponse =
        rootMessageChannel.sendCommandAndReceiveResponse<GetHardwareVersionResponse>(
            GetHardwareVersionCommand(),
        )

    suspend fun getExtendedVersionInfo(): GetExtendedVersionResponse =
        rootMessageChannel.sendCommandAndReceiveResponse<GetExtendedVersionResponse>(
            GetExtendedVersionCommand(),
        )

    companion object {
        private const val CYPRESS_HIGHEST_LEGACY_API_MAJOR_VERSION = 1
        private const val CYPRESS_HIGHEST_LEGACY_API_MINOR_VERSION = 0
        const val DEFAULT_HARDWARE_VERSION = "E-1"
        const val UNKNOWN_HARDWARE_VERSION = ""
    }
}
