package vm8.cpu.z80

import vm8.data.*

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
 * CPL instruction.
 */
data class Cpl(val cycles: Int, val size: UByte) : Inst {
    override suspend fun Processor.exec(): Int {
        regs.a = regs.a.inv()
        apply(
            (Flag.F3 on regs.a.bit(3)) and
            (Flag.F5 on regs.a.bit(5)) + Flag.N + Flag.H
        )
        regs.pc = regs.pc.increment(size)
        return cycles
    }
}

/**
 * DAA instruction.
 */
data class Daa(val cycles: Int, val size: UByte) : Inst {
    override suspend fun Processor.exec(): Int {
        var hasHalfCarry = false
        var hasCarry = false
        if (Flag.N.isReset(regs.f)) {
            if (regs.a.low() > 9u || Flag.H.isSet(regs.f)) {
                regs.a = regs.a.increment(0x06)
                hasHalfCarry = true
            }
            if (regs.a.high() > 9u || Flag.C.isSet(regs.f)) {
                regs.a = regs.a.increment(0x60)
                hasCarry = true
            }
        } else {
            if (regs.a.low() > 9u || Flag.H.isSet(regs.f)) {
                regs.a = regs.a.increment(-0x06)
                hasHalfCarry = true
            }
            if (regs.a.high() > 9u || Flag.C.isSet(regs.f)) {
                regs.a = regs.a.increment(-0x60)
                hasCarry = true
            }
        }

        apply(
            (Flag.C on hasCarry) and
            (Flag.P on regs.a.parity()) and
            (Flag.F3 on regs.a.bit(3)) and
            (Flag.H on hasHalfCarry) and
            (Flag.F5 on regs.a.bit(5)) and
            (Flag.Z on regs.a.isZero()) and
            (Flag.S on regs.a.isNegative())
        )

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
 * DJNZ instruction.
 */
data class Djnz(val dst: DestOp8, val relj: SrcOp8, val jcycles: Int, val njcycles: Int, val size: UByte) : Inst {
    override suspend fun Processor.exec(): Int {
        val a = load8(dst)
        val c = a.dec()
        store8(dst, c)
        if (c.isZero()) {
            regs.pc = regs.pc.increment(size)
            return njcycles
        } else {
            regs.pc = regs.pc.increment(load8(relj).toByte())
            return jcycles
        }
    }
}

/**
 * EX instruction
 */
data class Ex(val a: DestOp16, val b: DestOp16, val cycles: Int, val size: UByte) : Inst {
    override suspend fun Processor.exec(): Int {
        val va = load16(a)
        val vb = load16(b)
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
 * Describes the condition for a jump action to take place.
 */
enum class JumpCond {
    ALWAYS {  override fun matches(flags: UByte) = true },
    Z { override fun matches(flags: UByte) = Flag.Z.isSet(flags) },
    NZ { override fun matches(flags: UByte) = Flag.Z.isReset(flags) };

    abstract fun matches(flags: UByte): Boolean;
}

/**
 * JR instruction
 */
data class Jr(val cond: JumpCond, val relj: SrcOp8, val jcycles: Int, val njcycles: Int, val size: UByte) : Inst {
    override suspend fun Processor.exec(): Int {
        if (cond.matches(regs.f)) {
            regs.pc = regs.pc.increment(load8(relj).toByte())
            return jcycles
        } else {
            regs.pc = regs.pc.increment(size)
            return njcycles
        }
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
 * RLA instruction
 */
data class Rla(val cycles: Int, val size: UByte) : Inst {
    override suspend fun Processor.exec(): Int {
        val (v, carry) = regs.a.rotateLeft(isFlag(Flag.C))
        regs.a = v
        apply(PrecomputedFlags.ofRotateA(v, carry))        
        regs.pc = regs.pc.increment(size)
        return cycles
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
 * RRA instruction
 */
data class Rra(val cycles: Int, val size: UByte) : Inst {
    override suspend fun Processor.exec(): Int {
        val (v, carry) = regs.a.rotateRight(isFlag(Flag.C))
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
