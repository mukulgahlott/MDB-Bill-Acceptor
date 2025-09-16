package com.example.mdbbill.peripheral

import android.hardware.mdbMaster.MdbMaster
import android.os.Handler

abstract class MdbDeviceBase(
    protected val mdbMaster: MdbMaster?,
    protected val handler: Handler
) {
    /** mdb command data*/
    protected val cmdBuffer = ByteArray(Constant.MDB_DATA_BLOCK_MAX)

    var featureLevel = 1
    var currencyCode = 0x1840
    var scaleFactor = 0
    var decimalPlaces = 0
    val manufacturerCode = ByteArray(3)
    val serialNumber = ByteArray(12)
    val modelNumber = ByteArray(12)
    val softwareVersion = ByteArray(2)
    val optionalFeatures = ByteArray(4)
    var maxResponseTime = 2

    var isOnline = false
    var initializingSequenceFinish = false

    abstract fun process()
}
