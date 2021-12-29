package vm8.cpu.z80

import io.kotest.core.spec.style.*
import io.kotest.matchers.*

import vm8.cpu.z80.*
import vm8.data.*

internal class OperandsTest : FunSpec({

    val sys = MinimalSystem()
    val cpu = Processor(sys)

    context("8-bit register operands") {
        abstract class RegRef {
            abstract val name: String
            abstract val op: Reg8
            abstract var reg: Octet
        }
    
        fun regAsOperatorTest(ref: RegRef) = funSpec {
            test("$ref.name as source operator") {
                ref.reg = Octet(0xAB)
                cpu.load8(ref.op) shouldBe Octet(0xAB)
            }
    
            test("$ref.name as destination operator") {
                cpu.store8(ref.op, Octet(0xAB))
                ref.reg shouldBe Octet(0xAB)
            }
        }
    
        include(regAsOperatorTest(object : RegRef() {
            override val name: String = "A"
            override val op: Reg8 = Reg8.A
            override var reg: Octet by cpu.regs::a
        }))
    
        include(regAsOperatorTest(object : RegRef() {
            override val name: String = "F"
            override val op: Reg8 = Reg8.F
            override var reg: Octet by cpu.regs::f
        }))
    
        include(regAsOperatorTest(object : RegRef() {
            override val name: String = "B"
            override val op: Reg8 = Reg8.B
            override var reg: Octet by cpu.regs::b
        }))
    
        include(regAsOperatorTest(object : RegRef() {
            override val name: String = "C"
            override val op: Reg8 = Reg8.C
            override var reg: Octet by cpu.regs::c
        }))
    
        include(regAsOperatorTest(object : RegRef() {
            override val name: String = "D"
            override val op: Reg8 = Reg8.D
            override var reg: Octet by cpu.regs::d
        }))
    
        include(regAsOperatorTest(object : RegRef() {
            override val name: String = "E"
            override val op: Reg8 = Reg8.E
            override var reg: Octet by cpu.regs::e
        }))
    
        include(regAsOperatorTest(object : RegRef() {
            override val name: String = "H"
            override val op: Reg8 = Reg8.H
            override var reg: Octet by cpu.regs::h
        }))
    
        include(regAsOperatorTest(object : RegRef() {
            override val name: String = "L"
            override val op: Reg8 = Reg8.L
            override var reg: Octet by cpu.regs::l
        }))
    }

    context("16-bit register operands") {
        abstract class RegRef {
            abstract val name: String
            abstract val op: Reg16
            abstract var reg: Word
        }
    
        fun regAsOperatorTest(ref: RegRef) = funSpec {
            test("$ref.name as source operator") {
                ref.reg = Word(0xABCD)
                cpu.load16(ref.op) shouldBe Word(0xABCD)
            }
    
            test("$ref.name as destination operator") {
                cpu.store16(ref.op, Word(0xABCD))
                ref.reg shouldBe Word(0xABCD)
            }
        }
    
        include(regAsOperatorTest(object : RegRef() {
            override val name: String = "AF"
            override val op: Reg16 = Reg16.AF
            override var reg: Word by cpu.regs::af
        }))
    
        include(regAsOperatorTest(object : RegRef() {
            override val name: String = "BC"
            override val op: Reg16 = Reg16.BC
            override var reg: Word by cpu.regs::bc
        }))
    
        include(regAsOperatorTest(object : RegRef() {
            override val name: String = "DE"
            override val op: Reg16 = Reg16.DE
            override var reg: Word by cpu.regs::de
        }))
    
        include(regAsOperatorTest(object : RegRef() {
            override val name: String = "HL"
            override val op: Reg16 = Reg16.HL
            override var reg: Word by cpu.regs::hl
        }))
    
        include(regAsOperatorTest(object : RegRef() {
            override val name: String = "PC"
            override val op: Reg16 = Reg16.PC
            override var reg: Word by cpu.regs::pc
        }))
    }

    context("immediate operands") {
        test("immediate word as source operator") {
            cpu.regs.pc = Word(0xA800)
            sys.memory[0xA801] = 0xCD.toByte()
            sys.memory[0xA802] = 0xAB.toByte()

            cpu.load16(Imm16) shouldBe Word(0xABCD)
        }
    }
})
