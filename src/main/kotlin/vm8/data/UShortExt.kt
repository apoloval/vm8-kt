package vm8.data

/**
 * Obtain the low (less significant) [UByte] of this [UShort] value.
 */
fun UShort.low(): UByte = this.toUByte()

/**
 * Obtain the high (most significant) [UByte] of this [UShort] value.
 */
fun UShort.high(): UByte = (this.toUInt() shr 8).toUByte()

/**
 * Obtain the result from setting the low (less significant) [UByte] of this [UShort] value.
 */
fun UShort.setLow(v: UByte): UShort = (this and 0xFF00u) or (v.toUShort())

/**
* Obtain the result from setting the high (most significant) [UByte] of this [UShort] value.
*/
fun UShort.setHigh(v: UByte): UShort = (this and 0x00FFu) or (v.toUInt() shl 8).toUShort()

/**
 * Obtain the increment of this [UShort] by the given value.
 */
fun UShort.increment(v: UByte): UShort =  (this + v).toUShort()

/**
 * Obtain the increment of this [UShort] by the given value.
 */
fun UShort.increment(v: Byte): UShort =  (this.toShort() + v).toUShort()