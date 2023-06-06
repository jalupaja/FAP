package com.example.fap.ui.home

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.fap.R
import com.example.fap.data.FapDatabase
import com.example.fap.databinding.FragmentHomeBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var db: FapDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        db = FapDatabase.getInstance(requireContext())

        val view = binding.root

        val homeViewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]

        val lblTotal = binding.lblTotal
        val chartBalance = binding.chartBalance
        val chartStock = binding.chartStock
    //get theme OnSurface Color
        val typedValue = TypedValue()
        val theme = requireContext().theme
        theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)
        @ColorInt val colorOnSurface = typedValue.data

        lifecycleScope.launch {
            lblTotal.text = num2Money(updateTotal())
        }

    //Balance Chart
        chartBalance.setExtraOffsets(5f, 5f, 5f, 5f)
        chartBalance.setDrawEntryLabels(false)
        chartBalance.holeRadius = 70f
        chartBalance.transparentCircleRadius = 75f
        chartBalance.legend.isEnabled = false
        chartBalance.setHoleColor(resources.getColor(com.google.android.material.R.color.mtrl_btn_transparent_bg_color, context?.theme))
        chartBalance.setCenterTextSize(10f)
        chartBalance.setCenterTextColor(colorOnSurface)

        var einnahmen = 30f
        var ausgaben = 20f
        var saldo = einnahmen - ausgaben
        val entriesBalance = listOf(
            PieEntry(einnahmen, "Einnahmen"),
            PieEntry(ausgaben, "Ausgaben")
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
        chartBalance.data = PieData(dataSet)
        chartBalance.description.text = ""
        chartBalance.invalidate()
        chartBalance.notifyDataSetChanged()

    //Chart com.example.fap.data.Stock
        val entriesStock = listOf(
            Entry(1f, 10f),
            Entry(2f, 2f),
            Entry(3f, 7f),
            Entry(4f, 20f),
        )
        val stockDataSet = LineDataSet(entriesStock, "Test1")
        stockDataSet.lineWidth = 2f
        stockDataSet.color = resources.getColor(R.color.yellow, context?.theme)

        val entriesStock2 = listOf(
            Entry(1f, 30f),
            Entry(2f, 4f),
            Entry(3f, 100f),
            Entry(4f, 2f),
        )
        val stockDataSet2 = LineDataSet(entriesStock2, "Test2")
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

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun num2Money(num: Number): String {
        val currency: Char = '€'
        return "%.2f".format(num) + currency
    }

    private suspend fun updateTotal(): Double {
        // income and spent CAN be null even if Android Studio tells you otherwise
        val income: Double? = db.fapDao().getTotalIncome(requireContext().getString(R.string.shared_prefs_cur_user))
        val spent: Double? = db.fapDao().getTotalAmountSpent(requireContext().getString(R.string.shared_prefs_cur_user))
        return if (income != null && spent != null) {
            (income - spent)
        } else {
            0.0
        }
    }
}
