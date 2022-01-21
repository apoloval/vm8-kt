package vm8

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import vm8.cpu.z80.Assembler.*
import vm8.cpu.z80.MinimalSystem
import vm8.cpu.z80.Processor
import vm8.cpu.z80.asm
import java.text.NumberFormat
import kotlin.system.measureTimeMillis

const val TOTAL_INST = 300_000_000

fun main() {
    val sys = MinimalSystem()
    sys.memory.asm {
        LABEL("begin")
        NOP
        INC(B)
        DEC(C)
        RLCA
        EX(AF, `AF'`)
        JP(+"begin")
    }

    val cpu = Processor(sys)

    val elapsed = measureTimeMillis {
        runBlocking {
            launch {
                repeat (TOTAL_INST) {
                    cpu.run()
                }
            }
        }
    }
    println("Executed ${NumberFormat.getInstance().format(cpu.cycles)} cycles in $elapsed ms: ${cpu.cycles / (1000* elapsed)} Mhz")
}
