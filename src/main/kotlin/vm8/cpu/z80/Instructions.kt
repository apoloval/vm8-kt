package vm8.cpu.z80

import vm8.data.increment
import vm8.data.isZero
import vm8.data.rotateLeft
import vm8.data.rotateRight

/**
 * An instruction that can be executed by a Z80 processor.
 */
sealed interface Inst {
    /**
     * Execute the instruction over a [Processor].
     * 
     * @return the number of clock cycles required to execute.
     */
    suspend fun Processor.exec(): Int
}

/**
 * Run the given instruction over the Z80 processor.
 */
suspend fun Processor.run(inst: Inst): Int = with(inst) { exec() }

/**
 * ADD instruction for 16-bit operands.
 */
data class Add16(val dst: DestOp16, val src: SrcOp16, val cycles: Int, val size: UByte) : Inst {
    override suspend fun Processor.exec(): Int {
        val a = load16(src)
        val b = load16(dst)
        val c = (a + b).toUShort()
        store16(dst, c)
        apply(PrecomputedFlags.ofAdd(a, b))
        regs.pc = regs.pc.increment(size)
        return cycles
    }
}

/**
 * DEC instruction for 8-bit operands
 */
data class Dec8(val dest: DestOp8, val cycles: Int, val size: UByte) : Inst {
    override suspend fun Processor.exec(): Int {
        val a = load8(dest)
        val c = a.dec()
        store8(dest, c)
        apply(PrecomputedFlags.ofDec(a))
        regs.pc = regs.pc.increment(size)
        return cycles
    }
}

/**
 * DEC instruction for 16-bit operands
 */
data class Dec16(val dst: DestOp16, val cycles: Int, val size: UByte) : Inst {
    override suspend fun Processor.exec(): Int {
        val a = load16(dst)
        val c = a.dec()
        store16(dst, c)
        regs.pc = regs.pc.increment(size)
        return cycles
    }
}

/**
 * EX instruction
 */
data class Ex(val a: DestOp16, val b: DestOp16, val cycles: Int, val size: UByte) : Inst {
    override suspend fun Processor.exec(): Int {
        var va = load16(a)
        var vb = load16(b)
        store16(a, vb)
        store16(b, va)
        regs.pc = regs.pc.increment(size)
        return cycles
    }
}

/**
 * INC instruction for 8-bit operands
 */
data class Inc8(val dest: DestOp8, val cycles: Int, val size: UByte) : Inst {
    override suspend fun Processor.exec(): Int {
        val a = load8(dest)
        val c = a.inc()
        store8(dest, c)
        apply(PrecomputedFlags.ofInc(a))
        regs.pc = regs.pc.increment(size)
        return cycles
    }
}

/**
 * INC instruction for 16-bit operands
 */
data class Inc16(val dest: DestOp16, val cycles: Int, val size: UByte) : Inst {
    override suspend fun Processor.exec(): Int {
        var v = load16(dest)
        v++
        store16(dest, v)
        regs.pc = regs.pc.increment(size)
        return cycles
    }
}

/**
 * JP instruction.
 */
data class Jp(val addr: SrcOp16) : Inst {
    override suspend fun Processor.exec(): Int {
        regs.pc = load16(addr)
        return 10
    }
}

/**
 * LOAD instruction for 8-bit operands
 */
data class Ld8(val dest: DestOp8, val src: SrcOp8, val cycles: Int, val size: UByte): Inst {
    override suspend fun Processor.exec(): Int {
        val v = load8(src)
        store8(dest, v)
        regs.pc = regs.pc.increment(size)
        return cycles
    }
}

/**
 * LOAD instruction for 16-bit operands
 */
data class Ld16(val dest: DestOp16, val src: SrcOp16, val cycles: Int, val size: UByte): Inst {
    override suspend fun Processor.exec(): Int {
        val v = load16(src)
        store16(dest, v)
        regs.pc = regs.pc.increment(size)
        return cycles
    }
}

/**
 * NOP instruction
 */
object Nop : Inst {
    override suspend fun Processor.exec(): Int {
        regs.pc++
        return 4
    }
}

/**
 * RLCA instruction
 */
data class Rlca(val cycles: Int, val size: UByte) : Inst {
    override suspend fun Processor.exec(): Int {
        val (v, carry) = regs.a.rotateLeft()
        regs.a = v
        apply(PrecomputedFlags.ofRotateA(v, carry))        
        regs.pc = regs.pc.increment(size)
        return cycles
    }
}

/**
 * RRCA instruction
 */
data class Rrca(val cycles: Int, val size: UByte) : Inst {
    override suspend fun Processor.exec(): Int {
        val (v, carry) = regs.a.rotateRight()
        regs.a = v
        apply(PrecomputedFlags.ofRotateA(v, carry))        
        regs.pc = regs.pc.increment(size)
        return cycles
    }
}

/**
 * Illegal pseudo-instruction to refer to an illegal opcode.
 */
object Illegal : Inst {
    override suspend fun Processor.exec(): Int = throw Exception("illegal opcode")
}
