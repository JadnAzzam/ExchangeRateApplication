package com.mahdisabra.currencyexchange


import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.viewpager2.widget.ViewPager2
import com.mahdisabra.currencyexchange.api.Authentication
import com.mahdisabra.currencyexchange.api.ExchangeService
import com.mahdisabra.currencyexchange.api.model.RateOverTime
import com.mahdisabra.currencyexchange.api.model.Request
import com.mahdisabra.currencyexchange.api.model.Transaction
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputLayout
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private var fab: FloatingActionButton? = null
    private var graph: FloatingActionButton? = null
    private var transactionDialog: View? = null
    private var requestDialog: View? = null
    private var graphDialog: View? = null
    private var menu: Menu? = null
    private var tabLayout: TabLayout? = null
    private var tabsViewPager: ViewPager2? = null

    private fun showDialog() {
        transactionDialog = LayoutInflater.from(this)
            .inflate(R.layout.diallog_transaction, null, false)
        MaterialAlertDialogBuilder(this).setView(transactionDialog)
            .setTitle("Add Transaction")
            .setMessage("Enter transaction details")
            .setBackground(getResources().getDrawable(R.drawable.bg,null))
            .setPositiveButton("Add") { dialog, _ ->
                val usdAmount =
                    transactionDialog?.findViewById<TextInputLayout>(R.id.txtInputAmount1)?.editText?.text.toString().toFloat()
                val lbpAmount =
                    transactionDialog?.findViewById<TextInputLayout>(R.id.txtInptLbpAmount)?.editText?.text.toString().toFloat()
                val usdToLbp = transactionDialog?.findViewById<RadioButton>(R.id.rdBtnSellUsd)?.isChecked
                val transaction =  Transaction()
                transaction.usdAmount=usdAmount
                transaction.lbpAmount=lbpAmount
                transaction.usdToLbp=usdToLbp
                addTransaction(transaction)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showRequest() {
        requestDialog = LayoutInflater.from(this)
            .inflate(R.layout.dialog_request, null, false)
        MaterialAlertDialogBuilder(this).setView(requestDialog)
            .setTitle("Add Request")
            .setMessage("Enter Request details")
            .setBackground(getResources().getDrawable(R.drawable.bg,null))
            .setPositiveButton("Send") { dialog, _ ->
                val rec_id =
                    requestDialog?.findViewById<TextInputLayout>(R.id.txtReceiverId)?.editText?.text.toString().toInt()
                val usdAmount =
                    requestDialog?.findViewById<TextInputLayout>(R.id.txtInputAmount1)?.editText?.text.toString().toFloat()
                val lbpAmount =
                    requestDialog?.findViewById<TextInputLayout>(R.id.txtInptLbpAmount)?.editText?.text.toString().toFloat()
                val usdToLbp = requestDialog?.findViewById<RadioButton>(R.id.rdBtnSellUsd)?.isChecked
                val request =  Request()
                request.rec_id=rec_id
                request.usdAmount=usdAmount
                request.lbpAmount=lbpAmount
                request.usdToLbp=usdToLbp
                postRequest(request)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun postRequest(request: Request) {
        ExchangeService.exchangeApi().postRequest(request,
            if (Authentication.getToken() != null)
                "Bearer ${Authentication.getToken()}"
            else
                null
        ).enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response:
            Response<Any>) {
                Snackbar.make(fab as View, "Request Sent!",
                    Snackbar.LENGTH_LONG)
                    .show()
            }
            override fun onFailure(call: Call<Any>, t: Throwable) {
                Snackbar.make(fab as View, "Could not send request.",
                    Snackbar.LENGTH_LONG)
                    .show()
            }
        })
    }

    private fun showGraph() {
        graphDialog = LayoutInflater.from(this).inflate(R.layout.dialog_graph, null, false)
        fetchRatesOverTime()
        MaterialAlertDialogBuilder(this).setView(graphDialog)
            .setTitle("Graph")
            .setBackground(getResources().getDrawable(R.drawable.bg,null))
            .setMessage("View Rate Timeline")
            .setNegativeButton("Exit") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun fetchRatesOverTime(){
        ExchangeService.exchangeApi().getRateOverTime().enqueue(object : Callback<RateOverTime> {
            override fun onResponse(call: Call<RateOverTime>, response: Response<RateOverTime>) {

                val responseBody: RateOverTime? = response.body()

                val now: Calendar = Calendar.getInstance()

                now.add(Calendar.MONTH, -5)
                var graph: GraphView? = graphDialog?.findViewById(R.id.graph)
                var stat1: TextView? = graphDialog?.findViewById(R.id.stat1)
                var stat2: TextView? = graphDialog?.findViewById(R.id.stat2)
                var stat3: TextView? = graphDialog?.findViewById(R.id.stat3)
                var stat4: TextView? = graphDialog?.findViewById(R.id.stat4)
                var stat5: TextView? = graphDialog?.findViewById(R.id.stat5)
                var stat6: TextView? = graphDialog?.findViewById(R.id.stat6)
                var stat7: TextView? = graphDialog?.findViewById(R.id.stat7)
                var stat8: TextView? = graphDialog?.findViewById(R.id.stat8)
                stat1?.text = "Max S:" + responseBody?.sell_usd_max_rate
                stat2?.text = "Max B:" + responseBody?.buy_usd_max_rate
                stat3?.text = "Min S:" + responseBody?.sell_usd_min_rate
                stat4?.text = "Min B:" + responseBody?.buy_usd_min_rate
                stat5?.text = "Num of S:" + responseBody?.num_of_sell_usd_transactions
                stat6?.text = "Num of B:" + responseBody?.num_of_buy_usd_transactions
                stat7?.text = "STD S:" + "%.2f".format(responseBody?.std_dev_sell_usd)
                stat8?.text = "STD B:" + "%.2f".format(responseBody?.std_dev_buy_usd)
                graph?.gridLabelRenderer?.labelFormatter = DateAsXAxisLabelFormatter(this@MainActivity, SimpleDateFormat("MMM"))
                Log.d(now.time.toString(),"lol")
                val series: LineGraphSeries<DataPoint> = LineGraphSeries(
                    arrayOf(
                        DataPoint(now.time, responseBody?.lbp_to_usd_ratesOverTime?.get(now.time.month) ?: 0.0)
                    )
                )
                val series1: LineGraphSeries<DataPoint> = LineGraphSeries(
                    arrayOf(
                        DataPoint(now.time, responseBody?.usd_to_lbp_ratesOverTime?.get(now.time.month) ?: 0.0)
                    )
                )
                val numbs = arrayOf(1, 1, 1, 1, 1)
                for (i in numbs){
                    now.add(Calendar.MONTH,i)
                    series.appendData(DataPoint(now.time,responseBody?.lbp_to_usd_ratesOverTime?.get(now.time.month) ?: 0.0),true,12)
                    series1.appendData(DataPoint(now.time,responseBody?.usd_to_lbp_ratesOverTime?.get(now.time.month) ?: 0.0),true,12)
                }
                graph?.addSeries(series)
                graph?.addSeries(series1)
                series.color = Color.RED;
                graph?.gridLabelRenderer?.numHorizontalLabels = 4;
                graph?.viewport?.isXAxisBoundsManual = true
                graph?.viewport?.isYAxisBoundsManual = true
                now.add(Calendar.MONTH, 2)
                graph?.viewport?.setMaxX(now.time.time.toDouble())
                now.add(Calendar.MONTH, -6)
                graph?.viewport?.setMinX(now.time.time.toDouble())
                graph?.viewport?.setMinY(20000.00)
                graph?.viewport?.setMaxY(35000.00)
            }

            override fun onFailure(call: Call<RateOverTime>, t: Throwable) {
                return
                TODO("Not yet implemented")
            }
        })
    }

    private fun addTransaction(transaction: Transaction) {

        ExchangeService.exchangeApi().addTransaction(transaction,
            if (Authentication.getToken() != null)
                "Bearer ${Authentication.getToken()}"
            else
                null
        ).enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response:
            Response<Any>) {
                Snackbar.make(fab as View, "Transaction added!",
                    Snackbar.LENGTH_LONG)
                    .show()
            }
            override fun onFailure(call: Call<Any>, t: Throwable) {
                Snackbar.make(fab as View, "Could not add transaction.",
                    Snackbar.LENGTH_LONG)
                    .show()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menu = menu
        setMenu()
        return true
    }

    private fun setMenu() {
        menu?.clear()
        menuInflater.inflate(if(Authentication.getToken() == null)
            R.menu.menu_logged_out else R.menu.menu_logged_in, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.login) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        } else if (item.itemId == R.id.register) {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        } else if (item.itemId == R.id.logout) {
            Authentication.clearToken()
            setMenu()
        } else if (item.itemId == R.id.new_req) {
            showRequest()
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Authentication.initialize(this)
        setContentView(R.layout.activity_main)

        tabLayout = findViewById(R.id.tabLayout)
        tabsViewPager = findViewById(R.id.tabsViewPager)
        tabLayout?.tabMode = TabLayout.MODE_FIXED
        tabLayout?.isInlineLabel = true
        // Enable Swipe
        tabsViewPager?.isUserInputEnabled = true
        // Set the ViewPager Adapter
        val adapter = TabsPagerAdapter(supportFragmentManager, lifecycle)
        tabsViewPager?.adapter = adapter
        TabLayoutMediator(tabLayout!!, tabsViewPager!!) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Exchange"
                    tabsViewPager?.getChildAt(2)
                }
                1 -> {
                    tab.text = "Transactions"
                }
                2 -> {
                    tab.text = "Pending"
                }
            }
        }.attach()

        fab = findViewById(R.id.fab)
        fab?.setOnClickListener { _ -> showDialog() }

        graph = findViewById(R.id.graph_button)
        graph?.setOnClickListener { _ -> showGraph()}
    }
}