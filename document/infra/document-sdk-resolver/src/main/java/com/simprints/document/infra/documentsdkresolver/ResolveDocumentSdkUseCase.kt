package com.simprints.document.infra.documentsdkresolver

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResolveDocumentSdkUseCase @Inject constructor(
    private val mlKitDocumentSdk: MlKitDocumentSdk,
) {
    suspend operator fun invoke(): DocumentSDK = mlKitDocumentSdk
}
