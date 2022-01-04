package vm8.cpu.z80

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import vm8.data.bit
import vm8.data.high
import vm8.data.isNegative
import vm8.data.isZero

internal class FlagsTest : FunSpec({

    val sys = MinimalSystem()
    val cpu = Processor(sys)

    beforeEach { 
        cpu.reset() 
        cpu.regs.f = 0x00u
    }

    test("set/clear flag C") {
        cpu.apply(+Flag.C)
        cpu.regs.f shouldBe 0b00000001u
        cpu.apply(-Flag.C)
        cpu.regs.f shouldBe 0b00000000u
    }

    test("set/clear flag N") {
        cpu.apply(+Flag.N)
        cpu.regs.f shouldBe 0b00000010u
        cpu.apply(-Flag.N)
        cpu.regs.f shouldBe 0b00000000u
    }

    test("set/clear flag P/V") {
        cpu.apply(+Flag.PV)
        cpu.regs.f shouldBe 0b00000100u
        cpu.apply(-Flag.PV)
        cpu.regs.f shouldBe 0b00000000u
    }

    test("set/clear flag F3") {
        cpu.apply(+Flag.F3)
        cpu.regs.f shouldBe 0b00001000u
        cpu.apply(-Flag.F3)
        cpu.regs.f shouldBe 0b00000000u
    }

    test("set/clear flag H") {
        cpu.apply(+Flag.H)
        cpu.regs.f shouldBe 0b00010000u
        cpu.apply(-Flag.H)
        cpu.regs.f shouldBe 0b00000000u
    }

    test("set/clear flag F5") {
        cpu.apply(+Flag.F5)
        cpu.regs.f shouldBe 0b00100000u
        cpu.apply(-Flag.F5)
        cpu.regs.f shouldBe 0b00000000u
    }

    test("set/clear flag Z") {
        cpu.apply(+Flag.Z)
        cpu.regs.f shouldBe 0b01000000u
        cpu.apply(-Flag.Z)
        cpu.regs.f shouldBe 0b00000000u
    }

    test("set/clear flag S") {
        cpu.apply(+Flag.S)
        cpu.regs.f shouldBe 0b10000000u
        cpu.apply(-Flag.S)
        cpu.regs.f shouldBe 0b00000000u
    }

    test("set/clear several flags") {
        cpu.apply(+Flag.C + Flag.PV + Flag.H + Flag.Z)
        cpu.regs.f shouldBe 0b01010101u
        cpu.apply(+Flag.N + Flag.F3 + Flag.F5 + Flag.S)
        cpu.regs.f shouldBe 0b11111111u
        cpu.apply(-Flag.C - Flag.PV - Flag.H - Flag.Z)
        cpu.regs.f shouldBe 0b10101010u
    }

    test("precomputed intrinsic") {
        checkAll<UByte, UByte> { a, f ->
            val res = PrecomputedFlags.intrinsicOf(a).applyTo(f)
            res.bit(3) shouldBe a.bit(3)
            res.bit(5) shouldBe a.bit(5)
            res.bit(6) shouldBe a.isZero()
            res.bit(7) shouldBe a.bit(7)
        }
    }

    test("precomputed ADD/ADC for 8-bit") {
        checkAll<UByte, UByte, UByte> { a, b, f ->
            val c = (a + b).toUByte()
            val res = PrecomputedFlags.ofAdd(a, b).applyTo(f)
            res.bit(0) shouldBe setOnCarry(a, b, mask = 0xFF)
            res.bit(1) shouldBe false
            res.bit(2) shouldBe setOnOverflow(a, b)
            res.bit(3) shouldBe c.bit(3)
            res.bit(4) shouldBe setOnCarry(a, b, mask = 0x0F)
            res.bit(5) shouldBe c.bit(5)
            res.bit(6) shouldBe c.isZero()
            res.bit(7) shouldBe c.isNegative()
        }  
    }

    test("precomputed ADD/ADC for 16-bit") {
        checkAll<UShort, UShort, UByte> { a, b, f ->
            val c = (a + b).toUShort()
            val res = PrecomputedFlags.ofAdd(a, b).applyTo(f)
            res.bit(0) shouldBe (a.toInt() + b.toInt() > 0xFFFF)                
            res.bit(1) shouldBe false
            res.bit(2) shouldBe f.bit(2)
            res.bit(3) shouldBe c.high().bit(3)
            res.bit(5) shouldBe c.high().bit(5)
            res.bit(6) shouldBe f.bit(6)
            res.bit(7) shouldBe f.bit(7)
        }  
    }

    test("precomputed SUB/SBC for 8-bit") {
        checkAll<UByte, UByte, UByte> { a, b, f ->
            val c = (a - b).toUByte()
            val res = PrecomputedFlags.ofSub(a, b).applyTo(f)
            res.bit(0) shouldBe setOnBorrow(a, b, mask = 0xFF)
            res.bit(1) shouldBe true
            res.bit(2) shouldBe setOnUnderflow(a, b)
            res.bit(3) shouldBe c.bit(3)
            res.bit(4) shouldBe setOnBorrow(a, b, mask = 0x0F)
            res.bit(5) shouldBe c.bit(5)
            res.bit(6) shouldBe c.isZero()
            res.bit(7) shouldBe c.isNegative()
        }  
    }

    test("precomputed INC") {
        checkAll<UByte, UByte> { a, f ->
            val b: UByte = 1u
            val c = (a + b).toUByte()
            val res = PrecomputedFlags.ofInc(a).applyTo(f)
            res.bit(0) shouldBe f.bit(0)
            res.bit(1) shouldBe false
            res.bit(2) shouldBe setOnOverflow(a, b)
            res.bit(3) shouldBe c.bit(3)
            res.bit(4) shouldBe setOnCarry(a, b, mask = 0x0F)
            res.bit(5) shouldBe c.bit(5)
            res.bit(6) shouldBe c.isZero()
            res.bit(7) shouldBe c.isNegative()
        }  
    }

    test("precomputed DEC") {
        checkAll<UByte, UByte> { a, f ->
            val b: UByte = 1u
            val c = (a - b).toUByte()
            val res = PrecomputedFlags.ofDec(a).applyTo(f)
            res.bit(0) shouldBe f.bit(0)
            res.bit(1) shouldBe true
            res.bit(2) shouldBe setOnUnderflow(a, b)
            res.bit(3) shouldBe c.bit(3)
            res.bit(4) shouldBe setOnBorrow(a, b, mask = 0x0F)
            res.bit(5) shouldBe c.bit(5)
            res.bit(6) shouldBe c.isZero()
            res.bit(7) shouldBe c.isNegative()
        }  
    }

    test("precomputed RLCA/RLA/RRCA/RRA") {
        checkAll<UByte, UByte, Boolean> { c, f, carry ->
            val res = PrecomputedFlags.ofRotateA(c, carry).applyTo(f)
            res.bit(0) shouldBe carry
            res.bit(1) shouldBe false
            res.bit(2) shouldBe f.bit(2)
            res.bit(3) shouldBe c.bit(3)
            res.bit(4) shouldBe false
            res.bit(5) shouldBe c.bit(5)
            res.bit(6) shouldBe f.bit(6)
            res.bit(7) shouldBe f.bit(7)
        }  
    }
}) 

