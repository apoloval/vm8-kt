package vm8.cpu.z80

import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.funSpec
import io.kotest.matchers.shouldBe

internal class RegsBankTest : FunSpec({

    val bank = RegsBank()

    abstract class Register {
        abstract val name: String
        abstract var word: UShort
        abstract var high: UByte
        abstract var low: UByte
    }

    fun registerPairsTests(reg: Register) = funSpec {
        test("$reg.name register as word") {
            reg.word = 0xABCDu

            reg.word shouldBe 0xABCDu
            reg.high shouldBe 0xABu
            reg.low shouldBe 0xCDu
        }

        test("$reg.name register as high octet") {
            reg.word = 0xABCDu
            reg.high = 0x12u
    
            reg.word shouldBe 0x12CDu
            reg.high shouldBe 0x12u
            reg.low shouldBe 0xCDu
        }

        test("$reg.name register as low octet") {
            reg.word = 0xABCDu
            reg.low = 0x12u
    
            reg.word shouldBe 0xAB12u
            reg.high shouldBe 0xABu
            reg.low shouldBe 0x12u
        }
    }

    include(registerPairsTests(object : Register() {
        override val name: String = "AF"
        override var word: UShort by bank::af
        override var high: UByte by bank::a
        override var low: UByte by bank::f
    }))

    include(registerPairsTests(object : Register() {
        override val name: String = "BC"
        override var word: UShort by bank::bc
        override var high: UByte by bank::b
        override var low: UByte by bank::c
    }))

    include(registerPairsTests(object : Register() {
        override val name: String = "DE"
        override var word: UShort by bank::de
        override var high: UByte by bank::d
        override var low: UByte by bank::e
    }))

    include(registerPairsTests(object : Register() {
        override val name: String = "HL"
        override var word: UShort by bank::hl
        override var high: UByte by bank::h
        override var low: UByte by bank::l
    }))
})
