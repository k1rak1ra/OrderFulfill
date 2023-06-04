package net.k1ra.orderfulfill.providers.chitchats.api

import com.gap.hoodies_network.core.*
import com.google.gson.Gson
import net.k1ra.orderfulfill.providers.chitchats.model.ChitChatsApiAuth
import net.k1ra.orderfulfill.providers.chitchats.model.ChitChatsShipment
import net.k1ra.orderfulfill.providers.chitchats.model.ChitChatsShipmentResponseWrapper
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class ApiClient(apiAuthData: String?) {
    private val apiAuth = Gson().fromJson(apiAuthData, ChitChatsApiAuth::class.java)

    private val httpClient = HoodiesNetworkClient.Builder()
        .baseUrl("https://chitchats.com/api/v1/clients/${apiAuth.shipperId}/")
        .addHeader("Authorization", apiAuth.key)
        .build()

    suspend fun createShipmentAndGetId(shipment: ChitChatsShipment) : Result<ChitChatsShipmentResponseWrapper, HoodiesNetworkError> {
        return httpClient.post("shipments", shipment)
    }

    suspend fun buyShipment(shipmentIdentifier: String, selectedOptionIdentifier: String) {
        when (val result = httpClient.patch<Unit>("shipments/$shipmentIdentifier/buy", JSONObject("{\"postage_type\": \"$selectedOptionIdentifier\"}"))) {
            is Success -> println(result.value)
            is Failure -> println(result.reason.code)
        }
    }

    suspend fun getShipment(shipmentIdentifier: String) : ChitChatsShipmentResponseWrapper? {
        return when (val result = httpClient.get<ChitChatsShipmentResponseWrapper>("shipments/$shipmentIdentifier")) {
            is Success -> result.value
            is Failure -> null
        }
    }


    fun getLabelFromUrl(url: String) : String? {
        val con = URL(url).openConnection() as HttpURLConnection
        con.requestMethod = "GET"

        val responseCode = con.responseCode

        return if (responseCode == HttpURLConnection.HTTP_OK) { //success
            Base64.getEncoder().encodeToString(con.inputStream.readBytes())
        } else {
             null
        }
    }
}