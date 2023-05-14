package com.mahdisabra.currencyexchange.api

import com.mahdisabra.currencyexchange.api.model.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

object ExchangeService {
    private const val API_URL: String = "http://127.0.0.1:5000"
    fun exchangeApi():Exchange {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(API_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(Exchange::class.java)
    }
    interface Exchange {
        @GET("/exchangeRate")
        fun getExchangeRates(): Call<ExchangeRates>
        @POST("/exchange")
        fun addTransaction(@Body transaction: Transaction): Call<Any>
        @POST("/user")
        fun addUser(@Body user: User): Call<User>
        @POST ("/authentication")
        fun authenticate(@Body user:User): Call<Token>
        @POST("/transaction")
        fun addTransaction(@Body transaction: Transaction,
                           @Header("Authorization") authorization: String?): Call<Any>
        @GET("/transaction")
        fun getTransactions(@Header("Authorization") authorization: String):
                Call<List<Transaction>>
        @GET("/exchangeRateOverTime")
        fun getRateOverTime(): Call<RateOverTime>
        @GET("/fetchTransactionRequests")
        fun getRequests(@Header("Authorization") authorization: String):
                Call<RequestList>
        @POST("/respondToTransactionRequest")
        fun respondToRequest(@Body response: Resp): Call<Any>
        @POST("/postTransactionRequest")
        fun postRequest(@Body request: Request,
                        @Header("Authorization") authorization: String?): Call<Any>
    }
}
