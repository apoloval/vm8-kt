package vm8.cpu.z80

import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.funSpec
import io.kotest.matchers.shouldBe

internal class OperandsTest : FunSpec({

    val sys = MinimalSystem()
    val cpu = Processor(sys)

    context("8-bit register operands") {
        abstract class RegRef {
            abstract val name: String
            abstract val op: Reg8
            abstract var reg: UByte
        }
    
        fun regAsOperatorTest(ref: RegRef) = funSpec {
            test("$ref.name as source operator") {
                ref.reg = 0xABu
                cpu.load8(ref.op) shouldBe 0xABu
            }
    
            test("$ref.name as destination operator") {
                cpu.store8(ref.op, 0xABu)
                ref.reg shouldBe 0xABu
            }
        }
    
        include(regAsOperatorTest(object : RegRef() {
            override val name: String = "A"
            override val op: Reg8 = Reg8.A
            override var reg: UByte by cpu.regs::a
        }))
    
        include(regAsOperatorTest(object : RegRef() {
            override val name: String = "F"
            override val op: Reg8 = Reg8.F
            override var reg: UByte by cpu.regs::f
        }))
    
        include(regAsOperatorTest(object : RegRef() {
            override val name: String = "B"
            override val op: Reg8 = Reg8.B
            override var reg: UByte by cpu.regs::b
        }))
    
        include(regAsOperatorTest(object : RegRef() {
            override val name: String = "C"
            override val op: Reg8 = Reg8.C
            override var reg: UByte by cpu.regs::c
        }))
    
        include(regAsOperatorTest(object : RegRef() {
            override val name: String = "D"
            override val op: Reg8 = Reg8.D
            override var reg: UByte by cpu.regs::d
        }))
    
        include(regAsOperatorTest(object : RegRef() {
            override val name: String = "E"
            override val op: Reg8 = Reg8.E
            override var reg: UByte by cpu.regs::e
        }))
    
        include(regAsOperatorTest(object : RegRef() {
            override val name: String = "H"
            override val op: Reg8 = Reg8.H
            override var reg: UByte by cpu.regs::h
        }))
    
        include(regAsOperatorTest(object : RegRef() {
            override val name: String = "L"
            override val op: Reg8 = Reg8.L
            override var reg: UByte by cpu.regs::l
        }))
    }

    context("16-bit register operands") {
        abstract class RegRef {
            abstract val name: String
            abstract val op: Reg16
            abstract var reg: UShort
        }
    
        fun regAsOperatorTest(ref: RegRef) = funSpec {
            test("$ref.name as source operator") {
                ref.reg = 0xABCDu
                cpu.load16(ref.op) shouldBe 0xABCDu
            }
    
            test("$ref.name as destination operator") {
                cpu.store16(ref.op, 0xABCDu)
                ref.reg shouldBe 0xABCDu
            }
        }
    
        include(regAsOperatorTest(object : RegRef() {
            override val name: String = "AF"
            override val op: Reg16 = Reg16.AF
            override var reg: UShort by cpu.regs::af
        }))
    
        include(regAsOperatorTest(object : RegRef() {
            override val name: String = "BC"
            override val op: Reg16 = Reg16.BC
            override var reg: UShort by cpu.regs::bc
        }))
    
        include(regAsOperatorTest(object : RegRef() {
            override val name: String = "DE"
            override val op: Reg16 = Reg16.DE
            override var reg: UShort by cpu.regs::de
        }))
    
        include(regAsOperatorTest(object : RegRef() {
            override val name: String = "HL"
            override val op: Reg16 = Reg16.HL
            override var reg: UShort by cpu.regs::hl
        }))
    
        include(regAsOperatorTest(object : RegRef() {
            override val name: String = "PC"
            override val op: Reg16 = Reg16.PC
            override var reg: UShort by cpu.regs::pc
        }))

        include(regAsOperatorTest(object : RegRef() {
            override val name: String = "SP"
            override val op: Reg16 = Reg16.SP
            override var reg: UShort by cpu.regs::sp
        }))
    }

    context("8-bit immediate") {
        test("as source operand") {
            cpu.regs.pc = 0xA800u
            sys.memory[0xA801] = 0x42.toByte()

            cpu.load8(Imm8) shouldBe 0x42u
        }
    }

    context("16-bit immediate") {
        test("as source operand") {
            cpu.regs.pc = 0xA800u
            sys.memory[0xA801] = 0xCD.toByte()
            sys.memory[0xA802] = 0xAB.toByte()

            cpu.load16(Imm16) shouldBe 0xABCDu
        }
    }

    context("8-bit indirect") {
        test("as source operand") {
            cpu.regs.hl = 0xA800u
            sys.memory[0xA800] = 0x42.toByte()

            cpu.load8(Ind8(Reg16.HL)) shouldBe 0x42u
        }

        test("as dest operand") {
            cpu.regs.hl = 0xA800u
            sys.memory[0xA800] = 0x00.toByte()

            cpu.store8(Ind8(Reg16.HL), 0x42u)
            
            sys.memory[0xA800] shouldBe 0x42.toByte()
        }
    }

    context("16-bit indirect") {
        test("as source operand") {
            cpu.regs.hl = 0xA800u
            sys.memory[0xA800] = 0xCD.toByte()
            sys.memory[0xA801] = 0xAB.toByte()

            cpu.load16(Ind16(Reg16.HL)) shouldBe 0xABCDu
        }

        test("as dest operand") {
            cpu.regs.hl = 0xA800u
            sys.memory[0xA800] = 0x00.toByte()
            sys.memory[0xA801] = 0x0.toByte()

            cpu.store16(Ind16(Reg16.HL), 0xABCDu)

            sys.memory[0xA800] shouldBe 0xCD.toByte()
            sys.memory[0xA801] shouldBe 0xAB.toByte()
        }
    }
})
