package vm8.cpu.z80

import vm8.data.*

/**
 * A flag of the Z80 processor
 */
enum class Flag(val mask: Int) {
    S (0b10000000), 
    Z (0b01000000), 
    F5(0b00100000), 
    H (0b00010000), 
    F3(0b00001000), 
    PV(0b00000100), 
    P (0b00000100), 
    V (0b00000100), 
    N (0b00000010), 
    C (0b00000001);

    /**
     * Convert this flag into a [FlagAffection] by requesting this flag to be set
     */
    operator fun unaryPlus(): FlagsAffection = FlagsAffection() + this

    /**
     * Convert this flag into a [FlagAffection] by requesting this flag to be clear
     */
    operator fun unaryMinus(): FlagsAffection = FlagsAffection() - this

    /**
     * Convert this flag into a [FlagsAffection] by requesting this flag to be set/clear depending on a condition.
     */
    infix fun on(b: Boolean): FlagsAffection = if (b) +this else -this
}

/**
 * An accumulation of flag affections
 */
class FlagsAffection(val set: Int = 0, val clear: Int = 0) {
    override fun toString(): String = "[set:${Integer.toBinaryString(set)}, clr:${Integer.toBinaryString(clear)}]"

    /**
     * Indicate the given flag will be set
     */
    operator fun plus(f: Flag): FlagsAffection = FlagsAffection(set or f.mask, clear and f.mask.inv())

    /**
     * Indicate the given flag will be clear
     */
    operator fun minus(f: Flag): FlagsAffection = FlagsAffection(set and f.mask.inv(), clear or f.mask)

    /**
     * Indicate the given flag will be unaffected
     */
    operator fun times(f: Flag): FlagsAffection = FlagsAffection(set and f.mask.inv(), clear and f.mask.inv())

    /**
     * Combine this flags affection with others.
     * 
     * The affections of the right hand have higher priority. This means, if lhs says set/clear for a flag
     * and rhs also affects that flag, rhs affection prevails. 
     */
    infix fun and(other: FlagsAffection): FlagsAffection = FlagsAffection(
        other.set or (set and other.clear.inv()),   // consider only lhs sets that are not clear in rhs
        other.clear or (clear and other.set.inv()), // consider only lhs clears that are not set in rhs
    )

    /**
     * Apply this flag affections to the given octet.
     */
    fun applyTo(v: Octet): Octet = v.bitSet(set).bitClear(clear)
}

/**
 * Apply the given flags to the processor.
 */
fun Processor.apply(flags: FlagsAffection) {
    updateFlags { it.bitSet(flags.set).bitClear(flags.clear) }
}

/**
 * Update the flags applying the given function.
 */
inline fun Processor.updateFlags(fn: (Octet) -> Octet) {
    regs.f = fn(regs.f)
}

/**
 * Check if the given flag is active.
 */
fun Processor.isFlag(flag: Flag): Boolean = regs.f.areBitsSet(flag.mask)

/**
 * Precomputed flags for 8-bit arithmetic.
 */
object PrecomputedFlags { 
    private val intrinsic: Array<FlagsAffection> = Array(256) { Octet(it).run {
        (Flag.S on isNegative()) and 
        (Flag.Z on isZero()) and
        (Flag.F5 on bit(5)) and
        (Flag.F3 on bit(3))
    }}

    // ADD/ADC(a, b) flags for 8-bit operands
    private val add8 = Array(256) { a -> 
        Array(256) { b ->
            val c = (a + b) and 0xFF
            intrinsic[c] - Flag.N and 
                (Flag.H on carryNibble(a, c)) and 
                (Flag.V on overflow(a, b, c)) and
                (Flag.C on carryByte(a, c))
        }
    }

    // SUB/SBC(a, b) flags
    private val sub = Array(256) { a -> 
        Array(256) { b ->
            val c = (a - b) and 0xFF
            intrinsic[c] + Flag.N and 
                (Flag.H on borrowNibble(a, c)) and 
                (Flag.V on underflow(a, b, c)) and
                (Flag.C on borrowByte(a, c))
        }
    }

    // INC(a) flags are ADD(a, 1) flags but C is not affected
    private val inc = Array(256) { add8[it][1] * Flag.C }

    // DEC(a) flags are SUB(a, 1) flags but C is not affected
    private val dec = Array(256) { sub[it][1] * Flag.C }

    // RLCA/RLA/RRCA/RRA intrinsic flags
    private val rotA = Array(256) { Octet(it).run {
        (Flag.F5 on bit(5)) and (Flag.F3 on bit(3)) - Flag.H - Flag.N
    }}

    /**
     * Get the intrinsic flags of the given octet.
     * 
     * Intrinsic flags are those that are obtained from the ALU result, such as S (sign), Z (zero),
     * F3 and F5 (copy of bits 3 and 5, respectively). 
     */
    fun intrinsicOf(v: Octet): FlagsAffection = intrinsic[v.toInt()]

    /**
     * Get the flags resulting from adding two octets.
     */
    fun ofAdd(a: Octet, b: Octet): FlagsAffection = add8[a.toInt()][b.toInt()]

    /**
     * Get the flags resulting from adding two words.
     */
    fun ofAdd(a: Word, b: Word): FlagsAffection {
        // This is not actually pre-computed. But...         
        val c = a + b
        return (Flag.F5 on c.high().bit(5)) and
            (Flag.H on carry(a.toInt(), c.toInt(), 0x0FFF)) and
            (Flag.F3 on c.high().bit(3)) - Flag.N and
            (Flag.C on carryWord(a.toInt(), c.toInt()))
    }

    /**
     * Get the flags resulting from subtracting two octets.
     */
    fun ofSub(a: Octet, b: Octet): FlagsAffection = sub[a.toInt()][b.toInt()]

    /**
     * Get the flags resulting from incrementing an octet.
     */
    fun ofInc(a: Octet): FlagsAffection = inc[a.toInt()]

    /**
     * Get the flags resulting from decrementing an octet.
     */
    fun ofDec(a: Octet): FlagsAffection = dec[a.toInt()]

    /**
     * Get the flags resulting from a rotation of A register
     * 
     * This function receives the result as argument. Take this into account while calling.
     */
    fun ofRotateA(c: Octet, carry: Boolean): FlagsAffection = rotA[c.toInt()] and (Flag.C on carry)

    private fun carryNibble(a: Int, c: Int): Boolean = carry(a, c, 0x0F)
    
    private fun borrowNibble(a: Int, c: Int): Boolean = borrow(a, c, 0x0F)

    private fun carryByte(a: Int, c: Int): Boolean = carry(a, c, 0xFF)

    private fun borrowByte(a: Int, c: Int): Boolean = borrow(a, c, 0xFF)

    private fun carryWord(a: Int, c: Int): Boolean = carry(a, c, 0xFFFF)

    private fun borrowWord(a: Int, c: Int): Boolean = borrow(a, c, 0xFFFF)

    private fun carry(a: Int, c: Int, mask: Int) = (a and mask) > (c and mask)

    private fun borrow(a: Int, c: Int, mask: Int) = (a and mask) < (c and mask)

    private fun overflow(a: Int, b: Int, c: Int): Boolean = ((a xor b xor 0x80) and (b xor c) and 0x80) != 0

    private fun underflow(a: Int, b: Int, c: Int): Boolean = ((a xor b) and ((a xor c) and 0x80)) != 0
}
