package com.mahdisabra.currencyexchange.api.model

import com.google.gson.annotations.SerializedName
import java.util.*

class Transaction {
    @SerializedName("added_date")
    var addedDate: String? = null
    @SerializedName("sender_id")
    var id: Int? = null
    @SerializedName("usd_amount")
    var usdAmount: Float? = null
    @SerializedName("lbp_amount")
    var lbpAmount: Float? = null
    @SerializedName("usd_to_lbp")
    var usdToLbp: Boolean? = null
}