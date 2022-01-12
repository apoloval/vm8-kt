package vm8.data

/**
 * Obtain the low (less significant) nibble of this [UByte] value.
 */
fun UByte.low(): UByte = (this.toUInt() and 0x0Fu).toUByte()

/**
 * Obtain the high (most significant) nibble of this [UByte] value.
 */
fun UByte.high(): UByte = (this.toUInt() shr 4).toUByte()

/**
 * Check if this [UByte] has a valid BCD representation.
 */
fun UByte.isBCD(): Boolean = this.low() < 10u && this.high() < 10u

/**
 * Set (assign 1 to) the bits of this [UByte] indicated by the given mask value.
 *
 * If the mask indicates 0 for a bit, it will not be touched. If it indicates 1, the bit will be set.
 */
fun UByte.bitSet(mask: UByte): UByte = (this or mask)

/**
 * Clear (assign 0 to) the bits of this [UByte] indicated by the given mask value.
 *
 * If the mask indicates 0 for a bit, it will not be touched. If it indicates 1, the bit will be cleared.
 */
fun UByte.bitClear(mask: UByte): UByte = this and mask.inv()

/**
 * Check if this [UByte] is zero.
 */
fun UByte.isZero(): Boolean = this == 0u.toUByte()

/**
 * Check if this [UByte] is negative.
 */
fun UByte.isNegative(): Boolean = (this.toByte() < 0)

/**
 * Return the value of the n-th bit of this [UByte]
 */
fun UByte.bit(n: Int): Boolean = (this.toUInt() shr n) and 0x01u > 0u

/**
 * Return the bits of this [UByte] as an iterable sequence of booleans.
 *
 * The returned iterable starts with the less significant bit.
 */
fun UByte.bits(): Iterable<Boolean> = (0..7).map { bit(it) }

/**
 * Obtain the Rotation of this [UByte] one bit left, including its carry.
 *
 * The bit 7 is copied to bit 0, and also returned as carry.
 */
fun UByte.rotateLeft(): Pair<UByte, Boolean> = Pair(
    ((this.toUInt() shl 1) or (this.toUInt() shr 7)).toUByte(),
    this and 0x80u > 0u,
)

/**
 * Obtain the Rotation of this [UByte] and the carry input one bit left, including its carry.
 *
 * The input carry is copied to bit 0, and the bit 7 is returned as carry.
 */
fun UByte.rotateLeft(carry: Boolean): Pair<UByte, Boolean> = Pair(
    ((this.toUInt() shl 1) or (if (carry) 0x01u else 0x00u)).toUByte(),
    this and 0x80u > 0u,
)

/**
 * Obtain the Rotation of this [UByte] one bit right, including its carry.
 *
 * The bit 0 is copied to bit 7, and also returned as carry.
 */
fun UByte.rotateRight(): Pair<UByte, Boolean> = Pair(
    ((this.toUInt() shr 1) or (this.toUInt() shl 7)).toUByte(),
    this and 0x01u > 0u,
)

/**
 * Obtain the Rotation of this [UByte] and the carry input one bit right, including its carry.
 *
 * The input carry is copied to bit 7, and the bit 0 is returned as carry.
 */
fun UByte.rotateRight(carry: Boolean): Pair<UByte, Boolean> = Pair(
    ((this.toUInt() shr 1) or (if (carry) 0x80u else 0x00u)).toUByte(),
    this and 0x01u > 0u,
)

/**
 * Obtain the increment of this [UByte] by the given value.
 */
fun UByte.increment(v: UByte): UByte =  (this + v).toUByte()

/**
 * Obtain the increment of this [UByte] by the given value.
 */
fun UByte.increment(v: Byte): UByte =  (this.toByte() + v).toUByte()

/**
 * Return true if even number of bits is set.
 */
fun UByte.parity(): Boolean = this.countOneBits() % 2 == 0

/**
 * Convert this [UByte] into a hexadecimal string
 */
fun UByte.toHexString(): String = String.format("0x%02X", this.toInt())

/**
 * Convert this [UByte] into a binary string.
 */
fun UByte.toBinString(): String = String.format(
    "0b%8s",
    Integer.toBinaryString(this.toInt()).replace(' ', '0'),
)