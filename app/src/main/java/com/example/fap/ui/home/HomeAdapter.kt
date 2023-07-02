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
import com.example.fap.ui.category.CategoryItem
import com.example.fap.utils.SharedPreferencesManager
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class HomeAdapter(private val wallets: List<WalletInfo>) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    private lateinit var sharedPreferences: SharedPreferencesManager
    private lateinit var dbPayment: FapDatabase

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_home_item, parent, false)

        dbPayment = FapDatabase.getInstance(context)
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

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var lblBalance: TextView = itemView.findViewById(R.id.lbl_balance)
        private var lblIncomeMonth: TextView = itemView.findViewById(R.id.lbl_income_month)
        private var lblBalanceMonth: TextView = itemView.findViewById(R.id.lbl_balance_month)
        private var lblExpenseMonth: TextView = itemView.findViewById(R.id.lbl_expense_month)
        private var chartBalance: PieChart = itemView.findViewById(R.id.chart_balance)
        private var chartCategory : BarChart = itemView.findViewById(R.id.chart_category)

        val resources = itemView.resources
        val context = itemView.context

        //get theme OnSurface Color
        val typedValue = TypedValue()
        val theme = context.theme

        @ColorInt
        val colorOnSurface = typedValue.data

        init {
            setupChartBalance()
            setupChartCategory()
        }

        fun bind(wallet: WalletInfo) {
            //val income = "%.2f".format(wallet.income)
            //val expense = "%.2f".format(wallet.expense)
            //val balance = "%.2f".format(wallet.income - wallet.expense)
            val balanceCurrency = "%.2f".format(wallet.income - wallet.expense) + wallet.currency
            val incomeMonth = "%.2f".format(wallet.incomeMonth)
            val expenseMonth = "%.2f".format(wallet.expenseMonth)
            val balanceMonth = "%.2f".format(wallet.incomeMonth - wallet.expenseMonth)
            //val categories = wallet.category
            lblExpenseMonth.text = "%.2f".format(wallet.expenseMonth)

            // update values
            lblBalance.text = balanceCurrency
            lblIncomeMonth.text = incomeMonth
            lblExpenseMonth.text = expenseMonth
            lblBalanceMonth.text = balanceMonth
            val curDate = LocalDate.parse(
                SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(
                    Date()
                ), DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            chartBalance.centerText = "Data for: ${curDate.month}"
            chartBalance.setCenterTextSize(13f)
            val typedValue = TypedValue()
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true)
            @ColorInt val color = typedValue.data
            chartBalance.setCenterTextColor(color)
            updateChartData(wallet.incomeMonth, wallet.expenseMonth)
            updateCategoryData(wallet.category)
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

        private fun updateCategoryData(categories: List<CategoryItem>) {
            val entries = ArrayList<BarEntry>()
            val dataSets = ArrayList<BarDataSet>()


            categories.forEachIndexed{ index, categoryItem ->
                entries.add(BarEntry(index.toFloat(), categoryItem.sum.toFloat()))
            }

            val set1 = BarDataSet(entries, "test")
            set1.colors = listOf(
                resources.getColor(R.color.light_blue_900, context?.theme),
                resources.getColor(R.color.gray, context?.theme),
                resources.getColor(R.color.purple_700, context?.theme)
            )
            dataSets.add(set1)

            val data = BarData(set1)

            data.barWidth = 0.9f
            val typedValue = TypedValue()
            context.theme.resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true)
            @ColorInt val color = typedValue.data
            data.setValueTextColor(color)
            chartCategory.data = data
            chartCategory.setFitBars(true)
            chartCategory.xAxis.valueFormatter = BarXAxisFormatter(categories)
            chartCategory.xAxis.granularity = 1f
            chartCategory.xAxis.textColor = color
            chartCategory.axisLeft.textColor = color
            chartCategory.invalidate()
            chartCategory.notifyDataSetChanged()
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

        private fun setupChartCategory() {
            chartCategory.setDrawBarShadow(false)
            chartCategory.setDrawValueAboveBar(true)
            chartCategory.description.isEnabled = false
            chartCategory.setPinchZoom(false)
            chartCategory.setDrawGridBackground(false)

            val xAxis = chartCategory.xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)

            val leftAxis = chartCategory.axisLeft
            leftAxis.setDrawGridLines(false)

            val rightAxis = chartCategory.axisRight
            rightAxis.setDrawGridLines(false)
            rightAxis.setDrawLabels(false)

            chartCategory.legend.isEnabled = false

            chartCategory.invalidate()
            chartCategory.notifyDataSetChanged()
        }
    }
}