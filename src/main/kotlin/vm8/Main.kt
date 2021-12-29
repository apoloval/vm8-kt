package vm8

import kotlin.system.*
import kotlinx.coroutines.*

import vm8.cpu.z80.*
import vm8.data.*

const val TOTAL_INST = 250_000_000

fun main() {
    val mem = ByteArray(64*1024) {
        when(it) {
            0x00 -> OpCodes.NOP.toByte()
            0x01 -> OpCodes.`INC B`.toByte()
            0x02 -> OpCodes.`DEC C`.toByte()
            0x03 -> OpCodes.`JP NN`.toByte()
            0x04 -> 0x00
            0x05 -> 0x00
            else -> 0x00
        }
    }

    val bus = object : vm8.cpu.z80.Bus {
        override suspend fun read(addr: Addr): Octet = Octet(mem[addr.toInt()].toInt())
        override suspend fun write(addr: Addr, v: Octet) { mem[addr.toInt()] = v.toByte() }
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
