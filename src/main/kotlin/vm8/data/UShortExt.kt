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

/**
 * Convert this [UShort] into a hexadecimal string
 */
fun UShort.toHexString(): String = String.format("0x%04X", this.toInt())

/**
 * Convert this [UShort] into a binary string.
 */
fun UShort.toBinString(): String = String.format(
    "0b%16s",
    Integer.toBinaryString(this.toInt()).replace(' ', '0'),
)