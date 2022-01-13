package vm8.cpu.z80

import vm8.data.*

/**
 * A flag of the Z80 processor
 */
enum class Flag(val mask: UByte) {
    S (0b10000000u),
    Z (0b01000000u),
    F5(0b00100000u),
    H (0b00010000u),
    F3(0b00001000u),
    PV(0b00000100u),
    //P (0b00000100u),
    //V (0b00000100u),
    N (0b00000010u),
    C (0b00000001u);

    /**
     * Check if the flag is set in the given value.
     */
    fun isSet(v: UByte): Boolean = v and mask > 0u

    /**
     * Check if the flag is reset in the given value.
     */
    fun isReset(v: UByte) = !isSet(v)

    /**
     * Convert this flag into a [FlagsAffection] by requesting this flag to be set
     */
    operator fun unaryPlus(): FlagsAffection = FlagsAffection() + this

    /**
     * Convert this flag into a [FlagsAffection] by requesting this flag to be clear
     */
    operator fun unaryMinus(): FlagsAffection = FlagsAffection() - this

    /**
     * Convert this flag into a [FlagsAffection] by requesting this flag to be set/clear depending on a condition.
     */
    infix fun on(b: Boolean): FlagsAffection = if (b) +this else -this

    companion object {
        val P = PV
        val V = PV
    }
}

/**
 * Describes a predicate on register flags.
 */
enum class FlagsPredicate {
    ALWAYS {  override fun evaluate(flags: UByte) = true },
    Z { override fun evaluate(flags: UByte) = Flag.Z.isSet(flags) },
    NZ { override fun evaluate(flags: UByte) = Flag.Z.isReset(flags) },
    C { override fun evaluate(flags: UByte) = Flag.C.isSet(flags) },
    NC { override fun evaluate(flags: UByte) = Flag.C.isReset(flags) },
    PE { override fun evaluate(flags: UByte) = Flag.P.isSet(flags) },
    PO { override fun evaluate(flags: UByte) = Flag.P.isReset(flags) },
    P { override fun evaluate(flags: UByte) = Flag.S.isSet(flags) },
    M { override fun evaluate(flags: UByte) = Flag.S.isReset(flags) },
    ;

    abstract fun evaluate(flags: UByte): Boolean
}

/**
 * An accumulation of flag affections
 */
