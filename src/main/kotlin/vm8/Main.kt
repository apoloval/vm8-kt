package vm8

import kotlin.system.*
import kotlinx.coroutines.*

import vm8.cpu.z80.*
import vm8.data.*

const val TOTAL_INST = 250_000_000

fun main() {
    val mem = asm {
        LABEL("begin")
        NOP
        INC(B)
        DEC(C)
        JP(+"begin")
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
