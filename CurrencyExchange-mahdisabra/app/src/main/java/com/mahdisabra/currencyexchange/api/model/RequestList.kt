package com.mahdisabra.currencyexchange.api.model

import com.google.gson.annotations.SerializedName

class RequestList {
    @SerializedName("acceptedSentRequests")
    var acceptedSentRequests: List<Request>? = null
    @SerializedName("pendingReceivedRequests")
    var pendingReceivedRequests: List<Request>? = null
    @SerializedName("pendingSentRequests")
    var pendingSentRequests: List<Request>? = null
    @SerializedName("rejectedSentRequests")
    var rejectedSentRequests: List<Request>? = null
}