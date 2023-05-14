package com.mahdisabra.currencyexchange


import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.mahdisabra.currencyexchange.api.Authentication
import com.mahdisabra.currencyexchange.api.ExchangeService
import com.mahdisabra.currencyexchange.api.model.Request
import com.mahdisabra.currencyexchange.api.model.RequestList
import com.mahdisabra.currencyexchange.api.model.Resp
import com.mahdisabra.currencyexchange.api.model.Token
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PendingFragment : Fragment() {
    private var confirmButton: Button? = null
    private var rejectButton: Button? = null
    private var fab: FloatingActionButton? = null

    class PendingAdapter(
        private val inflater: LayoutInflater,
        private val dataSource: List<Request>
    ) : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent:
        ViewGroup?): View {
            val view: View = inflater.inflate(R.layout.item_request,
                parent, false)
            view.findViewById<TextView>(R.id.usd_amount).text = dataSource[position].usdAmount.toString()
            view.findViewById<TextView>(R.id.lbp_amount).text = dataSource[position].lbpAmount.toString()
            view.findViewById<TextView>(R.id.usd_to_lbp).text = dataSource[position].usdToLbp.toString()
            view.findViewById<TextView>(R.id.trans_id).text = dataSource[position].id.toString()
            return view
        }
        override fun getItem(position: Int): Any {
            return dataSource[position]
        }
        override fun getItemId(position: Int): Long {
            return dataSource[position].send_id?.toLong() ?: 0
        }
        override fun getCount(): Int {
            return dataSource.size
        }
    }

    private var listview: ListView? = null
    private var requests: ArrayList<Request>? = ArrayList()
    private var adapter: PendingAdapter? = null
    private val selected: Resp? = Resp()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fetchRequests()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_pending,
            container, false)
        confirmButton = view.findViewById(R.id.btnConfirm)
        rejectButton = view.findViewById(R.id.btnReject)
        confirmButton?.setOnClickListener {
            confirm()
        }
        rejectButton?.setOnClickListener {
            reject()
        }
        fab = view.findViewById(R.id.fab)
        fab?.setOnClickListener { _ -> fetchRequests() }
        listview = view.findViewById(R.id.listview)
        adapter =
            PendingAdapter(layoutInflater, requests!!)
        listview?.adapter = adapter
        listview?.setOnItemClickListener { parent, view, position, id ->

            selected?.transaction_id = view.findViewById<TextView>(R.id.trans_id).text.toString().toInt()
            Log.d("",view.findViewById<TextView>(R.id.trans_id).text.toString().toInt().toString())
            Log.d("", selected?.transaction_id.toString())
        }
        return view
    }

    private fun confirm(){
        if (selected?.transaction_id==null){Log.d("","");return}

        selected?.accepted="accepted"

        ExchangeService.exchangeApi().respondToRequest(selected).enqueue(object :
            Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                Snackbar.make(
                    confirmButton as View,
                    "Transaction Confirmation Failed.",
                    Snackbar.LENGTH_LONG
                )
                    .show()
            }
            override fun onResponse(call: Call<Any>, response:
            Response<Any>
            ) {
                Snackbar.make(
                    confirmButton as View,
                    "Transaction Confirmation Successful.",
                    Snackbar.LENGTH_LONG
                )
                    .show()
                fetchRequests()
            }
        })
    }

    private fun reject(){
        if (selected?.transaction_id==null){return}

        selected?.accepted="rejected"

        ExchangeService.exchangeApi().respondToRequest(selected).enqueue(object :
            Callback<Any> {
            override fun onFailure(call: Call<Any>, t: Throwable) {
                Snackbar.make(
                    confirmButton as View,
                    "Transaction Rejection Failed.",
                    Snackbar.LENGTH_LONG
                )
                    .show()
            }
            override fun onResponse(call: Call<Any>, response:
            Response<Any>
            ) {
                Snackbar.make(
                    confirmButton as View,
                    "Transaction Rejection Successful.",
                    Snackbar.LENGTH_LONG
                )
                    .show()
                fetchRequests()
            }
        })
    }

    private fun fetchRequests() {
        if (Authentication.getToken() != null) {
            ExchangeService.exchangeApi()
                .getRequests("okay ${Authentication.getToken()}")
                .enqueue(object : Callback<RequestList> {
                    override fun onFailure(call: Call<RequestList>,
                                           t: Throwable) {
                        return
                    }
                    override fun onResponse(
                        call: Call<RequestList>,
                        response: Response<RequestList>
                    ) {
                        requests?.clear()
                        requests?.addAll(response.body()?.pendingReceivedRequests!!)
                        adapter?.notifyDataSetChanged()
                    }
                })
        }
    }

}