package com.mahdisabra.currencyexchange


import com.mahdisabra.currencyexchange.api.model.ExchangeRates
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import com.mahdisabra.currencyexchange.api.ExchangeService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExchangeFragment : Fragment() {

    private var buyUsdTextView: TextView? = null
    private var sellUsdTextView: TextView? = null
    private var inputAmount1: com.google.android.material.textfield.TextInputLayout? = null
    private var inputAmount2: com.google.android.material.textfield.TextInputLayout? = null
    private var calcButton: Button? = null
    private var radioBuy: RadioButton? = null
    private var radioSell: RadioButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fetchRates()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view: View = inflater.inflate(R.layout.fragment_exchange,
            container, false)

        buyUsdTextView = view.findViewById(R.id.txtBuyUsdRate)
        sellUsdTextView = view.findViewById(R.id.txtSellUsdRate)
        inputAmount1 = view.findViewById(R.id.txtInputAmount1)
        inputAmount2 = view.findViewById(R.id.txtInputAmount2)
        calcButton = view.findViewById(R.id.btnCalc)
        calcButton?.setOnClickListener {
            calculate()
        }
        radioBuy = view.findViewById(R.id.rdBtnBuyUsd)
        radioBuy?.setOnClickListener{
            buySwap()
        }
        radioSell = view.findViewById(R.id.rdBtnSellUsd)
        radioSell?.setOnClickListener{
            sellSwap()
        }

        fetchRates()

        return view
    }

    private fun fetchRates(){
        ExchangeService.exchangeApi().getExchangeRates().enqueue(object : Callback<ExchangeRates> {
            override fun onResponse(call: Call<ExchangeRates>, response: Response<ExchangeRates>) {
                val responseBody: ExchangeRates? = response.body()

                buyUsdTextView?.text = String.format("%.2f", responseBody?.lbpToUsd)

                sellUsdTextView?.text = String.format("%.2f", responseBody?.usdToLbp)
            }

            override fun onFailure(call: Call<ExchangeRates>, t: Throwable) {
                return
                TODO("Not yet implemented")
            }
        })
    }

    private fun calculate(){
        var amount1 = inputAmount1?.editText?.text.toString().toInt()

        if (radioBuy?.isChecked == true){
            var amount2 = amount1/buyUsdTextView?.text.toString().toFloat()
            inputAmount2?.editText?.setText(String.format("%.2f", amount2))
        }
        else{
            var amount2 = amount1*sellUsdTextView?.text.toString().toFloat()
            inputAmount2?.editText?.setText(String.format("%.2f", amount2))
        }

    }

    private fun buySwap(){
        inputAmount1?.hint = "LBP Amount"
        inputAmount2?.hint = "USD Amount"
        inputAmount1?.editText?.setText("")
        inputAmount2?.editText?.setText("")
    }

    private fun sellSwap(){
        inputAmount1?.hint = "USD Amount"
        inputAmount2?.hint = "LBP Amount"
        inputAmount1?.editText?.setText("")
        inputAmount2?.editText?.setText("")
    }
}