private fun setOnCarry(a: Int, b: Int, mask: Int) = object : Matcher<Boolean> {
    override fun test(value: Boolean): MatcherResult { 
        val c = a + b
        return MatcherResult(
            value == ((a and mask) > (c and mask)), 
            { "Flag should be set for carry for $a and $b on mask $mask" },
            { "Flag should not be set for carry for $a and $b on mask $mask" }
        )
    }
}

private fun setOnCarry(a: UByte, b: UByte, mask: Int): Matcher<Boolean> = setOnCarry(a.toInt(), b.toInt(), mask)

private fun setOnBorrow(a: Int, b: Int, mask: Int) = object : Matcher<Boolean> {
    override fun test(value: Boolean): MatcherResult { 
        val c = a - b
        return MatcherResult(
            value == ((a and mask) < (c and mask)), 
            { "Flag should be set for borrow for $a and $b on mask $mask" },
            { "Flag should not be set for borrow for $a and $b on mask $mask" }
        )
    }
}

private fun setOnBorrow(a: UByte, b: UByte, mask: Int): Matcher<Boolean> = setOnBorrow(a.toInt(), b.toInt(), mask)

private fun setOnOverflow(a: Int, b: Int) = object : Matcher<Boolean> {
    override fun test(value: Boolean): MatcherResult { 
        val c = a + b
        return MatcherResult(
            value == (((a xor b xor 0x80) and (b xor c) and 0x80) != 0), 
            { "Flag should be set for overflow for $a and $b" }, 
            { "Flag should not be set for overflow for $a and $b" }
        )
    }
}

private fun setOnOverflow(a: UByte, b: UByte): Matcher<Boolean> = setOnOverflow(a.toInt(), b.toInt())

private fun setOnUnderflow(a: Int, b: Int) = object : Matcher<Boolean> {
    override fun test(value: Boolean): MatcherResult { 
        val c = a - b
        return MatcherResult(
            value == (((a xor b) and ((a xor c) and 0x80)) != 0), 
            { "Flag should be set for underflow for $a and $b" }, 
            { "Flag should not be set for underflow for $a and $b" }
        )
    }
}

private fun setOnUnderflow(a: UByte, b: UByte): Matcher<Boolean> = setOnUnderflow(a.toInt(), b.toInt())
