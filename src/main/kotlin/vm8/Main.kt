package vm8

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import vm8.cpu.z80.Addr
import vm8.cpu.z80.Assembler.*
import vm8.cpu.z80.Processor
import vm8.cpu.z80.asm
import kotlin.system.measureTimeMillis

const val TOTAL_INST = 250_000_000

fun main() {
    val mem = ByteArray(64*1024)
    mem.asm {
        LABEL("begin")
        NOP
        INC(B)
        DEC(C)
        RLCA
        EX(AF, `AF'`)
        JP(+"begin")
    }

    val bus = object : vm8.cpu.z80.Bus {
        override suspend fun read(addr: Addr): UByte = mem[addr.toInt()].toUByte()
        override suspend fun write(addr: Addr, v: UByte) { mem[addr.toInt()] = v.toByte() }
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
