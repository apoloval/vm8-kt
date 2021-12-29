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
 * An accumulation of flag effections
 */
class FlagsAffection(val set: Int = 0, val clear: Int = 0) {
    operator fun plus(f: Flag): FlagsAffection = FlagsAffection(set or f.mask, clear)

    operator fun minus(f: Flag): FlagsAffection = FlagsAffection(set, clear or f.mask)    

    infix fun and(other: FlagsAffection): FlagsAffection = FlagsAffection(set or other.set, clear or other.clear)
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
 * Return true if bit 5 of this octet is set
 */
fun Octet.isBit5(): Boolean = areBitsSet(Flag.F5.mask)

/**
 * Return true if bit 3 of this octet is set
 */
fun Octet.isBit3(): Boolean = areBitsSet(Flag.F3.mask)

/**
 * Flags instrinsic to this octet.
 */
fun Octet.flags(): FlagsAffection = 
    (Flag.S on isNegative()) and 
    (Flag.Z on isZero()) and
    (Flag.F5 on isBit5()) and
    (Flag.F3 on isBit3()) and
    (Flag.P on parity())

/**
 * Precomputed intrinsic flags for all octets.
 */
val IntrinsicFlags: Array<FlagsAffection> = Array(256) { Octet(it).flags() }

/**
 * Get the intrinsic flags of the given octet.
 */
fun Array<FlagsAffection>.of(v: Octet): FlagsAffection = this[v.toInt()]
