package com.simprints.id.tools.utils

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.take

fun <T : Any> generateSequenceN(n: Int, f: () -> T) = generateSequence(f).take(n)
fun <T : Any> generateFlowN(n: Int, f: () -> T) = f.asFlow().take(n)
