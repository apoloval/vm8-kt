package vm8

import kotlin.system.*
import kotlinx.coroutines.*

import vm8.cpu.z80.*
import vm8.mem.*

const val TOTAL_INST = 250_000_000

fun main() {
    val mem = ByteArray(64*1024) {
        when(it) {
            0x00 -> OPCODE_NOP.toByte()
            0x01 -> OPCODE_INC_B.toByte()
            0x02 -> OPCODE_DEC_C.toByte()
            0x03 -> OPCODE_JP_NN.toByte()
            0x04 -> 0x00
            0x05 -> 0x00
            else -> 0x00
        }
    }

    val bus = object : vm8.cpu.z80.Bus {
        override suspend fun read(addr: Addr): Byte = mem[addr.toInt()]
        override suspend fun write(addr: Addr, v: Byte) { mem[addr.toInt()] = v }
    }
    val cpu = Processor(bus)

    var cycles: Long = 0
    val elapsed = measureTimeMillis {
        runBlocking {
            launch {
                repeat (TOTAL_INST) {
                    cycles += cpu.run()
                }
            }
        }
    }
    println("Executed ${cycles / 1000} Mcycles in $elapsed ms: ${cycles / (1000* elapsed)} Mhz")
}
