package vm8.cpu.z80

import vm8.byteorder.*
import vm8.data.*

class Assembler(size: Int = 64*1024) {
    val buffer = ByteArray(size)
    var pointer: Int = 0
    val symbols: MutableMap<String, Int> = mutableMapOf()

    val A = Reg8.A
    val B = Reg8.B
    val C = Reg8.C
    val D = Reg8.D
    val E = Reg8.E
    val H = Reg8.H
    val L = Reg8.L

    val NOP: Unit get() = DB(OpCodes.NOP)

    operator fun String.unaryPlus(): Int = symbols.getValue(this)

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

    fun JP(addr: Int) {
        DB(OpCodes.`JP NN`)
        DW(addr)
    }
}

fun asm(size: Int = 64*1024, f: Assembler.() -> Unit): ByteArray {
    val asm = Assembler(size)
    asm.f()
    return asm.buffer
}
