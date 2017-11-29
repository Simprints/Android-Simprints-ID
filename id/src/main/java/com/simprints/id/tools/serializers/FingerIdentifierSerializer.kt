package com.simprints.id.tools.serializers

import com.simprints.libsimprints.FingerIdentifier

/**
 * Simple serializer for FingerIdentifier.
 *
 * If you manage to write a generic enum serializer that implements the Serializer interface,
 * shoot me an email because I am very much interested!
 *
 * @author: Etienne Thiery (etienne@simprints.com)
 */
class FingerIdentifierSerializer: Serializer<FingerIdentifier> {

    override fun serialize(value: FingerIdentifier): String =
            value.name

    override fun deserialize(string: String): FingerIdentifier =
            FingerIdentifier.valueOf(string)
}