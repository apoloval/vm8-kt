package vm8

interface Bus<A, D> {
    // Read a datum at given address from the bus
    suspend fun read(addr: A): D
}
