package com.simprints.fingerprint.infra.scanner.v2.domain

@Deprecated(message = "Use new extended api, this format will be no longer supported")
abstract class FirmwareVersion(
    val apiMajorVersion: Short,
    val apiMinorVersion: Short,
    val firmwareMajorVersion: Short,
    val firmwareMinorVersion: Short,
) {
    fun toNewVersionNamingScheme(): String = "$firmwareMajorVersion.E-1.$firmwareMinorVersion"
}
