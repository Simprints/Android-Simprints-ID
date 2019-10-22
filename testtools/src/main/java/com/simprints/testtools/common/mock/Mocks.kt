package com.simprints.testtools.common.mock

import java.util.*

/**
 * Generates a mock fingerprint or face template: a byte array larger than 256B
 */
fun mockTemplate(): ByteArray {
    return ("askdfjhsalkdjfhlaksdjfhaklsdhfjjaksdfgkashdgfkjashbdfkjasbfhkajsfsgfs" +
        "faskdjhfkajsfhalskfhaslkhf${UUID.randomUUID()}asdjfhgaskjfgjkasfgkjasfgas" +
        "sadfhjkgsakjfgjasgfashfgdjkashdgfkajshdfgkajshdgfkjashfdgajkshdgfkajshfgas" +
        "asjhdfgkjasgdfkjashgfkajsdhgfkajshfgkajsfghkajsgfhkajsfgakjsfgajshfgkajsfga" +
        "asjfhgkajsghfkjasfghkjasgfkajsfgakjsfghakjsfhgaksjfhgkajsfgaksjfgaskj" +
        "jshfdgkajsgfakjsghfkajsgfajshfgakjshfgajshfgajksfghakjsfghaskjf").toByteArray()
}
