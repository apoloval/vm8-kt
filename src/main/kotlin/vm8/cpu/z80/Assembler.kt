package vm8.cpu.z80

import vm8.byteorder.ByteOrder

@Suppress("EXPERIMENTAL_IS_NOT_ENABLED", "UNUSED_PARAMETER")
@OptIn(ExperimentalUnsignedTypes::class)
class Assembler(private val buffer: ByteArray, org: Int = 0) {
    private var pointer: Int = org
    private val symbols: MutableMap<String, Int> = mutableMapOf()

    // Register names
    object A
    object B
    object C // can also be a flag condition
    object D
    object E
    object H
    object L
    object AF
    object `AF'`
    object BC { operator fun not() = `(BC)` }
    object DE { operator fun not() = `(DE)` }
    object HL { operator fun not() = `(HL)` }
    object SP

    // Indirect registers
    object `(BC)`
    object `(DE)`
    object `(HL)`

    // Jump conditions
    object NC
    object Z
    object NZ

    data class Indirect<T>(val expr: T)

    operator fun UShort.not() = Indirect(this)
    operator fun UInt.not() = this.toUShort().not()

    operator fun String.unaryPlus(): UShort = symbols.getValue(this).toUShort()

    fun LABEL(name: String) = symbols.put(name, pointer)

    fun DB(vararg bytes: Int) = DB(*bytes.map { it.toUByte() }.toUByteArray())

    fun DB(vararg bytes: UByte) {
        for (b in bytes) {
            buffer[pointer++] = b.toByte()
        }
    }

    fun DW(vararg bytes: Int) = DW(*bytes.map { it.toUShort() }.toUShortArray())

    fun DW(vararg bytes: UShort) {
        for (b in bytes) {
            val (v0, v1) = ByteOrder.LITTLE_ENDIAN.encode(b)
            buffer[pointer++] = v0.toByte()
            buffer[pointer++] = v1.toByte()
        }
    }

    fun ADD(dst: HL, src: BC) = DB(OpCodes.`ADD HL, BC`)
    fun ADD(dst: HL, src: DE) = DB(OpCodes.`ADD HL, DE`)
    fun ADD(dst: HL, src: HL) = DB(OpCodes.`ADD HL, HL`)
    fun ADD(dst: HL, src: SP) = DB(OpCodes.`ADD HL, SP`)

    val CCF: Unit get() = DB(OpCodes.CCF)

    val CPL: Unit get() = DB(OpCodes.CPL)

    val DAA: Unit get() = DB(OpCodes.DAA)

    fun DEC(dst: A) = DB(OpCodes.`DEC A`)
    fun DEC(dst: B) = DB(OpCodes.`DEC B`)
    fun DEC(dst: C) = DB(OpCodes.`DEC C`)
    fun DEC(dst: D) = DB(OpCodes.`DEC D`)
    fun DEC(dst: E) = DB(OpCodes.`DEC E`)
    fun DEC(dst: H) = DB(OpCodes.`DEC H`)
    fun DEC(dst: L) = DB(OpCodes.`DEC L`)
    fun DEC(dst: `(HL)`) = DB(OpCodes.`DEC (HL)`)

    fun DEC(dst: BC) = DB(OpCodes.`DEC BC`)
    fun DEC(dst: DE) = DB(OpCodes.`DEC DE`)
    fun DEC(dst: HL) = DB(OpCodes.`DEC HL`)
    fun DEC(dst: SP) = DB(OpCodes.`DEC SP`)

    fun DJNZ(n: Int) { DB(OpCodes.`DJNZ N`); DB(n) }

    fun EX(a: AF, b: `AF'`) = DB(OpCodes.`EX AF, AF'`)

    fun INC(dst: A) = DB(OpCodes.`INC A`)
    fun INC(dst: B) = DB(OpCodes.`INC B`)
    fun INC(dst: C) = DB(OpCodes.`INC C`)
    fun INC(dst: D) = DB(OpCodes.`INC D`)
    fun INC(dst: E) = DB(OpCodes.`INC E`)
    fun INC(dst: H) = DB(OpCodes.`INC H`)
    fun INC(dst: L) = DB(OpCodes.`INC L`)
    fun INC(dst: `(HL)`) = DB(OpCodes.`INC (HL)`)

    fun INC(dst: BC) = DB(OpCodes.`INC BC`)
    fun INC(dst: DE) = DB(OpCodes.`INC DE`)
    fun INC(dst: HL) = DB(OpCodes.`INC HL`)
    fun INC(dst: SP) = DB(OpCodes.`INC SP`)

