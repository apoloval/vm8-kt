package vm8.data

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll

class UByteExtTest : FunSpec({
    test("low/high") {
        0x12u.toUByte().low() shouldBe 0x02u.toUByte()
        0x12u.toUByte().high() shouldBe 0x01u.toUByte()
        0xABu.toUByte().low() shouldBe 0x0Bu.toUByte()
        0xABu.toUByte().high() shouldBe 0x0Au.toUByte()
    }

    test("bit set") {
        0b00000000u.toUByte().bitSet(0b01010101u) shouldBe 0b01010101u
        0b00000001u.toUByte().bitSet(0b01010101u) shouldBe 0b01010101u
        0b00000011u.toUByte().bitSet(0b01010101u) shouldBe 0b01010111u
    }

    test("bit clear") {
        0b11111111u.toUByte().bitClear(0b01010101u) shouldBe 0b10101010u
        0b11111110u.toUByte().bitClear(0b01010101u) shouldBe 0b10101010u
        0b11111100u.toUByte().bitClear(0b01010101u) shouldBe 0b10101000u
    }

    test("bit") {
        0b01010101u.toUByte().bit(0) shouldBe true
        0b01010101u.toUByte().bit(1) shouldBe false
        0b01010101u.toUByte().bit(2) shouldBe true
        0b01010101u.toUByte().bit(3) shouldBe false
        0b01010101u.toUByte().bit(4) shouldBe true
        0b01010101u.toUByte().bit(5) shouldBe false
        0b01010101u.toUByte().bit(6) shouldBe true
        0b01010101u.toUByte().bit(7) shouldBe false
    }

    test("rotate left") {
        checkAll<UByte> { value ->
            val (res, cout) = value.rotateLeft()

            for (i in 1..7) {
                res.bit(i) shouldBe value.bit(i-1)
            }
            res.bit(0) shouldBe value.bit(7)
            cout shouldBe value.bit(7)
        }
    }

    test("rotate left with carry") {
        checkAll<UByte, Boolean> { value, cin ->
            val (res, cout) = value.rotateLeft(cin)

            for (i in 1..7) {
                res.bit(i) shouldBe value.bit(i-1)
            }
            res.bit(0) shouldBe cin
            cout shouldBe value.bit(7)
        }
    }

    test("rotate right") {
        checkAll<UByte> { value ->
            val (res, cout) = value.rotateRight()

            for (i in 0..6) {
                res.bit(i) shouldBe value.bit(i+1)
            }
            res.bit(7) shouldBe value.bit(0)
            cout shouldBe value.bit(0)
        }
    }

    test("rotate right with carry") {
        checkAll<UByte, Boolean> { value, cin ->
            val (res, cout) = value.rotateRight(cin)

            for (i in 0..6) {
                res.bit(i) shouldBe value.bit(i+1)
            }
            res.bit(7) shouldBe cin
            cout shouldBe value.bit(0)
        }
    }

    test("increment by ubyte") {
        0x00u.toUByte().increment(0x40u) shouldBe 0x40u
        0xF0u.toUByte().increment(0x40u) shouldBe 0x30u
    }

    test("increment by byte") {
        0x00u.toUByte().increment(-0x40) shouldBe 0xC0u
        0xF0u.toUByte().increment(-0x40) shouldBe 0xB0u
    }

    test("parity") {
        0b00000000u.toUByte().parity() shouldBe true
        0b00000001u.toUByte().parity() shouldBe false
        0b00000011u.toUByte().parity() shouldBe true
        0b10000011u.toUByte().parity() shouldBe false
        0b10010011u.toUByte().parity() shouldBe true
    }
})