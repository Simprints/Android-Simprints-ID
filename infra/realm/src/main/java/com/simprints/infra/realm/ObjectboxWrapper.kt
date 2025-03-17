package com.simprints.infra.realm

import io.objectbox.BoxStore

interface ObjectboxWrapper {
    suspend fun <R> readObjectBox(block: (BoxStore) -> R): R

    suspend fun <R> writeObjectBox(block: (BoxStore) -> R)
}
