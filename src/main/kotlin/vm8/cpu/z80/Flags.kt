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
     */
    infix fun and(other: FlagsAffection): FlagsAffection = FlagsAffection(set or other.set, clear or other.clear)

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

    // ADD/ADC(a, b) flags
    private val add = Array(256) { a -> 
        Array(256) { b ->
            val c = a + b
            intrinsic[c and 0xFF] - Flag.N and 
                (Flag.H on halfCarry(a, c)) and 
                (Flag.V on overflow(a, b, c)) and
                (Flag.C on carry(a, c))
        }
    }

    // SUB/SBC(a, b) flags
    private val sub = Array(256) { a -> 
        Array(256) { b ->
            val c = a - b
            intrinsic[c and 0xFF] + Flag.N and 
                (Flag.H on halfBorrow(a, c)) and 
                (Flag.V on underflow(a, b, c)) and
                (Flag.C on borrow(a, c))
        }
    }

    // INC(a) flags are ADD(a, 1) flags but C is not affected
    private val inc = Array(256) { add[it][1] * Flag.C }

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
    fun ofAdd(a: Octet, b: Octet): FlagsAffection = add[a.toInt()][b.toInt()]

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

    private fun halfCarry(a: Int, c: Int): Boolean = (a and 0x0F) > (c and 0x0F)
    
    private fun halfBorrow(a: Int, c: Int): Boolean = (a and 0x0F) < (c and 0x0F)

    private fun overflow(a: Int, b: Int, c: Int): Boolean = ((a xor b xor 0x80) and (b xor c) and 0x80) != 0

    private fun underflow(a: Int, b: Int, c: Int): Boolean = ((a xor b) and ((a xor c) and 0x80)) != 0

    private fun carry(a: Int, c: Int): Boolean = (c and 0xFF) < (a and 0xFF)

    private fun borrow(a: Int, c: Int): Boolean = (c and 0xFF) > (a and 0xFF)
}
