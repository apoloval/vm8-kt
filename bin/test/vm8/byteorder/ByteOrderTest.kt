package vm8.byteorder

import kotlin.test.Test
import kotlin.test.assertEquals

import vm8.byteorder.*
import vm8.data.*

internal class ByteOrderTest {

    val ab = Octet(0xAB)
    val cd = Octet(0xCD)

    @Test
    fun testDecodeShort() {
        assertEquals(0xABCD.toWord(), ByteOrder.BIG_ENDIAN.decode(ab, cd))
        assertEquals(0xCDAB.toWord(), ByteOrder.LITTLE_ENDIAN.decode(ab, cd))
    }
}
