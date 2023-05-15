package com.example.fap.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.fap.R
import com.example.fap.databinding.FragmentHomeBinding
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import android.R.color
import android.graphics.Color
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val view = binding.root

        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        val chartBalance = binding.chartBalance
        val chartStock = binding.chartStock

        //Balance Chart
        chartBalance.setExtraOffsets(5f, 10f, 5f, 5f)
        chartBalance.setTransparentCircleColor(Color.WHITE)
        chartBalance.setTransparentCircleAlpha(110)
        chartBalance.holeRadius = 58f
        chartBalance.transparentCircleRadius = 61f
        chartBalance.legend.isEnabled = false
        chartBalance.setEntryLabelColor(Color.WHITE)
        chartBalance.setEntryLabelTextSize(12f)
        val entriesBalance = listOf(
            PieEntry(30f, "Einnahmen"),
            PieEntry(50f, "Saldo"),
            PieEntry(20f, "Ausgaben")
        )
        val dataSet = PieDataSet(entriesBalance, "Finanzen")
        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f
        dataSet.colors = listOf(
            resources.getColor(R.color.green),
            resources.getColor(R.color.red),
            resources.getColor(R.color.purple_700)
        )
        val cbData = PieData(dataSet)
        chartBalance.data = cbData
        chartBalance.highlightValues(null)
        chartBalance.invalidate()

        //Chart Stock
        val entriesStock = listOf(
            Entry(1f, 10f),
            Entry(2f, 2f),
            Entry(3f, 7f),
            Entry(4f, 20f),
        )

        val vl = LineDataSet(entriesStock, "Test1")
        vl.setDrawValues(false)
        vl.setDrawFilled(false)
        vl.lineWidth = 2f

        val entriesStock2 = listOf(
            Entry(1f, 30f),
            Entry(2f, 4f),
            Entry(3f, 100f),
            Entry(4f, 2f),
        )

        val vl2 = LineDataSet(entriesStock2, "Test2")
        vl.setDrawValues(false)
        vl.setDrawFilled(false)
        vl.lineWidth = 2f

        chartStock.data = LineData(vl, vl2)
        chartStock.axisRight.isEnabled
        chartStock.setTouchEnabled(true)
        chartStock.setPinchZoom(true)
        chartStock.description.text = "Stock"
        chartStock.animateX(1800, Easing.EaseInExpo)
        chartStock.invalidate()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
