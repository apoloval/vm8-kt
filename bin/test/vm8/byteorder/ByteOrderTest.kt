package vm8.byteorder

import kotlin.test.Test
import kotlin.test.assertEquals

import vm8.byteorder.*

internal class ByteOrderTest {

    val ab = 0xAB.toByte()
    val cd = 0xCD.toByte()

    @Test
    fun testDecodeShort() {
        assertEquals(0xABCD.toShort(), ByteOrder.BIG_ENDIAN.decode(ab, cd))
        assertEquals(0xCDAB.toShort(), ByteOrder.LITTLE_ENDIAN.decode(ab, cd))
    }
}
