package com.simprints.fingerprintscanner.v2.domain


@Deprecated(message = "Use new extended api, this format will be no longer supported")
abstract class FirmwareVersion (
    val apiMajorVersion: Short,
    val apiMinorVersion: Short,
    val firmwareMajorVersion: Short,
    val firmwareMinorVersion: Short
) {

    fun toNewVersionNamingScheme(): String {
        return "${firmwareMajorVersion}.E-1.${firmwareMinorVersion}"
    }
}

