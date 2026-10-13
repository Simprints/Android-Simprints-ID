package com.simprints.feature.externalcredential.screens.scandocument.usecase

import android.nfc.Tag
import android.nfc.TagLostException
import android.nfc.tech.IsoDep
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.feature.externalcredential.screens.scandocument.model.ChipAccessProtocol
import com.simprints.feature.externalcredential.screens.scandocument.model.ChipReadError
import com.simprints.feature.externalcredential.screens.scandocument.model.ChipReadException
import com.simprints.feature.externalcredential.screens.scandocument.model.ChipReadResult
import com.simprints.feature.externalcredential.screens.scandocument.mrz.MrzData
import com.simprints.feature.externalcredential.screens.scandocument.mrz.MrzFormat
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MULTI_FACTOR_ID
import com.simprints.infra.logging.Simber
import net.sf.scuba.data.Gender
import net.sf.scuba.smartcards.CardService
import net.sf.scuba.smartcards.CardServiceException
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.jmrtd.AccessDeniedException
import org.jmrtd.BACDeniedException
import org.jmrtd.BACKey
import org.jmrtd.PassportService
import org.jmrtd.lds.CardAccessFile
import org.jmrtd.lds.LDSFileUtil
import org.jmrtd.lds.PACEInfo
import org.jmrtd.lds.SODFile
import org.jmrtd.lds.icao.COMFile
import org.jmrtd.lds.icao.DG1File
import org.jmrtd.lds.icao.MRZInfo
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.inject.Inject

/**
 * Reads the ICAO 9303 document chip over NFC:
 *  1. Establishes secure messaging with the access key derived from the MRZ (PACE if the chip supports it, BAC otherwise)
 *  2. Reads EF.COM, DG1 (MRZ data) and EF.SOD
 *  3. Checks that the DG1 hash matches the one in EF.SOD (data integrity only, the signature is not verified)
 *
 * Blocking call, must be invoked on a background thread. Nothing read from the chip is persisted or logged. Each attempt produces a
 * short [ChipReadLog] (returned with the result or the [ChipReadException]) that is meant for on-screen display only.
 */
@ExcludedFromGeneratedTestCoverageReports("Requires a physical NFC chip")
internal class ReadDocumentChipUseCase @Inject constructor() {
    operator fun invoke(
        tag: Tag,
        mrz: MrzData,
    ): ChipReadResult {
        val log = ChipReadLog(sensitiveValues = listOf(mrz.documentNumber, mrz.dateOfBirth, mrz.dateOfExpiry))
        val isoDep = IsoDep.get(tag) ?: run {
            log.add("Not a document chip (ISO 14443-4 not supported)")
            throw ChipReadException(ChipReadError.TagNotSupported, log.lines)
        }
        isoDep.timeout = ISO_DEP_TIMEOUT_MS

        val cardService = CardService.getInstance(isoDep)
        try {
            log.step("Connection") { cardService.open() }
            log.add("Connected to the chip")
            val service = PassportService(
                cardService,
                PassportService.NORMAL_MAX_TRANCEIVE_LENGTH,
                PassportService.DEFAULT_MAX_BLOCKSIZE,
                false,
                false,
            )
            log.step("Connection") { service.open() }

            val accessKey = BACKey(mrz.documentNumber, mrz.dateOfBirth, mrz.dateOfExpiry)
            val accessProtocol = establishSecureMessaging(service, accessKey, log)

            val com = runCatching {
                log.step("Reading EF.COM (optional)") { COMFile(service.readFile(PassportService.EF_COM).inputStream()) }
            }.getOrNull()
            com?.let { log.add("Read EF.COM: ${it.tagList.map(LDSFileUtil::lookupDataGroupNumberByTag).sorted().formatDataGroups()}") }

            val dg1Bytes = log.step("Reading DG1") { service.readFile(PassportService.EF_DG1) }
            val dg1 = log.step("Reading DG1") { DG1File(dg1Bytes.inputStream()) }
            log.add("Read DG1: ${dg1Bytes.size} bytes")

            val sod = log.step("Reading EF.SOD") { SODFile(service.readFile(PassportService.EF_SOD).inputStream()) }
            log.add("Read EF.SOD: ${sod.digestAlgorithm} hashes of ${sod.dataGroupHashes.keys.sorted().formatDataGroups()}")

            val dg1HashMatchesSod = log.step("DG1 hash check") {
                sod.dataGroupHashes[DG1_NUMBER]?.contentEquals(digest(sod.digestAlgorithm, dg1Bytes)) == true
            }
            log.add("DG1 hash check: ${if (dg1HashMatchesSod) "valid" else "INVALID"}")

            val signer = runCatching { sod.docSigningCertificate }.getOrNull()
            if (signer == null) log.add("Document signer certificate not available")

            return ChipReadResult(
                accessProtocol = accessProtocol,
                ldsVersion = com?.ldsVersion,
                unicodeVersion = com?.unicodeVersion,
                dataGroupsInCom = com?.tagList?.map(LDSFileUtil::lookupDataGroupNumberByTag)?.sorted(),
                dataGroupsInSod = sod.dataGroupHashes.keys.sorted(),
                sodDigestAlgorithm = sod.digestAlgorithm,
                dg1HashMatchesSod = dg1HashMatchesSod,
                documentSignerSubject = signer?.subjectX500Principal?.name,
                documentSignerIssuer = signer?.issuerX500Principal?.name,
                mrz = dg1.mrzInfo.toMrzData(),
                readLog = log.lines,
            )
        } catch (e: Throwable) {
            if (e is VirtualMachineError) throw e
            // Exceptions (e.g. BACDeniedException) may carry the access key, so only the type is logged
            Simber.i("Document chip read failed: ${e.javaClass.simpleName}", tag = MULTI_FACTOR_ID)
            log.addFailure("Read", e)
            throw ChipReadException(e.toChipReadError(cardService), log.lines, e)
        } finally {
            runCatching { cardService.close() }
        }
    }

