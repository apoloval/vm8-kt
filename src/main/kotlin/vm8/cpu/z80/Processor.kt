package vm8.cpu.z80

import vm8.data.*
import vm8.byteorder.*

class Processor(val bus: Bus) {
    val regs: RegsBank = RegsBank()

    fun reset() {
        regs.pc = Word(0x0000)
    }

    suspend fun run(): Int = decode().run { exec() }
}
