package vm8.cpu.z80

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.uByte
import io.kotest.property.checkAll
import vm8.data.*

fun Arb.Companion.bcd() = Arb.uByte().filter { it.isBCD() }

suspend fun behavesLike(f: suspend ProcessorBehavior.(flags: UByte) -> Unit) {
    checkAll<UByte> { flags ->
        val b = ProcessorBehavior()
        b.cpu.regs.f = flags
        b.f(flags)
    }
}

suspend inline fun<reified T> behavesLike(crossinline f: suspend ProcessorBehavior.(a: T, flags: UByte) -> Unit) {
    checkAll<T, UByte> { a, flags ->
        val b = ProcessorBehavior()
        b.cpu.regs.f = flags
        b.f(a, flags)
    }
}

suspend inline fun<reified T1, reified T2> behavesLike(crossinline f: suspend ProcessorBehavior.(a: T1, b: T2, flags: UByte) -> Unit) {
    checkAll<T1, T2, UByte> { a, b, flags ->
        val behavior = ProcessorBehavior()
        behavior.cpu.regs.f = flags
        behavior.f(a, b, flags)
    }
}

suspend inline fun<reified T1, reified T2> behavesLike(arbA: Arb<T1>, arbB: Arb<T2>, crossinline f: suspend ProcessorBehavior.(a: T1, b: T2, flags: UByte) -> Unit) {
    checkAll(arbA, arbB, Arb.uByte()) { a, b, flags ->
        val behavior = ProcessorBehavior()
        behavior.cpu.regs.f = flags
        behavior.f(a, b, flags)
    }
}

class ProcessorBehavior {
    val sys = MinimalSystem()
    val cpu = Processor(sys)

    val regs by cpu::regs
    val bus by cpu::bus
    val mem by sys::memory

    suspend fun whenProcessorRuns() {
        cpu.run()
    }

    suspend fun whenProcessorRuns(org: UShort = 0x0000u, f: Assembler.() -> Unit) {
        sys.memory.asm(org, f)
        cpu.run()
    }

    fun given(f: ProcessorBehavior.() -> Unit) {
        this.f()
    }

    fun given(
        i: UByte? = null,
        im: IntMode? = null,
        int: Boolean? = null,
        intAckData: UByte? = null,
        pc: UShort? = null,
        sp: UShort? = null,
    ) {
        if (i != null) { regs.i = i }
        if (im != null) { cpu.im = im }
        if (int != null) { cpu.int = int }
        if (intAckData != null) { sys.nextIntAck = intAckData }
        if (pc != null) { regs.pc = pc }
        if (sp != null) { regs.sp = sp }
    }

    fun givenCodeAt(org: UShort = 0x0000u, code: Assembler.() -> Unit) {
        mem.asm(org, code)
    }

    suspend fun expect(cycles: Int? = null, pc: Addr? = null, flags: UByte? = null, f: suspend ProcessorBehavior.() -> Unit = {}) {
        if (cycles != null)
            cpu.cycles shouldBe cycles.toLong()
        if (pc != null)
            cpu.regs.pc shouldBe pc
        if (flags != null)
            cpu.regs.f shouldBe flags
        this.f()
    }

    suspend fun expectRotate(expected: UByte, carry: Boolean, flags: UByte) = expect(cycles = 4, pc = 0x0001u) {
        regs.a shouldBe expected

        regs.f.bit(0) shouldBe carry
        regs.f.bit(1) shouldBe false
        regs.f.bit(2) shouldBe flags.bit(2)
        regs.f.bit(3) shouldBe expected.bit(3)
        regs.f.bit(4) shouldBe false
        regs.f.bit(5) shouldBe expected.bit(5)
        regs.f.bit(6) shouldBe flags.bit(6)
        regs.f.bit(7) shouldBe flags.bit(7)
    }

    data class Condition(val description: String, val eval: () -> Boolean)

    fun overflow(a: UByte, b: UByte) = overflow(a.toInt(), b.toInt())

    private fun overflow(a: Int, c: Int) = Condition("Overflow from $a to $c") {
        val b = c - a
        ((a xor b xor 0x80) and (b xor c) and 0x80) != 0
    }

