package vm8.mem

import vm8.Bus

class MemoryBank(val size: Int){
    val bytes = ByteArray(size)
    init {
        require(size > 0) { "Memory bank size must be positive, but $size given"}
    }

    fun read(addr: Int): Byte = bytes.get(addr)
}
