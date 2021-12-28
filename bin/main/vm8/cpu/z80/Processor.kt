package vm8.cpu.z80

import vm8.byteorder.*

class Processor(val bus: Bus) {
    val regs: RegsBank = RegsBank()

    suspend fun run(): Int {
        val inst = decode() 
        return exec(inst)
    }    

    inline suspend fun load8(op: SrcOp8): Byte = when(op) {
        is Reg8 -> regs.load8(op)
        is MemOp8 -> bus.read(op.addr)
    }

    inline suspend fun store8(op: DestOp8, data: Byte) = when(op) {
        is Reg8 -> regs.store8(op, data)
        is MemOp8 -> bus.write(op.addr, data)
    }

    inline suspend fun load16(op: SrcOp16): Short =  when (op) {
        Imm16 -> bus.readWord(regs.pc.inc().toUShort())
        // is MemOp16 -> bus.readWord(op.addr)
        else -> TODO()
    }
    
    inline suspend fun unary8(op: DestOp8, f: (Byte) -> Byte) = 
        load8(op).let(f).also { store8(op, it) }
}
