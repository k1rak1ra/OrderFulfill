package net.k1ra.orderfulfill.model

import java.util.function.Consumer

/**
 * Facilitates the pattern of passing data to a Consumer that then does something with the data and passes a result to a callback Consumer
 */
class DataWorker<T, N>(
    val data: T,
    val callback: Consumer<DataWorkerResult<N>>
)