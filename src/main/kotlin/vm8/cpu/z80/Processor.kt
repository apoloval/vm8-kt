package vm8.cpu.z80

class Processor(val bus: Bus) {
    val regs: RegsBank = RegsBank()

    fun reset() {
        regs.pc = 0x0000u
    }

    suspend fun run(): Int = decode().run { exec() }
}
