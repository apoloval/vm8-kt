package vm8.cpu.z80

import io.kotest.core.spec.style.*
import io.kotest.matchers.*

import vm8.cpu.z80.*
import vm8.data.*

internal class RegsBankTest : FunSpec({

    val bank = RegsBank()

    abstract class Register {
        abstract val name: String
        abstract var word: Word
        abstract var high: Octet
        abstract var low: Octet
    }

    fun registerPairsTests(reg: Register) = funSpec {
        test("$reg.name register as word") {
            reg.word = Word(0xABCD)

            reg.word shouldBe Word(0xABCD)
            reg.high shouldBe Octet(0xAB)
            reg.low shouldBe Octet(0xCD)
        }

        test("$reg.name register as high octet") {
            reg.word = Word(0xABCD)
            reg.high = Octet(0x12)
    
            reg.word shouldBe Word(0x12CD)
            reg.high shouldBe Octet(0x12)
            reg.low shouldBe Octet(0xCD)
        }

        test("$reg.name register as low octet") {
            reg.word = Word(0xABCD)
            reg.low = Octet(0x12)
    
            reg.word shouldBe Word(0xAB12)
            reg.high shouldBe Octet(0xAB)
            reg.low shouldBe Octet(0x12)
        }
    }

    include(registerPairsTests(object : Register() {
        override val name: String = "AF"
        override var word: Word by bank::af
        override var high: Octet by bank::a
        override var low: Octet by bank::f
    }))

    include(registerPairsTests(object : Register() {
        override val name: String = "BC"
        override var word: Word by bank::bc
        override var high: Octet by bank::b
        override var low: Octet by bank::c
    }))

    include(registerPairsTests(object : Register() {
        override val name: String = "DE"
        override var word: Word by bank::de
        override var high: Octet by bank::d
        override var low: Octet by bank::e
    }))

    include(registerPairsTests(object : Register() {
        override val name: String = "HL"
        override var word: Word by bank::hl
        override var high: Octet by bank::h
        override var low: Octet by bank::l
    }))
})
