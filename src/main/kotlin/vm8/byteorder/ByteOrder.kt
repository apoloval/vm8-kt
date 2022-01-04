package vm8.byteorder

enum class ByteOrder(private val nioOrder: java.nio.ByteOrder) {
    BIG_ENDIAN(java.nio.ByteOrder.BIG_ENDIAN) {
        override fun encode(w: UShort): Pair<UByte, UByte> {
            val v0 = (w.toInt() ushr 8) and 0xFF
            val v1 = (w.toInt() ushr 0) and 0xFF
            return Pair(v0.toUByte(), v1.toUByte())
        }

        override fun decode(b0: UByte, b1: UByte): UShort {
            val v0 = ((b1.toInt() and 0xFF) shl 0)
            val v1 = ((b0.toInt() and 0xFF) shl 8)
            return (v0 or v1).toUShort()
        }
    },
    LITTLE_ENDIAN(java.nio.ByteOrder.LITTLE_ENDIAN) {
        override fun encode(w: UShort): Pair<UByte, UByte> {
            val v0 = (w.toInt() ushr 0) and 0xFF
            val v1 = (w.toInt() ushr 8) and 0xFF
            return Pair(v0.toUByte(), v1.toUByte())
        }

        override fun decode(b0: UByte, b1: UByte): UShort {
            val v0 = ((b0.toInt() and 0xFF) shl 0)
            val v1 = ((b1.toInt() and 0xFF) shl 8)
            return (v0 or v1).toUShort()
        }
    };

    abstract fun encode(w: UShort): Pair<UByte, UByte>
    abstract fun decode(b0: UByte, b1: UByte): UShort

    fun isNative(): Boolean = (this === ByteOrder.Companion.nativeOrder())

    companion object {
        private val native: ByteOrder = orderOf(java.nio.ByteOrder.nativeOrder())

        fun of(nioOrder: java.nio.ByteOrder): ByteOrder = orderOf(nioOrder)

        fun nativeOrder(): ByteOrder = native
    }
}

private fun orderOf(nioOrder: java.nio.ByteOrder): ByteOrder =
    if (nioOrder === java.nio.ByteOrder.BIG_ENDIAN) ByteOrder.BIG_ENDIAN else ByteOrder.LITTLE_ENDIAN