package vm8.cpu.z80

import vm8.byteorder.*
import vm8.data.*

class Assembler(private val buffer: ByteArray, org: Int = 0) {
    var pointer: Int = org
    val symbols: MutableMap<String, Int> = mutableMapOf()

    val A = Reg8.A
    val B = Reg8.B
    val C = Reg8.C
    val D = Reg8.D
    val E = Reg8.E
    val H = Reg8.H
    val L = Reg8.L

    val AF = Reg16.AF
    val `AF'` = Reg16.`AF'`
    val BC = Reg16.BC
    val HL = Reg16.HL

    operator fun String.unaryPlus(): Int = symbols.getValue(this)

    operator fun Reg16.not() = Ind8(this)

    fun LABEL(name: String) = symbols.put(name, pointer)

    fun DB(vararg bytes: Int) {
        for (b in bytes) {
            buffer[pointer++] = b.toByte()
        }
    }

    fun DW(vararg bytes: Int) {
        for (b in bytes) {
            val (v0, v1) = ByteOrder.LITTLE_ENDIAN.encode(Word(b))
            buffer[pointer++] = v0.toByte()
            buffer[pointer++] = v1.toByte()
        }
    }

    fun DW(vararg labels: String) {
        for (l in labels) {
            DW(symbols.getValue(l))
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

    fun LD(r: Reg16, v: Int) {
        when(r) {
            Reg16.BC -> DB(OpCodes.`LD BC, NN`)
            else -> throw IllegalArgumentException("invalid instruction: LD $r")
        }
        DW(v)
    }

    fun LD(dst: Reg8, src: Int) {
        when(dst) {
            Reg8.B -> { DB(OpCodes.`LD B, N`); DB(src) }
            Reg8.C -> { DB(OpCodes.`LD C, N`); DB(src) }
            else -> throw IllegalArgumentException("invalid instruction: LD $dst, $src")
        }
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
            else -> throw IllegalArgumentException("invalid instruction: LD $dst, $src")
        }
    }

    fun JP(addr: Int) {
        DB(OpCodes.`JP NN`)
        DW(addr)
    }

    val NOP: Unit get() = DB(OpCodes.NOP)

    val RLCA: Unit get() { DB(OpCodes.RLCA) }
}

fun ByteArray.asm(org: Int = 0, f: Assembler.() -> Unit): ByteArray {
    val asm = Assembler(this, org)
    asm.f()
    return this
}
