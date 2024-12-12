package com.simprints.fingerprint.infra.basebiosdk.exceptions

sealed class BioSdkException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    class BioSdkInitializationException(
        override val cause: Throwable? = null,
        override val message: String? = null,
    ) : BioSdkException("Bio SDK initialization failed")

    class CannotAcquireFingerprintImageException(
        override val message: String?,
    ) : BioSdkException("Cannot acquire fingerprint")

    class ImageDecodingException : BioSdkException("Cannot decode WSQ image")

    class ImageQualityCheckingException(
        override val cause: Throwable?,
    ) : BioSdkException("Cannot check image quality")

    class TemplateExtractionException(
        override val cause: Throwable?,
    ) : BioSdkException("Cannot extract template")

    class TemplateMatchingException(
        override val cause: Throwable?,
    ) : BioSdkException("Cannot match template")
}
