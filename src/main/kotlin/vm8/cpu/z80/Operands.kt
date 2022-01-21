package vm8.cpu.z80

sealed interface SrcOp8 {
    suspend fun Processor.get(): UByte
}

sealed interface DestOp8 : SrcOp8 {
    suspend fun Processor.set(v: UByte)
}

inline suspend fun Processor.load8(op: SrcOp8): UByte = with(op) { get() }

inline suspend fun Processor.store8(op: DestOp8, v: UByte) { with(op) { set(v) } }

enum class Reg8 : DestOp8 {
    A {
        override suspend fun Processor.get(): UByte = regs.a
        override suspend fun Processor.set(v: UByte) { regs.a = v }
    }, 
    F {
        override suspend fun Processor.get(): UByte = regs.f
        override suspend fun Processor.set(v: UByte) { regs.f = v }
    }, 
    B {
        override suspend fun Processor.get(): UByte = regs.b
        override suspend fun Processor.set(v: UByte) { regs.b = v }
    }, 
    C {
        override suspend fun Processor.get(): UByte = regs.c
        override suspend fun Processor.set(v: UByte) { regs.c = v }
    },
    D {
        override suspend fun Processor.get(): UByte = regs.d
        override suspend fun Processor.set(v: UByte) { regs.d = v }
    },    
    E {
        override suspend fun Processor.get(): UByte = regs.e
        override suspend fun Processor.set(v: UByte) { regs.e = v }
    },    
    H {
        override suspend fun Processor.get(): UByte = regs.h
        override suspend fun Processor.set(v: UByte) { regs.h = v }
    },    
    L {
        override suspend fun Processor.get(): UByte = regs.l
        override suspend fun Processor.set(v: UByte) { regs.l = v }
    },    
}

object Imm8 : SrcOp8 {
    override suspend fun Processor.get(): UByte = bus.memReadByte(regs.pc.inc())
}

data class Ind8(val addr: SrcOp16) : DestOp8 {
    override suspend fun Processor.get(): UByte = bus.memReadByte(load16(addr))

    override suspend fun Processor.set(v: UByte) { bus.memWriteByte(load16(addr), v) }
}

sealed interface SrcOp16 {
    suspend fun Processor.get(): UShort
}

sealed interface DestOp16 : SrcOp16 {
    suspend fun Processor.set(v: UShort)
}

inline suspend fun Processor.load16(op: SrcOp16): UShort = with(op) { get() }

inline suspend fun Processor.store16(op: DestOp16, v: UShort) { with(op) { set(v) } }

inline suspend fun Processor.swap16(a: DestOp16, b: DestOp16) {
    val va = load16(a)
    val vb = load16(b)
    store16(a, vb)
    store16(b, va)
}

enum class Reg16 : DestOp16 {
    AF {
        override suspend fun Processor.get(): UShort = regs.af
        override suspend fun Processor.set(v: UShort) { regs.af = v }
    }, 
    BC {
        override suspend fun Processor.get(): UShort = regs.bc
        override suspend fun Processor.set(v: UShort) { regs.bc = v }
    }, 
    DE {
        override suspend fun Processor.get(): UShort = regs.de
        override suspend fun Processor.set(v: UShort) { regs.de = v }
    }, 
    HL {
        override suspend fun Processor.get(): UShort = regs.hl
        override suspend fun Processor.set(v: UShort) { regs.hl = v }
    }, 
    PC {
        override suspend fun Processor.get(): UShort = regs.pc
        override suspend fun Processor.set(v: UShort) { regs.pc = v }
    },
    SP {
        override suspend fun Processor.get(): UShort = regs.sp
        override suspend fun Processor.set(v: UShort) { regs.sp = v }
    },
    `AF'` {
        override suspend fun Processor.get(): UShort = regs.`af'`
        override suspend fun Processor.set(v: UShort) { regs.`af'` = v }
    },
    `BC'` {
        override suspend fun Processor.get(): UShort = regs.`bc'`
        override suspend fun Processor.set(v: UShort) { regs.`bc'` = v }
    },
    `DE'` {
        override suspend fun Processor.get(): UShort = regs.`de'`
        override suspend fun Processor.set(v: UShort) { regs.`de'` = v }
    },
    `HL'` {
        override suspend fun Processor.get(): UShort = regs.`hl'`
        override suspend fun Processor.set(v: UShort) { regs.`hl'` = v }
    },
}

object Imm16 : SrcOp16 {
    override suspend fun Processor.get(): UShort = bus.memReadWord(regs.pc.inc())
}

data class Ind16(val addr: SrcOp16) : DestOp16 {
    override suspend fun Processor.get(): UShort = bus.memReadWord(load16(addr))

    override suspend fun Processor.set(v: UShort) { bus.memWriteWord(load16(addr), v) }
}
