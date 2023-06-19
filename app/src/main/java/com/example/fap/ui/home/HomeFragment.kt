package com.example.fap.ui.home

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.fap.R
import com.example.fap.data.FapDatabase
import com.example.fap.databinding.FragmentHomeBinding
import com.example.fap.utils.SharedCurrencyManager
import com.example.fap.utils.SharedPreferencesManager
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferencesManager
    private lateinit var sharedCurrency: SharedCurrencyManager

    private lateinit var db: FapDatabase

    private lateinit var lblTotal: TextView
    private lateinit var chartBalance: PieChart
    private lateinit var chartCategory: BarChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        sharedPreferences = SharedPreferencesManager.getInstance(requireContext())
        sharedCurrency = SharedCurrencyManager.getInstance(requireContext())

        db = FapDatabase.getInstance(requireContext())

        val view = binding.root

        lblTotal = binding.lblTotal
        chartBalance = binding.chartBalance
        chartCategory = binding.chartCategory
    //get theme OnSurface Color
        val typedValue = TypedValue()
        val theme = requireContext().theme
        theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)
        @ColorInt val colorOnSurface = typedValue.data

        //Balance Chart
        chartBalance.setExtraOffsets(5f, 5f, 5f, 5f)
        chartBalance.setDrawEntryLabels(false)
        chartBalance.holeRadius = 70f
        chartBalance.transparentCircleRadius = 75f
        chartBalance.legend.isEnabled = false
        chartBalance.setHoleColor(resources.getColor(com.google.android.material.R.color.mtrl_btn_transparent_bg_color, context?.theme))
        chartBalance.setCenterTextSize(10f)
        chartBalance.setCenterTextColor(colorOnSurface)

        var einnahmen = 0f//getTotalIncome()
        var ausgaben = 0f//getTotalSpent()
        var saldo = einnahmen - ausgaben
        val entriesBalance = listOf(
            PieEntry(einnahmen.toFloat(), "Einnahmen"),
            PieEntry(ausgaben.toFloat(), "Ausgaben")
        )
        val dataSet = PieDataSet(entriesBalance, "Finanzen")
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
        val chartData = PieData(dataSet)
        chartBalance.data = chartData
        chartBalance.description.text = ""
        chartBalance.invalidate()
        chartBalance.notifyDataSetChanged()

        //Bar Chart Category


        return view
    }

    override fun onResume() {
        super.onResume()
        // update values
        lifecycleScope.launch {
            lblTotal.text = sharedCurrency.num2Money(updateTotal())
            // Pie Chart
            var einnahmen = getTotalIncome()
            var ausgaben = getTotalSpent()
            val entriesBalance = listOf(
                PieEntry(einnahmen.toFloat(), "Einnahmen"),
                PieEntry(ausgaben.toFloat(), "Ausgaben")
            )
            var saldo = einnahmen - ausgaben
            //TODO: change € to actual used currency
            chartBalance.centerText = "Income: ${String.format("%.2f", einnahmen)} € \nSpent: ${String.format("%.2f", ausgaben)} €\n _______________________ \nSaldo: ${String.format("%.2f",saldo)} €"

            val dataSet = PieDataSet(entriesBalance, "Finanzen")
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
            val chartData = PieData(dataSet)
            chartBalance.data = chartData
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private suspend fun updateTotal(): Double {
        var income: Double? = db.fapDao().getTotalIncome(sharedPreferences.getCurUser(requireContext()))
        var spent: Double? = db.fapDao().getTotalAmountSpent(sharedPreferences.getCurUser(requireContext()))

        if (income == null)
            income = 0.0
        if (spent == null)
            spent = 0.0

        return income - spent
    }

    private suspend fun getTotalIncome(): Double {
       var income: Double? = db.fapDao().getTotalIncome(sharedPreferences.getCurUser(requireContext()))

        if (income == null)
            income = 0.0

        return income
    }

    private suspend fun getTotalSpent(): Double {
        var spent: Double? = db.fapDao().getTotalAmountSpent(sharedPreferences.getCurUser(requireContext()))

        if (spent == null)
            spent = 0.0

        return spent
    }
}
