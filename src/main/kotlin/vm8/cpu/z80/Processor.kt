package vm8.cpu.z80

const val OPCODE_NOP: Byte = 0x00

interface Bus : vm8.Bus<UShort, Byte> {}

class Processor {
    var pc: UShort = 0x0000.toUShort()

    suspend fun run(bus: Bus) {
        when (bus.read(pc)) {
            OPCODE_NOP -> pc++
        }
    }
}