    fun LD(dst: A, src: UByte) { DB(OpCodes.`LD A, N`); DB(src) }
    fun LD(dst: B, src: UByte) { DB(OpCodes.`LD B, N`); DB(src) }
    fun LD(dst: C, src: UByte) { DB(OpCodes.`LD C, N`); DB(src) }
    fun LD(dst: D, src: UByte) { DB(OpCodes.`LD D, N`); DB(src) }
    fun LD(dst: E, src: UByte) { DB(OpCodes.`LD E, N`); DB(src) }
    fun LD(dst: H, src: UByte) { DB(OpCodes.`LD H, N`); DB(src) }
    fun LD(dst: L, src: UByte) { DB(OpCodes.`LD L, N`); DB(src) }
    fun LD(dst: A, src: `(BC)`) { DB(OpCodes.`LD A, (BC)`) }
    fun LD(dst: A, src: `(DE)`) { DB(OpCodes.`LD A, (DE)`) }
    fun LD(dst: A, src: Indirect<UShort>) { DB(OpCodes.`LD A, (NN)`); DW(src.expr) }
    fun LD(dst: B, src: A) { DB(OpCodes.`LD B, A`) }
    fun LD(dst: B, src: B) { DB(OpCodes.`LD B, B`) }
    fun LD(dst: B, src: C) { DB(OpCodes.`LD B, C`) }
    fun LD(dst: B, src: D) { DB(OpCodes.`LD B, D`) }
    fun LD(dst: B, src: E) { DB(OpCodes.`LD B, E`) }
    fun LD(dst: B, src: H) { DB(OpCodes.`LD B, H`) }
    fun LD(dst: B, src: L) { DB(OpCodes.`LD B, L`) }
    fun LD(dst: B, src: `(HL)`) { DB(OpCodes.`LD B, (HL)`) }
    fun LD(dst: C, src: A) { DB(OpCodes.`LD C, A`) }
    fun LD(dst: C, src: B) { DB(OpCodes.`LD C, B`) }
    fun LD(dst: C, src: C) { DB(OpCodes.`LD C, C`) }
    fun LD(dst: C, src: D) { DB(OpCodes.`LD C, D`) }
    fun LD(dst: C, src: E) { DB(OpCodes.`LD C, E`) }
    fun LD(dst: C, src: H) { DB(OpCodes.`LD C, H`) }
    fun LD(dst: C, src: L) { DB(OpCodes.`LD C, L`) }
    fun LD(dst: C, src: `(HL)`) { DB(OpCodes.`LD C, (HL)`) }
    fun LD(dst: D, src: A) { DB(OpCodes.`LD D, A`) }
    fun LD(dst: D, src: B) { DB(OpCodes.`LD D, B`) }
    fun LD(dst: D, src: C) { DB(OpCodes.`LD D, C`) }
    fun LD(dst: D, src: D) { DB(OpCodes.`LD D, D`) }
    fun LD(dst: D, src: E) { DB(OpCodes.`LD D, E`) }
    fun LD(dst: D, src: H) { DB(OpCodes.`LD D, H`) }
    fun LD(dst: D, src: L) { DB(OpCodes.`LD D, L`) }
    fun LD(dst: D, src: `(HL)`) { DB(OpCodes.`LD D, (HL)`) }
    fun LD(dst: E, src: A) { DB(OpCodes.`LD E, A`) }
    fun LD(dst: E, src: B) { DB(OpCodes.`LD E, B`) }
    fun LD(dst: E, src: C) { DB(OpCodes.`LD E, C`) }
    fun LD(dst: E, src: D) { DB(OpCodes.`LD E, D`) }
    fun LD(dst: E, src: E) { DB(OpCodes.`LD E, E`) }
    fun LD(dst: E, src: H) { DB(OpCodes.`LD E, H`) }
    fun LD(dst: E, src: L) { DB(OpCodes.`LD E, L`) }
    fun LD(dst: E, src: `(HL)`) { DB(OpCodes.`LD E, (HL)`) }
    fun LD(dst: `(BC)`, src: A) { DB(OpCodes.`LD (BC), A`) }
    fun LD(dst: `(DE)`, src: A) { DB(OpCodes.`LD (DE), A`) }
    fun LD(dst: `(HL)`, src: UByte) { DB(OpCodes.`LD (HL), N`); DB(src) }

    fun LD(dst: Indirect<UShort>, src: A) { DB(OpCodes.`LD (NN), A`); DW(dst.expr) }
    fun LD(dst: BC, src: UShort) { DB(OpCodes.`LD BC, NN`); DW(src) }
    fun LD(dst: DE, src: UShort) { DB(OpCodes.`LD DE, NN`); DW(src) }
    fun LD(dst: HL, src: UShort) { DB(OpCodes.`LD HL, NN`); DW(src) }
    fun LD(dst: SP, src: UShort) { DB(OpCodes.`LD SP, NN`); DW(src) }
    fun LD(dst: Indirect<UShort>, src: HL) { DB(OpCodes.`LD (NN), HL`); DW(dst.expr) }
    fun LD(dst: HL, src: Indirect<UShort>) { DB(OpCodes.`LD HL, (NN)`); DW(src.expr) }

    fun JP(addr: UShort) { DB(OpCodes.`JP NN`); DW(addr) }

    fun JR(rel: Byte) { DB(OpCodes.`JR N`); DB(rel.toUByte()) }
    fun JR(cond: Z, n: Byte) { DB(OpCodes.`JR Z, N`); DB(n.toUByte()) }
    fun JR(cond: NZ, n: Byte) { DB(OpCodes.`JR NZ, N`); DB(n.toUByte()) }
    fun JR(cond: C, n: Byte) { DB(OpCodes.`JR C, N`); DB(n.toUByte()) }
    fun JR(cond: NC, n: Byte) { DB(OpCodes.`JR NC, N`); DB(n.toUByte()) }

    val NOP: Unit get() = DB(OpCodes.NOP)

    val RLA: Unit get() { DB(OpCodes.RLA) }

    val RLCA: Unit get() { DB(OpCodes.RLCA) }

    val RRA: Unit get() { DB(OpCodes.RRA) }

    val RRCA: Unit get() { DB(OpCodes.RRCA) }

    val SCF: Unit get() { DB(OpCodes.SCF) }
}

fun ByteArray.asm(org: Int = 0, f: Assembler.() -> Unit): ByteArray {
    val asm = Assembler(this, org)
    asm.f()
    return this
}
