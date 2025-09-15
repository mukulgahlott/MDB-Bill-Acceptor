package com.example.mdbbill.peripheral

object Constant {
    const val TAG = "mdbMaster"

    /**
     * Response code
     */
    const val ACK = 0x00
    const val NAK = 0xFF
    const val RET = 0xAA

    /**
     * MDB Data block size
     */
    const val MDB_DATA_BLOCK_MAX = 36

    /**
     * delay n*ms
     */
    fun delay(ms: Int) {
        try {
            Thread.sleep(ms.toLong())
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}
