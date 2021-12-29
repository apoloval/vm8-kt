package vm8.byteorder

import vm8.data.*

enum class ByteOrder(private val nioOrder: java.nio.ByteOrder) {
    BIG_ENDIAN(java.nio.ByteOrder.BIG_ENDIAN) {
        override fun decode(b0: Octet, b1: Octet): Word {
            val v0 = ((b1.toInt() and 0xFF) shl 0)
            val v1 = ((b0.toInt() and 0xFF) shl 8)
            return (v0 or v1).toWord()
        }
    },
    LITTLE_ENDIAN(java.nio.ByteOrder.LITTLE_ENDIAN) {
        override fun decode(b0: Octet, b1: Octet): Word {
            val v0 = ((b0.toInt() and 0xFF) shl 0)
            val v1 = ((b1.toInt() and 0xFF) shl 8)
            return (v0 or v1).toWord()
        }
    };

    abstract fun decode(b0: Octet, b1: Octet): Word

    fun isNative(): Boolean = (this === ByteOrder.Companion.nativeOrder())

    companion object {
        private val native: ByteOrder = orderOf(java.nio.ByteOrder.nativeOrder())

        public fun of(nioOrder: java.nio.ByteOrder): ByteOrder = orderOf(nioOrder)

        public fun nativeOrder(): ByteOrder = native
    }
}

private fun orderOf(nioOrder: java.nio.ByteOrder): ByteOrder =
    if (nioOrder === java.nio.ByteOrder.BIG_ENDIAN) ByteOrder.BIG_ENDIAN else ByteOrder.LITTLE_ENDIAN