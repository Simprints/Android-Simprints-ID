package com.simprints.fingerprint.infra.scanner.v2.tools

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

fun <T> Flow<T>.mapPotentialErrorFromScanner() = catch { ex -> throw wrapErrorFromScanner(ex) }
