package vm8.cpu.z80

import io.kotest.assertions.*
import io.kotest.core.spec.style.*
import io.kotest.data.row
import io.kotest.data.forAll as forAllData
import io.kotest.matchers.*
import io.kotest.property.*
import io.kotest.property.arbitrary.boolean

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

    test("precomputed intrinsic") {
        checkAll(OctetArb, OctetArb) { a, f -> 
            val res = PrecomputedFlags.intrinsicOf(a).applyTo(f)
            res.bit(3) shouldBe a.bit(3)
            res.bit(5) shouldBe a.bit(5)
            res.bit(6) shouldBe a.isZero()
            res.bit(7) shouldBe a.bit(7)
        }
    }

    test("precomputed ADD/ADC for 8-bit") {
        checkAll(OctetArb, OctetArb, OctetArb) { a, b, f -> 
            val c = a + b
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
        checkAll(WordArb, WordArb, OctetArb) { a, b, f -> 
            val c = a + b
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
        checkAll(OctetArb, OctetArb, OctetArb) { a, b, f -> 
            val c = a - b
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
        checkAll(OctetArb, OctetArb) { a, f -> 
            val b = Octet(1)
            val c = a + b
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
        checkAll(OctetArb, OctetArb) { a, f -> 
            val b = Octet(1)
            val c = a - b
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
        checkAll(OctetArb, OctetArb, Arb.boolean()) { c, f, carry -> 
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

private fun setOnCarry(a: Octet, b: Octet, mask: Int): Matcher<Boolean> = setOnCarry(a.toInt(), b.toInt(), mask)

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

private fun setOnBorrow(a: Octet, b: Octet, mask: Int): Matcher<Boolean> = setOnBorrow(a.toInt(), b.toInt(), mask)

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

private fun setOnOverflow(a: Octet, b: Octet): Matcher<Boolean> = setOnOverflow(a.toInt(), b.toInt())

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

private fun setOnUnderflow(a: Octet, b: Octet): Matcher<Boolean> = setOnUnderflow(a.toInt(), b.toInt())
