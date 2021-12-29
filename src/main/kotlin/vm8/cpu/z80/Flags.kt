package vm8.cpu.z80

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
}

/**
 * An accumulation of flag effections
 */
class FlagsAffection(val set: Int = 0, val clear: Int = 0) {
    operator fun plus(f: Flag): FlagsAffection = FlagsAffection(set or f.mask, clear)

    operator fun minus(f: Flag): FlagsAffection = FlagsAffection(set, clear or f.mask)    
}

/**
 * Apply the given flags to the processor.
 */
fun Processor.apply(flags: FlagsAffection) {
    regs.updateFlags { it.bitSet(flags.set).bitClear(flags.clear) }
}