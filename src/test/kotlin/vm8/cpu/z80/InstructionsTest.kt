package vm8.cpu.z80

import io.kotest.core.spec.style.*
import io.kotest.matchers.*
import io.kotest.assertions.throwables.shouldThrow

import vm8.cpu.z80.*
import vm8.data.*

internal class InstructionsTest : FunSpec({

    val sys = MinimalSystem()
    val cpu = Processor(sys)

    beforeEach { cpu.reset() }

    test("DEC8 instruction") {
        cpu.regs.a = Octet(0x42)
        val cycles = with(Dec8(Reg8.A)) { cpu.exec() }

        cycles shouldBe 4
        cpu.regs.pc shouldBe Word(0x0001)
        cpu.regs.a shouldBe Octet(0x41)
    }

    test("JP instruction") {
        cpu.regs.hl = Word(0xABCD)
        val cycles = with(Jp(Reg16.HL)) { cpu.exec() }

        cycles shouldBe 10
        cpu.regs.pc shouldBe Word(0xABCD)
    }

    test("INC8 instruction") {
        cpu.regs.a = Octet(0x42)
        val cycles = with(Inc8(Reg8.A)) { cpu.exec() }

        cycles shouldBe 4
        cpu.regs.pc shouldBe Word(0x0001)
        cpu.regs.a shouldBe Octet(0x43)
    }

    test("NOP instruction") {
        val cycles = with(Nop) { cpu.exec() }

        cycles shouldBe 4
        cpu.regs.pc shouldBe Word(0x0001)
    }

    test("illegal instruction") {
        shouldThrow<Exception> {
            with(Illegal) { cpu.exec() }
        }
    }
})
