package com.mahdisabra.currencyexchange.api.model

import com.google.gson.annotations.SerializedName

class Request {
    @SerializedName("id")
    var id: Int? = null
    @SerializedName("sender_id")
    var send_id: Int? = null
    @SerializedName("receiver_id")
    var rec_id: Int? = null
    @SerializedName("exchange_state")
    var state: String? = null
    @SerializedName("usd_amount")
    var usdAmount: Float? = null
    @SerializedName("lbp_amount")
    var lbpAmount: Float? = null
    @SerializedName("usd_to_lbp")
    var usdToLbp: Boolean? = null
}