class FlagsAffection(val set: UByte = 0u, val clear: UByte = 0u) {
    override fun toString(): String =
        "[set:${Integer.toBinaryString(set.toInt())}, clr:${Integer.toBinaryString(clear.toInt())}]"

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
     * Apply this flag affections to the given UByte.
     */
    fun applyTo(v: UByte): UByte = v.bitSet(set).bitClear(clear)
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
inline fun Processor.updateFlags(fn: (UByte) -> UByte) {
    regs.f = fn(regs.f)
}

/**
 * Check if the given flag is active.
 */
fun Processor.isFlag(flag: Flag): Boolean = regs.f and flag.mask > 0u

/**
 * Precomputed flags for 8-bit arithmetic.
 */
object PrecomputedFlags {
    private val intrinsic: Array<FlagsAffection> = Array(256) { it.toUByte().run {
        (Flag.S on isNegative()) and
        (Flag.Z on isZero()) and
        (Flag.F3 on bit(3)) and
        (Flag.F5 on bit(5))
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

    // CP(a, b) flags
    private val cp = Array(256) { a ->
        Array(256) { b ->
            val c = (a - b) and 0xFF
            intrinsic[c] + Flag.N and
                (Flag.H on borrowNibble(a, c)) and
                (Flag.V on underflow(a, b, c)) and
                (Flag.C on borrowByte(a, c)) and
                (Flag.F3 on b.toUByte().bit(3)) and
                (Flag.F5 on b.toUByte().bit(5))
        }
    }

    // INC(a) flags are ADD(a, 1) flags but C is not affected
    private val inc = Array(256) { add8[it][1] * Flag.C }

    // DEC(a) flags are SUB(a, 1) flags but C is not affected
    private val dec = Array(256) { sub[it][1] * Flag.C }

    // AND(a, b) flags
    private val and8 = Array(256) { c ->
        intrinsic[c] - Flag.C - Flag.N + Flag.H and
                (Flag.P on c.toUByte().parity())
    }

    // OR/XOR(a, b) flags
    private val or8 = Array(256) { c ->
        intrinsic[c] - Flag.C - Flag.N - Flag.H and
                (Flag.P on c.toUByte().parity())
    }

    // RLCA/RLA/RRCA/RRA intrinsic flags
    private val rotA = Array(256) { it.toUByte().run {
        (Flag.F5 on bit(5)) and (Flag.F3 on bit(3)) - Flag.H - Flag.N
    }}

    /**
     * Get the intrinsic flags of the given UByte.
     * 
     * Intrinsic flags are those that are obtained from the ALU result, such as S (sign), Z (zero),
     * F3 and F5 (copy of bits 3 and 5, respectively). 
     */
    fun intrinsicOf(v: UByte): FlagsAffection = intrinsic[v.toInt()]

    /**
     * Get the flags resulting from adding two UBytes.
     */
    fun ofAdd(a: UByte, b: UByte): FlagsAffection = add8[a.toInt()][b.toInt()]

    /**
     * Get the flags resulting from adding two UShorts.
     */
    fun ofAdd(a: UShort, b: UShort): FlagsAffection {
        // This is not actually pre-computed. But...         
        val c = (a + b).toUShort()
        return (Flag.F5 on c.high().bit(5)) and
            (Flag.H on carry(a.toInt(), c.toInt(), 0x0FFF)) and
            (Flag.F3 on c.high().bit(3)) - Flag.N and
            (Flag.C on carryUShort(a.toInt(), c.toInt()))
    }

    /**
     * Get the flags resulting from an AND operation.
     */
    fun ofAnd(c: UByte): FlagsAffection = and8[c.toInt()]

    /**
     * Get the flags resulting from an XOR operation.
     */
    fun ofXor(c: UByte): FlagsAffection = or8[c.toInt()]

    /**
     * Get the flags resulting from an OR operation.
     */
    fun ofOr(c: UByte): FlagsAffection = or8[c.toInt()]

    /**
     * Get the flags resulting from subtracting two UBytes.
     */
    fun ofSub(a: UByte, b: UByte): FlagsAffection = sub[a.toInt()][b.toInt()]

    /**
     * Get the flags resulting from comparing two UBytes.
     */
    fun ofCp(a: UByte, b: UByte): FlagsAffection = cp[a.toInt()][b.toInt()]

    /**
     * Get the flags resulting from incrementing an UByte.
     */
    fun ofInc(a: UByte): FlagsAffection = inc[a.toInt()]

    /**
     * Get the flags resulting from decrementing an UByte.
     */
    fun ofDec(a: UByte): FlagsAffection = dec[a.toInt()]

    /**
     * Get the flags resulting from a rotation of A register
     * 
     * This function receives the result as argument. Take this into account while calling.
     */
    fun ofRotateA(c: UByte, carry: Boolean): FlagsAffection = rotA[c.toInt()] and (Flag.C on carry)

    private fun carryNibble(a: Int, c: Int): Boolean = carry(a, c, 0x0F)
    
    private fun borrowNibble(a: Int, c: Int): Boolean = borrow(a, c, 0x0F)

    private fun carryByte(a: Int, c: Int): Boolean = carry(a, c, 0xFF)

    private fun borrowByte(a: Int, c: Int): Boolean = borrow(a, c, 0xFF)

    private fun carryUShort(a: Int, c: Int): Boolean = carry(a, c, 0xFFFF)

    private fun borrowUShort(a: Int, c: Int): Boolean = borrow(a, c, 0xFFFF)

    private fun carry(a: Int, c: Int, mask: Int) = (a and mask) > (c and mask)

    private fun borrow(a: Int, c: Int, mask: Int) = (a and mask) < (c and mask)

    private fun overflow(a: Int, b: Int, c: Int): Boolean = ((a xor b xor 0x80) and (b xor c) and 0x80) != 0

    private fun underflow(a: Int, b: Int, c: Int): Boolean = ((a xor b) and ((a xor c) and 0x80)) != 0
}