    private fun establishSecureMessaging(
        service: PassportService,
        accessKey: BACKey,
        log: ChipReadLog,
    ): ChipAccessProtocol {
        // EF.CardAccess is only present on chips that support PACE
        val paceInfo = runCatching {
            CardAccessFile(service.readFile(PassportService.EF_CARD_ACCESS).inputStream())
                .securityInfos
                .filterIsInstance<PACEInfo>()
                .firstOrNull()
        }.getOrNull()

        val isPaceEstablished = when {
            paceInfo == null -> {
                log.add("PACE not supported, using BAC")
                false
            }

            else -> runCatching {
                log.step("PACE") {
                    service.doPACE(
                        accessKey,
                        paceInfo.objectIdentifier,
                        PACEInfo.toParameterSpec(paceInfo.parameterId),
                        paceInfo.parameterId,
                    )
                }
                true
            }.onFailure {
                Simber.i("PACE not established, falling back to BAC: ${it.javaClass.simpleName}", tag = MULTI_FACTOR_ID)
                log.add("Falling back to BAC")
            }.getOrDefault(false)
        }

        log.step("Selecting the document application") { service.sendSelectApplet(isPaceEstablished) }
        if (!isPaceEstablished) {
            log.step("BAC") { service.doBAC(accessKey) }
        }
        val protocol = if (isPaceEstablished) ChipAccessProtocol.PACE else ChipAccessProtocol.BAC
        log.add("Access granted ($protocol)")
        return protocol
    }

    private fun PassportService.readFile(fid: Short): ByteArray = getInputStream(fid, PassportService.DEFAULT_MAX_BLOCKSIZE)
        .use { it.readBytes() }

    private fun digest(
        algorithm: String,
        data: ByteArray,
    ): ByteArray {
        val messageDigest = try {
            MessageDigest.getInstance(algorithm)
        } catch (_: NoSuchAlgorithmException) {
            // Not all digest names used in SOD are known to the platform providers
            MessageDigest.getInstance(algorithm, BouncyCastleProvider())
        }
        return messageDigest.digest(data)
    }

    private fun List<Int>.formatDataGroups() = joinToString(", ") { "DG$it" }.ifEmpty { "no data groups" }

    private fun Throwable.toChipReadError(cardService: CardService): ChipReadError {
        val causes = generateSequence<Throwable>(this) { it.cause }.toList()
        val isConnectionLost = this is Exception && cardService.isConnectionLost(this)
        return when {
            causes.any { it is TagLostException } || isConnectionLost -> ChipReadError.ConnectionLost
            causes.any { it is BACDeniedException || it is AccessDeniedException } -> ChipReadError.AccessDenied
            causes.any { it is CardServiceException && it.sw in ACCESS_DENIED_STATUS_WORDS } -> ChipReadError.AccessDenied
            else -> ChipReadError.ReadFailed
        }
    }

    private fun MRZInfo.toMrzData() = MrzData(
        format = when (documentType) {
            MRZInfo.DOC_TYPE_ID1 -> MrzFormat.TD1
            MRZInfo.DOC_TYPE_ID2 -> MrzFormat.TD2
            else -> MrzFormat.TD3
        },
        documentCode = documentCode.orEmpty(),
        issuingState = issuingState.orEmpty(),
        documentNumber = documentNumber.orEmpty(),
        dateOfBirth = dateOfBirth.orEmpty(),
        sex = when (gender) {
            Gender.MALE -> "M"
            Gender.FEMALE -> "F"
            else -> "<"
        },
        dateOfExpiry = dateOfExpiry.orEmpty(),
        nationality = nationality.orEmpty(),
        primaryIdentifier = primaryIdentifier.orEmpty().replace('<', ' ').trim(),
        secondaryIdentifier = secondaryIdentifier.orEmpty().replace('<', ' ').trim(),
        optionalData1 = optionalData1.orEmpty().trim('<'),
        optionalData2 = optionalData2.orEmpty().trim('<'),
    )

    private companion object {
        const val ISO_DEP_TIMEOUT_MS = 10_000
        const val DG1_NUMBER = 1

        // 0x6300 - authentication failed, 0x6982 - security status not satisfied
        val ACCESS_DENIED_STATUS_WORDS = setOf(0x6300, 0x6982)
    }
}
