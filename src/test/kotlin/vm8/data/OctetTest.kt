package vm8.cpu.z80

import io.kotest.core.spec.style.*
import io.kotest.matchers.*

import vm8.data.*

class OctetTest : FunSpec({
    test("increment") {
        Octet(0x00).inc() shouldBe Octet(0x01)
        Octet(0xFF).inc() shouldBe Octet(0x00)
    }

    test("decrement") {
        Octet(0x00).dec() shouldBe Octet(0xFF)
        Octet(0xFF).dec() shouldBe Octet(0xFE)
    }

    test("bit set") {
        Octet(0b00000000).bitSet(0b01010101) shouldBe Octet(0b01010101)
        Octet(0b00000001).bitSet(0b01010101) shouldBe Octet(0b01010101)
        Octet(0b00000011).bitSet(0b01010101) shouldBe Octet(0b01010111)
    }

    test("bit clear") {
        Octet(0b11111111).bitClear(0b01010101) shouldBe Octet(0b10101010)
        Octet(0b11111110).bitClear(0b01010101) shouldBe Octet(0b10101010)
        Octet(0b11111100).bitClear(0b01010101) shouldBe Octet(0b10101000)
    }

    test("bit toggle") {
        Octet(0b00000000).bitToggle(0b01010101) shouldBe Octet(0b01010101)
        Octet(0b00000001).bitToggle(0b01010101) shouldBe Octet(0b01010100)
        Octet(0b00000011).bitToggle(0b01010101) shouldBe Octet(0b01010110)
    }

    test("rotate left") {
        Octet(0b00000001).rotateLeft() shouldBe Pair(Octet(0b00000010), false)
        Octet(0b00000011).rotateLeft() shouldBe Pair(Octet(0b00000110), false)
        Octet(0b10000011).rotateLeft() shouldBe Pair(Octet(0b00000111), true)
    }

    test("rotate left with carry") {
        Octet(0b00000001).rotateLeft(carry = true) shouldBe Pair(Octet(0b00000011), false)
        Octet(0b00000001).rotateLeft(carry = false) shouldBe Pair(Octet(0b00000010), false)
        Octet(0b00000011).rotateLeft(carry = true) shouldBe Pair(Octet(0b00000111), false)
        Octet(0b10000011).rotateLeft(carry = true) shouldBe Pair(Octet(0b00000111), true)
        Octet(0b10000011).rotateLeft(carry = false) shouldBe Pair(Octet(0b00000110), true)
    }
})