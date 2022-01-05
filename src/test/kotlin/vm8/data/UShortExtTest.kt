package vm8.data

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class UShortExtTest : FunSpec({
    test("low byte") {
        0x1234u.toUShort().low() shouldBe 0x34u
        0xABCDu.toUShort().low() shouldBe 0xCDu
    }

    test("high byte") {
        0x1234u.toUShort().high() shouldBe 0x12u
        0xABCDu.toUShort().high() shouldBe 0xABu
    }

    test("set low byte") {
        0x1234u.toUShort().setLow(0xABu) shouldBe 0x12ABu
        0xABCDu.toUShort().setLow(0x12u) shouldBe 0xAB12u
    }

    test("set high byte") {
        0x1234u.toUShort().setHigh(0xABu) shouldBe 0xAB34u
        0xABCDu.toUShort().setHigh(0x12u) shouldBe 0x12CDu
    }

    test("increment by ubyte") {
        0x0000u.toUShort().increment(0x40u) shouldBe 0x0040u
        0xF000u.toUShort().increment(0x40u) shouldBe 0xF040u
        0xFF00u.toUShort().increment(0x40u) shouldBe 0xFF40u
        0xFFF0u.toUShort().increment(0x40u) shouldBe 0x0030u
    }

    test("increment by byte") {
        0x0000u.toUShort().increment(-0x40) shouldBe 0xFFC0u
        0xF000u.toUShort().increment(-0x40) shouldBe 0xEFC0u
        0xFF00u.toUShort().increment(-0x40) shouldBe 0xFEC0u
        0xFFF0u.toUShort().increment(-0x40) shouldBe 0xFFB0u
    }
})
