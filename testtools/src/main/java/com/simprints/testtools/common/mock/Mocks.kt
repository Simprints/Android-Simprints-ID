package com.simprints.testtools.common.mock

import java.util.*

fun mockTemplate(): ByteArray {
    return ("askdfjhsalkdjfhlaksdjfhaklsdhfjjaksdfgkashdgfkjashbdfkjasbfhkajsfsgfs" +
        "faskdjhfkajsfhalskfhaslkhf${UUID.randomUUID()}asdjfhgaskjfgjkasfgkjasfgas" +
        "sadfhjkgsakjfgjasgfashfgdjkashdgfkajshdfgkajshdgfkjashfdgajkshdgfkajshfgas" +
        "asjhdfgkjasgdfkjashgfkajsdhgfkajshfgkajsfghkajsgfhkajsfgakjsfgajshfgkajsfga" +
        "asjfhgkajsghfkjasfghkjasgfkajsfgakjsfghakjsfhgaksjfhgkajsfgaksjfgaskj" +
        "jshfdgkajsgfakjsghfkajsgfajshfgakjshfgajshfgajksfghakjsfghaskjf").toByteArray()
}
