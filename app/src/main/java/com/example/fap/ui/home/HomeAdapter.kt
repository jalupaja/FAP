package com.example.fap.ui.home

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.fap.R
import com.example.fap.data.FapDatabase
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

class HomeAdapter(private val wallets: List<WalletInfo>) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    private lateinit var sharedPreferences: SharedPreferencesManager
    private lateinit var db: FapDatabase

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_home_item, parent, false)

        db = FapDatabase.getInstance(context)
        sharedPreferences = SharedPreferencesManager.getInstance(context)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return wallets.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val wallet = wallets[position]
        holder.bind(wallet)
    }

    private suspend fun updateTotal(context: Context): Double {
        var income: Double? = db.fapDao().getTotalIncome(sharedPreferences.getCurUser(context))
        var expense: Double? =
            db.fapDao().getTotalAmountSpent(sharedPreferences.getCurUser(context))

        if (income == null)
            income = 0.0
        if (expense == null)
            expense = 0.0

        return (income - expense)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var lblBalance: TextView = itemView.findViewById(R.id.lbl_balance)
        private var lblIncomeMonth: TextView = itemView.findViewById(R.id.lbl_income_month)
        private var lblBalanceMonth: TextView = itemView.findViewById(R.id.lbl_balance_month)
        private var lblExpenseMonth: TextView = itemView.findViewById(R.id.lbl_expense_month)
        private var chartBalance: PieChart = itemView.findViewById(R.id.chart_balance)
        private var chartStock: LineChart = itemView.findViewById(R.id.chart_stock)

        val resources = itemView.resources
        val context = itemView.context

        //get theme OnSurface Color
        val typedValue = TypedValue()
        val theme = context.theme

        @ColorInt
        val colorOnSurface = typedValue.data


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

        init {
            setupChartBalance()
            setupChartStock()
        }

        fun bind(wallet: WalletInfo) {
            val income = "%.2f".format(wallet.income)
            val expense = "%.2f".format(wallet.expense)
            val balance = "%.2f".format(wallet.income - wallet.expense)
            val balanceCurrency = "%.2f".format(wallet.income - wallet.expense) + wallet.currency
            val incomeMonth = "%.2f".format(wallet.incomeMonth)
            val expenseMonth = "%.2f".format(wallet.expenseMonth)
            val balanceMonth = "%.2f".format(wallet.incomeMonth - wallet.expenseMonth)
            lblExpenseMonth.text = "%.2f".format(wallet.expenseMonth)

            // update values
            lblBalance.text = balanceCurrency
            lblIncomeMonth.text = incomeMonth
            lblExpenseMonth.text = expenseMonth
            lblBalanceMonth.text = balanceMonth
            //chartBalance.centerText = "Income: ${wallet.income}${wallet.currency} \nExpense: ${wallet.expense}}${wallet.currency}"
            updateChartData(wallet.incomeMonth, wallet.expenseMonth)
        }
        private fun updateChartData(income: Double, expense: Double) {
            var nDataSet = PieDataSet(
                listOf(
                    PieEntry(income.toFloat(), "Income"),
                    PieEntry(expense.toFloat(), "Expense")
                ), "monthly finances"
            )

            if (income == 0.0 && expense == 0.0) {
                nDataSet = PieDataSet(
                    listOf(
                        PieEntry(1F, "")
                    ), "monthly finances"
                )

                nDataSet.colors = listOf(
                    resources.getColor(R.color.gray, context?.theme)
                )
            } else {
                nDataSet.colors = listOf(
                    resources.getColor(R.color.green, context?.theme),
                    resources.getColor(R.color.red, context?.theme)
                )
            }

            nDataSet.sliceSpace = 3f
            nDataSet.selectionShift = 5f

            nDataSet.setValueTextColors(
                listOf(
                    resources.getColor(
                        com.google.android.material.R.color.mtrl_btn_transparent_bg_color,
                        context?.theme
                    )
                )
            )

            chartBalance.data.dataSet = nDataSet

            chartBalance.invalidate()
            chartBalance.notifyDataSetChanged()
        }

        private fun setupOther() {
            theme.resolveAttribute(
                com.google.android.material.R.attr.colorOnSurface,
                typedValue,
                true
            )
        }

        private fun setupChartBalance() {
            //Balance Chart
            chartBalance.setExtraOffsets(5f, 5f, 5f, 5f)
            chartBalance.setDrawEntryLabels(false)
            chartBalance.holeRadius = 70f
            chartBalance.transparentCircleRadius = 75f
            chartBalance.legend.isEnabled = false

            chartBalance.setHoleColor(
                resources.getColor(
                    com.google.android.material.R.color.mtrl_btn_transparent_bg_color,
                    context?.theme
                )
            )
            chartBalance.setCenterTextSize(10f)
            chartBalance.setCenterTextColor(colorOnSurface)

            val dataSet = PieDataSet(listOf(
                PieEntry(0F),
                PieEntry(0F)
            ), "monthly finances")
            val chartData = PieData(dataSet)
            chartBalance.data = chartData
            chartBalance.description.text = ""
            chartBalance.invalidate()
            chartBalance.notifyDataSetChanged()
        }

        private fun setupChartStock() {
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