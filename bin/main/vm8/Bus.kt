package vm8

interface Bus<A, D> {
    // Read a datum at given address from the bus
    suspend fun read(addr: A): D

    // write a datum at given address to the bus
    suspend fun write(addr: A, v: D)
}
