package vm8.cpu.z80

import io.kotest.core.spec.style.*
import io.kotest.matchers.*

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
})
