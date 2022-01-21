package vm8.cpu.z80

import vm8.data.increment

class Processor(val bus: Bus, fn: Processor.() -> Unit = {}) {
    val regs: RegsBank = RegsBank()

    /**
     * A counter of the T cycles the processor has executed instructions.
     */
    var cycles: Long = 0
        internal set

    /**
     * A logic signal to request a non-maskable interrupt (NMI) to the CPU.
     *
     * Any device that have to interrupt the CPU will have to activate this logic signal.
     * This will be eventually read by the CPU, and it will accept the interrupt
     * accordingly. This will not be reset by the CPU in any way. The peripherals that
     * drive this signal have to deactivate it when they do not want to interrupt any longer.
     */
    var nmi = false
        set(value) {
            if (!field && value) {
                // On active flank, the NMI flip-flop is set
                regs.nmiff = true
            }
            field = value
        }

    /**
     * A logic signal to request a maskable interrupt (INT) to the CPU.
     *
     * Any device that have to interrupt the CPU will have to activate this logic signal.
     * This will be eventually read by the CPU, and it will accept (or not) the interrupt
     * accordingly. This will not be reset by the CPU in any way. The peripherals that
     * drive this signal have to deactivate it when they do not want to interrupt any longer.
     */
    var int = false
        set(value) {
            regs.intff = true
            field = value
        }


    internal var intEnabled: Boolean
        get() = regs.iff1 && !regs.eiff
        set(value) {
            regs.iff1 = value
            regs.iff2 = value
            if (value) {
                regs.eiff = true
            }
        }

    private var resetReq = false

    internal var im = IntMode.Zero

    init {
        reset()
        fn(this)
    }

    suspend fun run() {
        processReset()
        if (processNmiRequest()) {
            return
        }
        if (processIntRequest()) {
            return
        }
        decode().run { exec() }
    }

    /**
     * Request the CPU to be reset.
     *
     * This is different from [reset] in the sense that it will not perform an immediate reset
     * of the internal state of the CPU, but it will request it to reset before the next instruction
     * is executed. It means a coroutine can be looping over [run] function and being reset using
     * this function.
     */
    fun requestReset() {
        resetReq = true
    }

    /**
     * Request a maskable interrupt to the CPU.
     */
    fun requestInt() { int = true}

    /**
     * Reset the internal state of the CPU.
     *
     * Do not call this method if the CPU is running (i.e. a coroutine is looping over [run]). Use
     * [requestReset] instead.
     */
    fun reset() {
        regs.af = 0xffffu
        regs.pc = 0x0000u
        regs.sp = 0xffffu
        regs.iff1 = false
        regs.iff2 = false
        regs.nmiff = false
        regs.intff = int
    }

    /**
     * Reset the cycles counter to zero.
     */
    fun resetCycles() {
        cycles = 0
    }

    private fun processReset() {
        if (resetReq) {
            reset()
            resetReq = false
        }
    }

    private suspend fun processNmiRequest(): Boolean {
        if (regs.nmiff) {
            regs.iff1 = false
            regs.sp = regs.sp.increment(-2)
            bus.memWriteWord(regs.sp, regs.pc)
            regs.pc = 0x0066u
            regs.nmiff = false
            cycles += 11
            return true
        }
        return false
    }

    private suspend fun processIntRequest(): Boolean {
        if (regs.intff && intEnabled) {
            im.run() { accept() }
            return true
        }
        regs.eiff = false
        return false
    }

    internal suspend fun call(addr: UShort) {
        regs.sp = regs.sp.increment(-2)
        bus.memWriteWord(regs.sp, regs.pc)
        regs.pc = addr
    }
}
