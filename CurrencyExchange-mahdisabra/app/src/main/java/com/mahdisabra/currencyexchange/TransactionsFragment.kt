package com.mahdisabra.currencyexchange

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import com.mahdisabra.currencyexchange.api.Authentication
import com.mahdisabra.currencyexchange.api.ExchangeService
import com.mahdisabra.currencyexchange.api.model.Transaction
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TransactionsFragment : Fragment() {

    class TransactionAdapter(
        private val inflater: LayoutInflater,
        private val dataSource: List<Transaction>
    ) : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent:
        ViewGroup?): View {
            val view: View = inflater.inflate(R.layout.item_transaction,
                parent, false)
            view.findViewById<TextView>(R.id.usd_amount).text = dataSource[position].usdAmount.toString()
            view.findViewById<TextView>(R.id.lbp_amount).text = dataSource[position].lbpAmount.toString()
            view.findViewById<TextView>(R.id.usd_to_lbp).text = dataSource[position].usdToLbp.toString()
            view.findViewById<TextView>(R.id.added_date).text = dataSource[position].addedDate.toString()
            return view
        }
        override fun getItem(position: Int): Any {
            return dataSource[position]
        }
        override fun getItemId(position: Int): Long {
            return dataSource[position].id?.toLong() ?: 0
        }
        override fun getCount(): Int {
            return dataSource.size
        }
    }

    private var listview: ListView? = null
    private var fab: FloatingActionButton? = null
    private var transactions: ArrayList<Transaction>? = ArrayList()
    private var adapter: TransactionAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fetchTransactions()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_transactions,
            container, false)
        fab = view.findViewById(R.id.fab)
        fab?.setOnClickListener { _ -> fetchTransactions() }
        listview = view.findViewById(R.id.listview)
        adapter =
            TransactionAdapter(layoutInflater, transactions!!)
        listview?.adapter = adapter
        return view
    }

    private fun fetchTransactions() {
        if (Authentication.getToken() != null) {
            ExchangeService.exchangeApi()
                .getTransactions("okay ${Authentication.getToken()}")
                .enqueue(object : Callback<List<Transaction>> {
                    override fun onFailure(call: Call<List<Transaction>>,
                                           t: Throwable) {
                        return
                    }
                    override fun onResponse(
                        call: Call<List<Transaction>>,
                        response: Response<List<Transaction>>
                    ) {
                        transactions?.clear()
                        transactions?.addAll(response.body()!!)
                        adapter?.notifyDataSetChanged()
                    }
                })
        }
    }
}