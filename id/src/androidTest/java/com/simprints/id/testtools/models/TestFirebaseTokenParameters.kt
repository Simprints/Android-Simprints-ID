package com.simprints.id.testtools.models

data class TestFirebaseTokenParameters(val projectId: String,
                                       val userId: String = "the_only_user",
                                       val validCertificate: Boolean = true,
                                       val validNonce: Boolean = true,
                                       val validApkCertificateDigest: Boolean = true,
                                       val basicIntegrity: Boolean = true,
                                       val ctsProfileMatch: Boolean = true)
