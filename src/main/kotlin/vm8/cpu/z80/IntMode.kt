package vm8.cpu.z80

import vm8.byteorder.ByteOrder

enum class IntMode {
    Zero {
        override suspend fun Processor.accept() {
            intEnabled = false
            val opCode = bus.intAck()
            decode(opCode).run { exec() }
            cycles += 2
        }
    },
    One {
        override suspend fun Processor.accept() {
            intEnabled = false
            bus.intAck()
            call(0x0038u)
            cycles += 13
        }
    },
    Two {
        override suspend fun Processor.accept() {
            intEnabled = false
            val v = bus.intAck()
            val addr = ByteOrder.LITTLE_ENDIAN.decode(v, regs.i)
            call(addr)
            cycles += 19
        }
    };

    abstract suspend fun Processor.accept()
}