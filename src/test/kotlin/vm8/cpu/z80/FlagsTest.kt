package vm8.cpu.z80

import io.kotest.core.spec.style.*
import io.kotest.data.row
import io.kotest.data.forAll as forAllData
import io.kotest.matchers.*
import io.kotest.property.*

import vm8.data.*

internal class FlagsTest : FunSpec({

    val sys = MinimalSystem()
    val cpu = Processor(sys)

    beforeEach { 
        cpu.reset() 
        cpu.regs.f = Octet(0)
    }

    test("set/clear flag C") {
        cpu.apply(+Flag.C)
        cpu.regs.f shouldBe Octet(0b00000001)
        cpu.apply(-Flag.C)
        cpu.regs.f shouldBe Octet(0b00000000)
    }

    test("set/clear flag N") {
        cpu.apply(+Flag.N)
        cpu.regs.f shouldBe Octet(0b00000010)
        cpu.apply(-Flag.N)
        cpu.regs.f shouldBe Octet(0b00000000)
    }

    test("set/clear flag P/V") {
        cpu.apply(+Flag.PV)
        cpu.regs.f shouldBe Octet(0b00000100)
        cpu.apply(-Flag.PV)
        cpu.regs.f shouldBe Octet(0b00000000)
    }

    test("set/clear flag F3") {
        cpu.apply(+Flag.F3)
        cpu.regs.f shouldBe Octet(0b00001000)
        cpu.apply(-Flag.F3)
        cpu.regs.f shouldBe Octet(0b00000000)
    }

    test("set/clear flag H") {
        cpu.apply(+Flag.H)
        cpu.regs.f shouldBe Octet(0b00010000)
        cpu.apply(-Flag.H)
        cpu.regs.f shouldBe Octet(0b00000000)
    }

    test("set/clear flag F5") {
        cpu.apply(+Flag.F5)
        cpu.regs.f shouldBe Octet(0b00100000)
        cpu.apply(-Flag.F5)
        cpu.regs.f shouldBe Octet(0b00000000)
    }

    test("set/clear flag Z") {
        cpu.apply(+Flag.Z)
        cpu.regs.f shouldBe Octet(0b01000000)
        cpu.apply(-Flag.Z)
        cpu.regs.f shouldBe Octet(0b00000000)
    }

    test("set/clear flag S") {
        cpu.apply(+Flag.S)
        cpu.regs.f shouldBe Octet(0b10000000)
        cpu.apply(-Flag.S)
        cpu.regs.f shouldBe Octet(0b00000000)
    }

    test("set/clear several flags") {
        cpu.apply(+Flag.C + Flag.PV + Flag.H + Flag.Z)
        cpu.regs.f shouldBe Octet(0b01010101)
        cpu.apply(+Flag.N + Flag.F3 + Flag.F5 + Flag.S)
        cpu.regs.f shouldBe Octet(0b11111111)
        cpu.apply(-Flag.C - Flag.PV - Flag.H - Flag.Z)
        cpu.regs.f shouldBe Octet(0b10101010)
    }

    context("precomputed intrinsic") {
        test("S flag") {
            PrecomputedFlags.intrinsicOf(Octet(0x00)).applyTo(Octet(0x00)).bit(7) shouldBe false
            PrecomputedFlags.intrinsicOf(Octet(0x80)).applyTo(Octet(0x00)).bit(7) shouldBe true
        }
        test("Z flag") {
            PrecomputedFlags.intrinsicOf(Octet(0x00)).applyTo(Octet(0x00)).bit(6) shouldBe true
            PrecomputedFlags.intrinsicOf(Octet(0x80)).applyTo(Octet(0x00)).bit(6) shouldBe false
        }
        test("F5 flag") {
            PrecomputedFlags.intrinsicOf(Octet(0b00000000)).applyTo(Octet(0x00)).bit(5) shouldBe false
            PrecomputedFlags.intrinsicOf(Octet(0b00100000)).applyTo(Octet(0x00)).bit(5) shouldBe true
        }
        test("F3 flag") {
            PrecomputedFlags.intrinsicOf(Octet(0b00000000)).applyTo(Octet(0x00)).bit(3) shouldBe false
            PrecomputedFlags.intrinsicOf(Octet(0b00001000)).applyTo(Octet(0x00)).bit(3) shouldBe true
        }
    }

    context("precomputed ADD/ADC") {
        test("H flag") {
            forAllData(
                row(0x0F, 0x01, true),
                row(0x01, 0x0F, true),
                row(0x01, 0xFF, true),
                row(0x0F, 0xFF, true),
                row(0x01, 0x01, false),
                row(0xF1, 0xF1, false),
            ) { a, b, res -> 
                PrecomputedFlags.ofAdd(Octet(a), Octet(b)).applyTo(Octet(0x00)).bit(4) shouldBe res
            }
        }
        test("V flag") {
            forAllData(
                row(0x01, 0x01, false),
                row(0x7F, 0x01, true),
                row(0xFF, 0x01, false),
            ) { a, b, res -> 
                PrecomputedFlags.ofAdd(Octet(a), Octet(b)).applyTo(Octet(0x00)).bit(2) shouldBe res
            }
        }
        test("N flag") {
            forAll<Byte, Byte> { a, b ->
                !PrecomputedFlags.ofAdd(a.toOctet(), b.toOctet()).applyTo(Octet(0x00)).bit(1)
            }
        }
    }

    context("precomputed SUB/SBC") {
        test("H flag") {
            forAllData(
                row(0x10, 0x01, true),
                row(0x01, 0x12, true),
                row(0x01, 0x01, false),
                row(0xF1, 0xF1, false),
            ) { a, b, res -> 
                PrecomputedFlags.ofSub(Octet(a), Octet(b)).applyTo(Octet(0x00)).bit(4) shouldBe res
            }
        }
        test("V flag") {
            forAllData(
                row(0x01, 0x01, false),
                row(0x80, 0x01, true),
                row(0x00, 0x01, false),
            ) { a, b, res -> 
                PrecomputedFlags.ofSub(Octet(a), Octet(b)).applyTo(Octet(0x00)).bit(2) shouldBe res
            }
        }
        test("N flag") {
            forAll<Byte, Byte> { a, b ->
                PrecomputedFlags.ofSub(a.toOctet(), b.toOctet()).applyTo(Octet(0x00)).bit(1)
            }
        }
    }

    context("precomputed INC") {
        test("H flag") {
            forAllData(
                row(0x0F, true),
                row(0x01, false),
                row(0xF1, false),
            ) { a, res -> 
                PrecomputedFlags.ofInc(Octet(a)).applyTo(Octet(0x00)).bit(4) shouldBe res
            }
        }
        test("V flag") {
            forAllData(
                row(0x01, false),
                row(0x7F, true),
                row(0xFF, false),
            ) { a, res -> 
                PrecomputedFlags.ofInc(Octet(a)).applyTo(Octet(0x00)).bit(2) shouldBe res
            }
        }
        test("N flag") {
            forAll<Byte> { a ->
                !PrecomputedFlags.ofInc(a.toOctet()).applyTo(Octet(0x00)).bit(1)
            }
        }
        test("C flag") {
            forAll<Byte> { a ->
                !PrecomputedFlags.ofInc(a.toOctet()).applyTo(Octet(0x00)).bit(0) &&
                    PrecomputedFlags.ofInc(a.toOctet()).applyTo(Octet(0x01)).bit(0)
            }
        }
    }

    context("precomputed DEC") {
        test("H flag") {
            forAllData(
                row(0x00, true),
                row(0x0F, false),
                row(0xF1, false),
            ) { a, res -> 
                PrecomputedFlags.ofDec(Octet(a)).applyTo(Octet(0x00)).bit(4) shouldBe res
            }
        }
        test("V flag") {
            forAllData(
                row(0x01, false),
                row(0x80, true),
                row(0x00, false),
            ) { a, res -> 
                PrecomputedFlags.ofDec(Octet(a)).applyTo(Octet(0x00)).bit(2) shouldBe res
            }
        }
        test("N flag") {
            forAll<Byte> { a ->
                PrecomputedFlags.ofDec(a.toOctet()).applyTo(Octet(0x00)).bit(1)
            }
        }
        test("C flag") {
            forAll<Byte> { a ->
                !PrecomputedFlags.ofDec(a.toOctet()).applyTo(Octet(0x00)).bit(0) &&
                    PrecomputedFlags.ofDec(a.toOctet()).applyTo(Octet(0x01)).bit(0)
            }
        }
    }

    context("precomputed RLCA/RLA/RRCA/RRA") {
        test("H and N flags") {
            forAll<Byte, Boolean> { a, carry ->
                !PrecomputedFlags.ofRotateA(a.toOctet(), carry).applyTo(Octet(0x00)).bit(1) &&
                    !PrecomputedFlags.ofRotateA(a.toOctet(), carry).applyTo(Octet(0x00)).bit(4)
            }
        }
        test("F3 and F5 flags") {
            forAll<Byte, Boolean> { a, carry ->
                val v = a.toOctet()
                PrecomputedFlags.ofRotateA(v, carry).applyTo(Octet(0x00)).bit(3) == v.bit(3) &&
                    PrecomputedFlags.ofRotateA(v, carry).applyTo(Octet(0x00)).bit(5) == v.bit(5)
            }
        }
        test("C flag") {
            forAll<Byte, Boolean> { a, carry ->
                val v = a.toOctet()
                PrecomputedFlags.ofRotateA(v, carry).applyTo(Octet(0x00)).bit(0) == carry
            }
        }
    }
})
