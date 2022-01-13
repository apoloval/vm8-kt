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
 * ADD instruction for 8-bit operands.
 */
data class Add8(val dst: DestOp8, val src: SrcOp8, val withCarry: Boolean, val cycles: Int, val size: UByte) : Inst {
    override suspend fun Processor.exec(): Int {
        val a = load8(dst)
        var b = load8(src)
        if (withCarry && Flag.C.isSet(regs.f)) {
            b++
        }
        val c = (a + b).toUByte()
        store8(dst, c)
        apply(PrecomputedFlags.ofAdd(a, b))
        regs.pc = regs.pc.increment(size)
        return cycles
    }
}

/**
 * ADD instruction for 16-bit operands.
 */
data class Add16(val dst: DestOp16, val src: SrcOp16, val cycles: Int, val size: UByte) : Inst {
    override suspend fun Processor.exec(): Int {
        val a = load16(dst)
        val b = load16(src)
        val c = (a + b).toUShort()
        store16(dst, c)
        apply(PrecomputedFlags.ofAdd(a, b))
        regs.pc = regs.pc.increment(size)
        return cycles
    }
}

/**
 * AND instruction for 8-bit operands.
 */
data class And8(val dst: DestOp8, val src: SrcOp8, val cycles: Int, val size: UByte) : Inst {
    override suspend fun Processor.exec(): Int {
        val a = load8(dst)
        val b = load8(src)
        val c = (a and b)
        store8(dst, c)
        apply(PrecomputedFlags.ofAnd(c))
        regs.pc = regs.pc.increment(size)
        return cycles
    }
}

/**
 * CCF instruction.
 */
data class Ccf(val cycles: Int, val size: UByte) : Inst {
    override suspend fun Processor.exec(): Int {
        apply(
            (Flag.C on (Flag.C.isReset(regs.f))) and
            (Flag.H on (Flag.C.isSet(regs.f))) and
            (Flag.F3 on regs.a.bit(3)) and
            (Flag.F5 on regs.a.bit(5)) - Flag.N
        )
        regs.pc = regs.pc.increment(size)
        return cycles
    }
}

/**
 * CP instruction for 8-bit operands.
 */
data class Cp8(val dst: DestOp8, val src: SrcOp8, val cycles: Int, val size: UByte) : Inst {
    override suspend fun Processor.exec(): Int {
        val a = load8(dst)
        val b = load8(src)
        apply(PrecomputedFlags.ofCp(a, b))
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
        return if (c.isZero()) {
            regs.pc = regs.pc.increment(size)
            njcycles
        } else {
            regs.pc = regs.pc.increment(load8(relj).toByte())
            jcycles
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
 * HALT instruction
 */
object Halt : Inst {
    override suspend fun Processor.exec(): Int {
        return 4
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
data class Jp(val pred: FlagsPredicate, val addr: SrcOp16, val cycles: Int, val size: UByte) : Inst {
    override suspend fun Processor.exec(): Int {
        regs.pc = if (pred.evaluate(regs.f)) {
            load16(addr)
        } else {
            regs.pc.increment(size)
        }
        return cycles
    }
}

/**
 * JR instruction
 */
data class Jr(val pred: FlagsPredicate, val relj: SrcOp8, val jcycles: Int, val njcycles: Int, val size: UByte) : Inst {
    override suspend fun Processor.exec(): Int {
        return if (pred.evaluate(regs.f)) {
            regs.pc = regs.pc.increment(load8(relj).toByte())
            jcycles
        } else {
            regs.pc = regs.pc.increment(size)
            njcycles
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
 * OR instruction for 8-bit operands.
 */
data class Or8(val dst: DestOp8, val src: SrcOp8, val cycles: Int, val size: UByte) : Inst {
    override suspend fun Processor.exec(): Int {
        val a = load8(dst)
        val b = load8(src)
        val c = (a or b)
        store8(dst, c)
        apply(PrecomputedFlags.ofOr(c))
        regs.pc = regs.pc.increment(size)
        return cycles
    }
}

/**
 * OUT instruction.
 */
data class Out(val port: SrcOp8, val src: SrcOp8, val cycles: Int, val size: UByte) : Inst {
    override suspend fun Processor.exec(): Int {
        bus.ioWriteByte(load8(port), load8(src))
        regs.pc = regs.pc.increment(size)
        return cycles
    }
}

/**
 * POP instruction.
 */
data class Pop(val dst: DestOp16, val cycles: Int, val size: UByte) : Inst {
    override suspend fun Processor.exec(): Int {
        val word = bus.memReadWord(regs.sp)
        store16(dst, word)
        regs.sp = regs.sp.increment(2u)
        regs.pc = regs.pc.increment(size)
        return cycles
    }
}

/**
 * RET instruction.
 */
data class Ret(val pred: FlagsPredicate, val jcycles: Int, val njcycles: Int, val size: UByte) : Inst {
    override suspend fun Processor.exec(): Int {
        return if (pred.evaluate(regs.f)) {
            regs.pc = bus.memReadWord(regs.sp)
            regs.sp = regs.sp.increment(2u)
            jcycles
        } else {
            regs.pc = regs.pc.increment(size)
            njcycles
        }
    }
}

/**
 * RLA instruction.
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
 * SCF instruction.
 */
data class Scf(val cycles: Int, val size: UByte) : Inst {
    override suspend fun Processor.exec(): Int {
        apply(
            (Flag.F3 on regs.a.bit(3)) and
            (Flag.F5 on regs.a.bit(5)) + Flag.C - Flag.N - Flag.H
        )
        regs.pc = regs.pc.increment(size)
        return cycles
    }
}

/**
 * SUB instruction for 8-bit operands.
 */
data class Sub8(val dst: DestOp8, val src: SrcOp8, val withCarry: Boolean, val cycles: Int, val size: UByte) : Inst {
    override suspend fun Processor.exec(): Int {
        val a = load8(dst)
        var b = load8(src)
        if (withCarry && Flag.C.isSet(regs.f)) {
            b++
        }
        val c = (a - b).toUByte()
        store8(dst, c)
        apply(PrecomputedFlags.ofSub(a, b))
        regs.pc = regs.pc.increment(size)
        return cycles
    }
}

/**
 * XOR instruction for 8-bit operands.
 */
data class Xor8(val dst: DestOp8, val src: SrcOp8, val cycles: Int, val size: UByte) : Inst {
    override suspend fun Processor.exec(): Int {
        val a = load8(dst)
        val b = load8(src)
        val c = (a xor b)
        store8(dst, c)
        apply(PrecomputedFlags.ofXor(c))
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
