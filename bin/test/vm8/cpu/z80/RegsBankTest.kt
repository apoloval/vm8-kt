package vm8.cpu.z80

import kotlin.system.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.*

import vm8.cpu.z80.*
import vm8.data.*

internal class ProcessorTest {

    val mem = ByteArray(64*1024)
    val bus: Bus = object : Bus {
        override suspend fun read(addr: Addr): Octet = Octet(mem[addr.toInt()].toInt()) 
        override suspend fun write(addr: Addr, v: Octet) { mem[addr.toInt()] = v.toByte() }
    }
    val cpu = Processor(bus)

    @Test
    fun testExecNop() {
        mem[0x0000] = OPCODE_NOP.toByte()
        val cycles = runBlocking { cpu.run() }

        assertEquals(4, cycles)
        assertEquals(Word(0x0001), cpu.regs.pc)
    }

    @Test
    fun testExecInc8() {
        mem[0x0000] = OPCODE_INC_B.toByte()
        val cycles = runBlocking { cpu.run() }

        assertEquals(4, cycles)
        assertEquals(Word(0x0001), cpu.regs.pc)
        assertEquals(Octet(0x01), cpu.regs.b)
    }

    @Test
    fun testExecDec8() {
        mem[0x0000] = OPCODE_DEC_C.toByte()
        val cycles = runBlocking { cpu.run() }

        assertEquals(4, cycles)
        assertEquals(Word(0x0001), cpu.regs.pc)
        assertEquals(Octet(0xFF), cpu.regs.c)
    }

    @Test
    fun testExecJp() {
        mem[0x0000] = OPCODE_JP_NN.toByte()
        mem[0x0001] = 0x34.toByte()
        mem[0x0002] = 0x12.toByte()
        val cycles = runBlocking { cpu.run() }

        assertEquals(10, cycles)
        assertEquals(Word(0x1234), cpu.regs.pc)
    }
}
