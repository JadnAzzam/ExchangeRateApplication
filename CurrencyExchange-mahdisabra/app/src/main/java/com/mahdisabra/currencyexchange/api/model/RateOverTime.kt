package com.mahdisabra.currencyexchange.api.model

import com.google.gson.annotations.SerializedName

class RateOverTime {
    @SerializedName("buy_usd_max_rate")
    var buy_usd_max_rate: Double? = null
    @SerializedName("buy_usd_min_rate")
    var buy_usd_min_rate: Double? = null
    @SerializedName("lbp_to_usd_ratesOverTime")
    var lbp_to_usd_ratesOverTime: List<Double>? = null
    @SerializedName("lbp_to_usd_ratesOverTime_dates")
    var lbp_to_usd_ratesOverTime_dates: List<String>? = null
    @SerializedName("num_of_buy_usd_transactions")
    var num_of_buy_usd_transactions: Int? = null
    @SerializedName("num_of_sell_usd_transactions")
    var num_of_sell_usd_transactions: Int? = null
    @SerializedName("sell_usd_max_rate")
    var sell_usd_max_rate: Double? = null
    @SerializedName("sell_usd_min_rate")
    var sell_usd_min_rate: Double? = null
    @SerializedName("usd_to_lbp_ratesOverTime")
    var usd_to_lbp_ratesOverTime: List<Double>? = null
    @SerializedName("usd_to_lbp_ratesOverTime_dates")
    var usd_to_lbp_ratesOverTime_dates: List<String>? = null
    @SerializedName("std_dev_sell_usd")
    var std_dev_sell_usd: Double? = null
    @SerializedName("std_dev_buy_usd")
    var std_dev_buy_usd: Double? = null
}