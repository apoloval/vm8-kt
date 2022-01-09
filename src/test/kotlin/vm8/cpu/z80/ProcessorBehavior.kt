package vm8.cpu.z80

import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import vm8.data.bit

suspend fun behavesLike(f: suspend ProcessorBehavior.(flags: UByte) -> Unit) {
    checkAll<UByte> { flags ->
        val b = ProcessorBehavior()
        b.cpu.regs.f = flags
        b.f(flags)
    }
}

suspend inline fun<reified T> behavesLike(crossinline f: suspend ProcessorBehavior.(a: T, flags: UByte) -> Unit) {
    checkAll<T, UByte> { a, flags ->
        val b = ProcessorBehavior()
        b.cpu.regs.f = flags
        b.f(a, flags)
    }
}

suspend inline fun<reified T1, reified T2> behavesLike(crossinline f: suspend ProcessorBehavior.(a: T1, b: T2, flags: UByte) -> Unit) {
    checkAll<T1, T2, UByte> { a, b, flags ->
        val behav = ProcessorBehavior()
        behav.cpu.regs.f = flags
        behav.f(a, b, flags)
    }
}

class ProcessorBehavior {
    private val sys = MinimalSystem()
    val cpu = Processor(sys)

    val regs by cpu::regs
    val bus by cpu::bus
    val mem by sys::memory

    private var givenCycles: Int = 0

    suspend fun whenProcessorRuns() {
        givenCycles = cpu.run()
    }

    suspend fun whenProcessorRuns(org: Int = 0x0000, f: Assembler.() -> Unit) {
        sys.memory.asm(org, f)
        givenCycles = cpu.run()
    }

    fun given(f: ProcessorBehavior.() -> Unit) {
        this.f()
    }

    suspend fun expect(cycles: Int? = null, pc: Addr? = null, flags: UByte? = null, f: suspend ProcessorBehavior.() -> Unit = {}) {
        if (cycles != null)
            givenCycles shouldBe cycles
        if (pc != null)
            cpu.regs.pc shouldBe pc
        if (flags != null)
            cpu.regs.f shouldBe flags
        this.f()
    }

    suspend fun expectRotate(xval: UByte, carry: Boolean, flags: UByte) = expect(cycles = 4, pc = 0x0001u) {
        regs.a shouldBe xval

        regs.f.bit(0) shouldBe carry
        regs.f.bit(1) shouldBe false
        regs.f.bit(2) shouldBe flags.bit(2)
        regs.f.bit(3) shouldBe xval.bit(3)
        regs.f.bit(4) shouldBe false
        regs.f.bit(5) shouldBe xval.bit(5)
        regs.f.bit(6) shouldBe flags.bit(6)
        regs.f.bit(7) shouldBe flags.bit(7)
    }

    fun flagActiveOnOverflow(a: Int, c: Int): Boolean {
        val b = c - a
        return ((a xor b xor 0x80) and (b xor c) and 0x80) != 0
    }

    fun flagActiveOnOverflow(a: UByte, b: UByte) = flagActiveOnOverflow(a.toInt(), b.toInt())

    fun flagActiveOnUnderflow(a: Int, c: Int): Boolean {
        val b = a - c
        return ((a xor b) and ((a xor c) and 0x80)) != 0
    }

    fun flagActiveOnUnderflow(a: UByte, b: UByte) = flagActiveOnUnderflow(a.toInt(), b.toInt())

    fun flagActiveOnCarry(a: Int, c: Int, mask: Int): Boolean = (a and mask) > (c and mask)

    fun flagActiveOnCarry(a: UByte, c: UByte, mask: Int): Boolean = flagActiveOnCarry(a.toInt(), c.toInt(), mask)

    fun flagActiveOnCarry(a: UShort, c: UShort, mask: Int): Boolean = flagActiveOnCarry(a.toInt(), c.toInt(), mask)

    fun flagActiveOnBorrow(a: Int, c: Int, mask: Int): Boolean = (a and mask) < (c and mask)

    fun flagActiveOnBorrow(a: UByte, c: UByte, mask: Int): Boolean = flagActiveOnBorrow(a.toInt(), c.toInt(), mask)
}