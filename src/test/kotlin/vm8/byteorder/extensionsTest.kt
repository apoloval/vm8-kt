package vm8.byteorder

import kotlin.test.Test
import kotlin.test.assertEquals

import vm8.byteorder.*

internal class UShortExtTest {

    val abcd = 0xABCD.toShort()

    @Test
    fun testShortLow() {
        assertEquals(0xCD.toByte(), abcd.low())
    }

    @Test
    fun testShortSetLow() {
        assertEquals(0xABEF.toShort(), abcd.setLow(0xEF.toByte()))
    }

    @Test
    fun testShortHigh() {
        assertEquals(0xAB.toByte(), abcd.high())
    }

    @Test
    fun testShortSetHigh() {
        assertEquals(0xEFCD.toShort(), abcd.setHigh(0xEF.toByte()))
    }

}
