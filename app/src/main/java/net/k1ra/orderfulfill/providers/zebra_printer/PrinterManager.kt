package net.k1ra.orderfulfill.providers.zebra_printer

import com.zebra.sdk.comm.TcpConnection
import java.util.*

class PrinterManager {
    fun print(ip: String, base64EncodedZpl: String) {
        val connection = TcpConnection(ip, TcpConnection.DEFAULT_ZPL_TCP_PORT)
        connection.open()
        connection.write(Base64.getDecoder().decode(base64EncodedZpl))
        connection.close()
    }
}