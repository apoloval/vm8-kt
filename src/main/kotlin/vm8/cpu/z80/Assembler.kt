package vm8.cpu.z80

import vm8.byteorder.ByteOrder

@Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
@OptIn(ExperimentalUnsignedTypes::class)
class Assembler(private val buffer: ByteArray, org: Int = 0) {
    var pointer: Int = org
    val symbols: MutableMap<String, Int> = mutableMapOf()

    val A = Reg8.A
    val B = Reg8.B
    val C = Reg8.C

    val AF = Reg16.AF
    val `AF'` = Reg16.`AF'`
    val BC = Reg16.BC
    val DE = Reg16.DE
    val HL = Reg16.HL

    operator fun String.unaryPlus(): Int = symbols.getValue(this)

    operator fun Reg16.not() = Ind8(this)

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

    fun ADD(dst: Reg16, src: Reg16) = when(Pair(dst, src)) {
        Pair(Reg16.HL, Reg16.BC) -> DB(OpCodes.`ADD HL, BC`)
        else -> throw IllegalArgumentException("invalid instruction: ADD $dst, $src")
    }

    fun DEC(r: Reg8) = when(r) {
        Reg8.A -> DB(OpCodes.`DEC A`)
        Reg8.B -> DB(OpCodes.`DEC B`)
        Reg8.C -> DB(OpCodes.`DEC C`)
        Reg8.D -> DB(OpCodes.`DEC D`)
        Reg8.E -> DB(OpCodes.`DEC E`)
        Reg8.H -> DB(OpCodes.`DEC H`)
        Reg8.L -> DB(OpCodes.`DEC L`)
        else -> throw IllegalArgumentException("invalid instruction: DEC $r")
    }

    fun DEC(r: Reg16) = when(r) {
        Reg16.BC -> DB(OpCodes.`DEC BC`)
        else -> throw IllegalArgumentException("invalid instruction: DEC $r")
    }

    fun DJNZ(v: Int) {
        DB(OpCodes.`DJNZ N`)
        DB(v)
    }

    fun EX(a: Reg16, b: Reg16) = when(Pair(a, b)) {
        Pair(Reg16.AF, Reg16.`AF'`) -> DB(OpCodes.`EX AF, AF'`)
        else -> throw IllegalArgumentException("invalid instruction: EX $a, $b")
    }

    fun INC(r: Reg8) = when(r) {
        Reg8.A -> DB(OpCodes.`INC A`)
        Reg8.B -> DB(OpCodes.`INC B`)
        Reg8.C -> DB(OpCodes.`INC C`)
        Reg8.D -> DB(OpCodes.`INC D`)
        Reg8.E -> DB(OpCodes.`INC E`)
        Reg8.H -> DB(OpCodes.`INC H`)
        Reg8.L -> DB(OpCodes.`INC L`)
        else -> throw IllegalArgumentException("invalid instruction: INC $r")
    }

    fun INC(r: Reg16) = when(r) {
        Reg16.BC -> DB(OpCodes.`INC BC`)
        else -> throw IllegalArgumentException("invalid instruction: INC $r")
    }

    fun LD(dst: Reg8, src: UByte) {
        when(dst) {
            Reg8.B -> { DB(OpCodes.`LD B, N`); DB(src) }
            Reg8.C -> { DB(OpCodes.`LD C, N`); DB(src) }
            else -> throw IllegalArgumentException("invalid instruction: LD $dst, $src")
        }
    }

    fun LD(r: Reg16, v: UShort) {
        when(r) {
            Reg16.BC -> DB(OpCodes.`LD BC, NN`)
            Reg16.DE -> DB(OpCodes.`LD DE, NN`)
            else -> throw IllegalArgumentException("invalid instruction: LD $r")
        }
        DW(v)
    }

    fun LD(dst: Reg8, src: Ind8) {
        when(Pair(dst, src)) {
            Pair(Reg8.A, Ind8(Reg16.BC)) -> DB(OpCodes.`LD A, (BC)`)
            else -> throw IllegalArgumentException("invalid instruction: LD $dst, $src")
        }
    }

    fun LD(dst: Ind8, src: Reg8) {
        when(Pair(dst, src)) {
            Pair(Ind8(Reg16.BC), Reg8.A) -> DB(OpCodes.`LD (BC), A`)
            Pair(Ind8(Reg16.DE), Reg8.A) -> DB(OpCodes.`LD (DE), A`)
            else -> throw IllegalArgumentException("invalid instruction: LD $dst, $src")
        }
    }

    fun JP(addr: Int) {
        DB(OpCodes.`JP NN`)
        DW(addr)
    }

    val NOP: Unit get() = DB(OpCodes.NOP)

    val RLCA: Unit get() { DB(OpCodes.RLCA) }

    val RRCA: Unit get() { DB(OpCodes.RRCA) }
}

fun ByteArray.asm(org: Int = 0, f: Assembler.() -> Unit): ByteArray {
    val asm = Assembler(this, org)
    asm.f()
    return this
}
