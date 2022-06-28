package com.simprints.fingerprintscanner.v2.scanner

import com.simprints.fingerprintscanner.v2.channel.MainMessageChannel
import com.simprints.fingerprintscanner.v2.channel.RootMessageChannel
import com.simprints.fingerprintscanner.v2.domain.main.message.IncomingMainMessage
import com.simprints.fingerprintscanner.v2.domain.main.message.OutgoingMainMessage
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.commands.GetUn20ExtendedAppVersionCommand
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.models.Un20ExtendedAppVersion
import com.simprints.fingerprintscanner.v2.domain.main.message.un20.responses.GetUn20ExtendedAppVersionResponse
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.commands.GetStmExtendedFirmwareVersionCommand
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.models.StmExtendedFirmwareVersion
import com.simprints.fingerprintscanner.v2.domain.main.message.vero.responses.GetStmExtendedFirmwareVersionResponse
import com.simprints.fingerprintscanner.v2.domain.root.RootCommand
import com.simprints.fingerprintscanner.v2.domain.root.RootResponse
import com.simprints.fingerprintscanner.v2.domain.root.commands.*
import com.simprints.fingerprintscanner.v2.domain.root.models.*
import com.simprints.fingerprintscanner.v2.domain.root.responses.*
import com.simprints.fingerprintscanner.v2.scanner.errorhandler.ResponseErrorHandler
import com.simprints.fingerprintscanner.v2.scanner.errorhandler.handleErrorsWith
import io.reactivex.Single
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.rx2.rxSingle

class ScannerExtendedInfoReaderHelper(
    private val mainMessageChannel: MainMessageChannel,
    private val rootMessageChannel: RootMessageChannel,
    private val responseErrorHandler: ResponseErrorHandler
) {

    fun readScannerInfo(): Single<ScannerInformation>  {
        return getCypressVersion().flatMap { cypressVersion ->
            val isLegacyApi = cypressVersion.apiMajorVersion <= CYPRESS_HIGHEST_LEGACY_API_MAJOR_VERSION
                    && cypressVersion.apiMinorVersion <= CYPRESS_HIGHEST_LEGACY_API_MINOR_VERSION

           rxSingle { readScannerInfoBasedOnApiVersion(isLegacyApi) }
        }
    }

    private suspend fun readScannerInfoBasedOnApiVersion(isLegacyApi: Boolean): ScannerInformation {
        return if (isLegacyApi) getScannerInformationWithLegacyApi()
        else getScannerInformationWithNewApi()
    }

    fun getCypressVersion(): Single<CypressFirmwareVersion> {
        return sendRootModeCommandAndReceiveResponse<GetCypressVersionResponse>(
            GetCypressVersionCommand()
        ).map { it.version }
    }

    fun getCypressExtendedVersion(): Single<CypressExtendedFirmwareVersion> {
        return sendRootModeCommandAndReceiveResponse<GetCypressExtendedVersionResponse>(
            GetCypressExtendedVersionCommand()
        ).map { it.version }
    }

    fun getStmExtendedFirmwareVersion(): Single<StmExtendedFirmwareVersion>  {
        return sendMainModeCommandAndReceiveResponse<GetStmExtendedFirmwareVersionResponse>(
            GetStmExtendedFirmwareVersionCommand()
        ).map { it.stmFirmwareVersion }
    }

    fun getUn20ExtendedAppVersion(): Single<Un20ExtendedAppVersion> {
        return sendMainModeCommandAndReceiveResponse<GetUn20ExtendedAppVersionResponse>(
            GetUn20ExtendedAppVersionCommand()
        ).map { it.un20AppVersion }
    }

    fun setExtendedVersionInformation(versionInformation: ExtendedVersionInformation): Single<SetVersionResponse> {
        return sendRootModeCommandAndReceiveResponse(
            SetExtendedVersionCommand(versionInformation)
        )
    }

    private suspend fun getScannerInformationWithLegacyApi(): ScannerInformation {
        val legacyUnifiedVersion =  sendRootModeCommandAndReceiveResponse<GetVersionResponse>(
            GetVersionCommand()
        ).await()

        val extendedVersionInfo = legacyUnifiedVersion.version.toExtendedVersionInfo()

        return ScannerInformation(
            hardwareVersion = DEFAULT_HARDWARE_VERSION,
            firmwareVersions = extendedVersionInfo
        )
    }


    private suspend fun getScannerInformationWithNewApi(): ScannerInformation {
        val (unifiedVersion, hardwareVersion) = getExtendedVersionInfoAndHardwareVersionInfo()
        val mergedUnifiedVersion = validateUnifiedVersionOrMergeWithOldVersion(unifiedVersion.version)

        return ScannerInformation(
            hardwareVersion = hardwareVersion.version.versionIdentifier,
            firmwareVersions = mergedUnifiedVersion
        )
    }

    private suspend fun validateUnifiedVersionOrMergeWithOldVersion(
        unifiedVersion: ExtendedVersionInformation
    ): ExtendedVersionInformation {
        val requiresOldApiValues = listOf(
            unifiedVersion.cypressFirmwareVersion.versionAsString,
            unifiedVersion.stmFirmwareVersion.versionAsString,
            unifiedVersion.un20AppVersion.versionAsString
        ).any { it.isEmpty() }

        var mergedScannerInfo = unifiedVersion

        if (requiresOldApiValues) {
            val oldUnifiedVersionInformation =
                getScannerInformationWithLegacyApi().firmwareVersions

            if (unifiedVersion.cypressFirmwareVersion.versionAsString.isEmpty()) {
                mergedScannerInfo = mergedScannerInfo.copy(
                    cypressFirmwareVersion = oldUnifiedVersionInformation.cypressFirmwareVersion
                )
            }

            if (unifiedVersion.stmFirmwareVersion.versionAsString.isEmpty()) {
                mergedScannerInfo = mergedScannerInfo.copy(
                    stmFirmwareVersion = oldUnifiedVersionInformation.stmFirmwareVersion
                )
            }

            if (unifiedVersion.un20AppVersion.versionAsString.isEmpty()) {
                mergedScannerInfo = mergedScannerInfo.copy(
                    un20AppVersion = oldUnifiedVersionInformation.un20AppVersion
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

    private suspend fun getHardwareVersionInfo(): GetHardwareVersionResponse {
        return sendRootModeCommandAndReceiveResponse<GetHardwareVersionResponse>(
            GetHardwareVersionCommand()
        ).await()
    }

    suspend fun getExtendedVersionInfo(): GetExtendedVersionResponse {
        return sendRootModeCommandAndReceiveResponse<GetExtendedVersionResponse>(
            GetExtendedVersionCommand()
        ).await()
    }

    private inline fun <reified R : RootResponse> sendRootModeCommandAndReceiveResponse(command: RootCommand): Single<R> =
        rootMessageChannel.sendRootModeCommandAndReceiveResponse<R>(command)
            .handleErrorsWith(responseErrorHandler)

    private inline fun <reified R : IncomingMainMessage> sendMainModeCommandAndReceiveResponse(command: OutgoingMainMessage): Single<R> =
        mainMessageChannel.sendMainModeCommandAndReceiveResponse<R>(command)
            .handleErrorsWith(responseErrorHandler)



    companion object {
        private const val CYPRESS_HIGHEST_LEGACY_API_MAJOR_VERSION = 1
        private const val CYPRESS_HIGHEST_LEGACY_API_MINOR_VERSION = 0
        const val DEFAULT_HARDWARE_VERSION = "E-1"
        const val UNKNOWN_HARDWARE_VERSION = ""
    }
}
