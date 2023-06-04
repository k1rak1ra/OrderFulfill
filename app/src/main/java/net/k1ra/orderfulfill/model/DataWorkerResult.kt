package net.k1ra.orderfulfill.model

class DataWorkerResult<T>(
    val result: SuccessFail,
    val data: T
)