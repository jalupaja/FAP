package com.example.fap.ui.home

import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.fap.R
import com.example.fap.data.FapDatabase
import com.example.fap.data.Wallet
import com.example.fap.databinding.ViewpagerItemBinding
import com.example.fap.utils.SharedPreferencesManager
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class ViewPagerAdapter(private val wallets: List<Wallet>) : RecyclerView.Adapter<ViewPagerAdapter.ViewHolder>() {

    private lateinit var sharedPreferences: SharedPreferencesManager
    private lateinit var db: FapDatabase

    //private lateinit var lblTotal: TextView

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.viewpager_item, parent, false)

        db = FapDatabase.getInstance(context)
        sharedPreferences = SharedPreferencesManager.getInstance(context)
        /*
        TODO
        setup possible lateinit vars, constants, ...
         */

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return wallets.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val wallet = wallets[position]
        holder.bind(wallet)

        /*
        val context = holder.itemView.context
        on click listeners, ...
         */
    }

    private suspend fun updateTotal(context: Context): Double {
        var income: Double? = db.fapDao().getTotalIncome(sharedPreferences.getCurUser(context))
        var spent: Double? = db.fapDao().getTotalAmountSpent(sharedPreferences.getCurUser(context))

        if (income == null)
            income = 0.0
        if (spent == null)
            spent = 0.0

        return (income - spent)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var lblTotal: TextView = itemView.findViewById(R.id.lbl_total)
        private var chartBalance: PieChart = itemView.findViewById(R.id.chart_balance)
        private var chartStock: LineChart = itemView.findViewById(R.id.chart_stock)

        val resources = itemView.resources
        val context = itemView.context

        //get theme OnSurface Color
        val typedValue = TypedValue()
        val theme = context.theme
        @ColorInt
        val colorOnSurface = typedValue.data


        var einnahmen = 30f
        var ausgaben = 20f
        var saldo = einnahmen - ausgaben
        val entriesBalance = listOf(
            PieEntry(einnahmen, "Einnahmen"),
            PieEntry(ausgaben, "Ausgaben")
        )
        val dataSet = PieDataSet(entriesBalance, "Finanzen")

        val chartData = PieData(dataSet)

        //Chart com.example.fap.data.Stock
        val entriesStock = listOf(
            Entry(1f, 10f),
            Entry(2f, 2f),
            Entry(3f, 7f),
            Entry(4f, 20f),
        )
        val stockDataSet = LineDataSet(entriesStock, "Test1")

        val entriesStock2 = listOf(
            Entry(1f, 30f),
            Entry(2f, 4f),
            Entry(3f, 100f),
            Entry(4f, 2f),
        )
        val stockDataSet2 = LineDataSet(entriesStock2, "Test2")

        fun bind(wallet: Wallet) {
            // TODO implement db
            /*
            TODO
            lifecycleScope.launch {
                lblTotal.text = sharedCurrency.num2Money(updateTotal())
            }
            */
            Log.d("WalletAdapter", wallet.name)
            Log.d("WalletAdapter", wallet.userId)
            Log.d("WalletAdapter", wallets.size.toString())
            lblTotal.text = wallet.name

            theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)

            //Balance Chart
            chartBalance.setExtraOffsets(5f, 5f, 5f, 5f)
            chartBalance.setDrawEntryLabels(false)
            chartBalance.holeRadius = 70f
            chartBalance.transparentCircleRadius = 75f
            chartBalance.legend.isEnabled = false

            chartBalance.setHoleColor(resources.getColor(com.google.android.material.R.color.mtrl_btn_transparent_bg_color, context?.theme))
            chartBalance.setCenterTextSize(10f)
            chartBalance.setCenterTextColor(colorOnSurface)

            chartBalance.centerText = "Einnahmen: $einnahmen € \nAusgaben: $ausgaben €\n _______________________ \nSaldo: $saldo €"

            dataSet.sliceSpace = 3f
            dataSet.selectionShift = 5f
            dataSet.colors = listOf(
                resources.getColor(R.color.green, context?.theme),
                resources.getColor(R.color.red, context?.theme)
            )
            dataSet.setValueTextColors(
                listOf(
                    resources.getColor(com.google.android.material.R.color.mtrl_btn_transparent_bg_color, context?.theme)
                )
            )

            chartBalance.data = chartData
            chartBalance.description.text = ""
            chartBalance.invalidate()
            chartBalance.notifyDataSetChanged()

            stockDataSet.lineWidth = 2f
            stockDataSet.color = resources.getColor(R.color.yellow, context?.theme)

            stockDataSet2.color = resources.getColor(R.color.purple_700, context?.theme)
            stockDataSet2.lineWidth = 2f

            chartStock.data = LineData(stockDataSet, stockDataSet2)
            chartStock.axisRight.isEnabled = false
            chartStock.setTouchEnabled(false)
            chartStock.setPinchZoom(true)
            chartStock.description.text = "com.example.fap.data.Stock"
            chartStock.animateX(1000, Easing.EaseInExpo)
            chartStock.legend.textColor = colorOnSurface

            chartStock.xAxis.position = XAxis.XAxisPosition.BOTTOM
            chartStock.xAxis.setDrawGridLines(false)

            chartStock.invalidate()
            chartStock.notifyDataSetChanged()
        }
    }

}