package vm8.cpu.z80

import io.kotest.core.spec.style.*
import io.kotest.matchers.*
import io.kotest.assertions.throwables.shouldThrow

import vm8.cpu.z80.*
import vm8.data.*

internal class InstructionsTest : FunSpec({

    val sys = MinimalSystem()
    val cpu = Processor(sys)

    beforeEach { 
        cpu.reset() 
        cpu.regs.f = Octet(0x00)
    }

    test("DEC8 instruction") {
        cpu.regs.a = Octet(0x42)
        val cycles = with(Dec8(Reg8.A)) { cpu.exec() }

        cycles shouldBe 4
        cpu.regs.pc shouldBe Word(0x0001)
        cpu.regs.a shouldBe Octet(0x41)
    }

    test("EX instruction") {
        cpu.regs.af = Word(0xABCD)
        cpu.regs.`af'` = Word(0x1234)
        val cycles = cpu.run(Ex(Reg16.AF, Reg16.`AF'`, cycles = 4, size = 1))

        cycles shouldBe 4
        cpu.regs.pc shouldBe Word(0x0001)
        cpu.regs.af shouldBe Word(0x1234)
        cpu.regs.`af'` shouldBe Word(0xABCD)
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

    test("INC16 instruction") {
        cpu.regs.hl = Word(0xABCD)
        val cycles = with(Inc16(Reg16.HL, cycles = 6, size = 1)) { cpu.exec() }

        cycles shouldBe 6
        cpu.regs.pc shouldBe Word(0x0001)
        cpu.regs.hl shouldBe Word(0xABCE)
    }

    test("LD8 instruction") {
        cpu.regs.a = Octet(0x00)
        cpu.regs.pc = Word(0x4000)
        sys.memory[0x4001] = 0x42.toByte()
        val cycles = with(Ld8(Reg8.A, Imm8, cycles = 7, size = 2)) { cpu.exec() }

        cycles shouldBe 7
        cpu.regs.pc shouldBe Word(0x4002)
        cpu.regs.a shouldBe Octet(0x42)
    }

    test("LD16 instruction") {
        cpu.regs.bc = Word(0x0000)
        cpu.regs.pc = Word(0x4000)
        sys.memory[0x4001] = 0xCD.toByte()
        sys.memory[0x4002] = 0xAB.toByte()
        val cycles = with(Ld16(Reg16.BC, Imm16, 10, 3)) { cpu.exec() }

        cycles shouldBe 10
        cpu.regs.pc shouldBe Word(0x4003)
        cpu.regs.bc shouldBe Word(0xABCD)
    }

    test("NOP instruction") {
        val cycles = with(Nop) { cpu.exec() }

        cycles shouldBe 4
        cpu.regs.pc shouldBe Word(0x0001)
    }

    test("RLCA instruction") {
        cpu.regs.a = Octet(0b10010101)
        val cycles = cpu.run(Rlca(cycles = 4, size = 1))

        cycles shouldBe 4
        cpu.regs.pc shouldBe Word(0x0001)
        cpu.regs.a shouldBe Octet(0b00101010)
        cpu.regs.f shouldBe Octet(0b00101001)

        cpu.run(Rlca(cycles = 4, size = 1))

        cpu.regs.a shouldBe Octet(0b01010101)
        cpu.regs.f shouldBe Octet(0b00000000)
    }

    test("illegal instruction") {
        shouldThrow<Exception> {
            with(Illegal) { cpu.exec() }
        }
    }
})
