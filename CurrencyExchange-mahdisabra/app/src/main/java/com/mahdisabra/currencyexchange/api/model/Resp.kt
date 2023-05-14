package com.mahdisabra.currencyexchange.api.model

import com.google.gson.annotations.SerializedName

class Resp {
    @SerializedName("accepted")
    var accepted: String? = null
    @SerializedName("transaction_id")
    var transaction_id: Int? = null
}