    fun underflow(a: UByte, b: UByte) = underflow(a.toInt(), b.toInt())

    private fun underflow(a: Int, c: Int) = Condition("underflow from $a to $c") {
        val b = a - c
        ((a xor b) and ((a xor c) and 0x80)) != 0
    }

    fun carry(a: UByte, c: UByte) = carry(a.toInt(), c.toInt(), 0xFF)

    fun halfCarry(a: UByte, c: UByte) = carry(a.toInt(), c.toInt(), 0x0F)

    fun carry(a: UShort, c: UShort) = carry(a.toInt(), c.toInt(), 0xFFFF)

    fun halfCarry(a: UShort, c: UShort) = carry(a.toInt(), c.toInt(), 0x0FFF)

    private fun carry(a: Int, c: Int, mask: Int) = Condition("carry from $a to $c respect mask $mask") {
        (a and mask) > (c and mask)
    }

    fun borrow(a: UByte, c: UByte) = borrow(a.toInt(), c.toInt(), 0xFF)

    fun halfBorrow(a: UByte, c: UByte) = borrow(a.toInt(), c.toInt(), 0x0F)

    private fun borrow(a: Int, c: Int, mask: Int) = Condition("borrow from $a to $c respect mask $mask") {
        (a and mask) < (c and mask)
    }

    fun isZero(v: UByte) = Condition("value ${v.toHexString()} is zero") { v.isZero() }

    fun isNegative(v: UByte) = Condition("value ${v.toHexString()} is negative") { v.isNegative() }

    fun hasEvenParity(v: UByte) = Condition("value ${v.toHexString()} has even parity") { v.parity() }

    fun areNotEqual(a: UByte, b: UByte) = Condition(
        "value ${a.toHexString()} and ${b.toHexString()} are not equal"
    ) {
        a != b
    }

    suspend fun expectFlags(matcherFn: suspend (Flag) -> Matcher<UByte>) {
        Flag.values().forEach { flag ->
                regs.f shouldBe matcherFn(flag)
        }
    }

    fun flagIsSet(flag: Flag) = object : Matcher<UByte> {
        override fun test(value: UByte) = MatcherResult(
            flag.isSet(value),
            failureMessageFn = { "expected flag $flag to be set in ${value.toBinString()}" },
            negatedFailureMessageFn = { "expected flag $flag to be reset in ${value.toBinString()}" },
        )
    }

    fun flagIsReset(flag: Flag) = flagIsSet(flag).invert()

    fun flagCopiedFrom(flag: Flag, v: UByte, copiedFromFlag: Flag = flag) = object : Matcher<UByte> {
        override fun test(value: UByte) = MatcherResult(
            flag.isSet(value) == copiedFromFlag.isSet(v),
            failureMessageFn = { "expected flag $flag to be copied from $copiedFromFlag in ${v.toBinString()}" },
            negatedFailureMessageFn = { "expected flag $flag not to be copied from $copiedFromFlag in ${v.toBinString()}" },
        )
    }

    fun flagNotCopiedFrom(flag: Flag, v: UByte) = flagCopiedFrom(flag, v).invert()

    fun flagIsSetOn(flag: Flag, cond: Condition) = object : Matcher<UByte> {
        override fun test(value: UByte) = MatcherResult(
            flag.isSet(value) == cond.eval(),
            failureMessageFn = { "expected flag $flag to be set when ${cond.description}" },
            negatedFailureMessageFn = { "expected flag $flag to be reset when ${cond.description}" },
        )
    }

    fun whenFlagIsSetThen(flag: Flag, cond: Condition) = object : Matcher<UByte> {
        override fun test(value: UByte): MatcherResult {
            val passed = if (flag.isSet(value)) cond.eval() else true
            return MatcherResult(
                passed,
                failureMessageFn = { "expecting ${cond.description} to happen when flag $flag is set" },
                negatedFailureMessageFn = { "expecting ${cond.description} not to happen when flag $flag is set" },
            )
        }
    }
}