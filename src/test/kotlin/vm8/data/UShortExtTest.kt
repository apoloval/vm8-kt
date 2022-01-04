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
})
