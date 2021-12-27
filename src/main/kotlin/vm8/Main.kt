package vm8

import kotlin.system.*
import kotlinx.coroutines.*

import vm8.cpu.z80.*
import vm8.mem.*

const val TOTAL_INST = 100_000_000

fun main() {
    val mem = MemoryBank(64*1024)
    val cpu = Processor()
    val bus = object : vm8.cpu.z80.Bus {
        override suspend fun read(addr: UShort): Byte = mem.read(addr.toInt())
    }
    val elapsed = measureTimeMillis {
        runBlocking {
            launch {
                repeat (TOTAL_INST) {
                    cpu.run(bus)
                }
            }
        }
    }
    println("Executed $TOTAL_INST in $elapsed millis")
}
