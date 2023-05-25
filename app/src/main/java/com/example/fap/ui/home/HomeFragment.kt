package com.example.fap.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.fap.R
import com.example.fap.data.FapDatabase
import com.example.fap.databinding.FragmentHomeBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var sharedDatabase: SharedDatabaseManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        sharedDatabase = SharedDatabaseManager.getInstance(requireContext())

        val view = binding.root

        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        val lblTotal = binding.lblTotal
        val chartBalance = binding.chartBalance
        val chartStock = binding.chartStock

        lifecycleScope.launch {
            //lblTotal.text = sharedDatabase.getTotal().toString()
        }


    //Balance Chart
        chartBalance.setExtraOffsets(5f, 5f, 5f, 5f)
        chartBalance.setDrawEntryLabels(false)
        chartBalance.holeRadius = 70f
        chartBalance.transparentCircleRadius = 72.5f
        chartBalance.legend.isEnabled = false

        var einnahmen = 30f
        var ausgaben = 20f
        var saldo = (einnahmen - ausgaben)
        val entriesBalance = listOf(
            PieEntry(einnahmen, "Einnahmen"),
            PieEntry(ausgaben, "Ausgaben"),
            PieEntry(saldo, "Saldo")
        )
        val dataSet = PieDataSet(entriesBalance, "Finanzen")
        chartBalance.centerText = "Einnahmen: $einnahmen € \nAusgaben: $ausgaben €\n _______________________ \nSaldo: $saldo €"

        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f
        dataSet.colors = listOf(
            resources.getColor(R.color.green),
            resources.getColor(R.color.red),
            resources.getColor(androidx.transition.R.color.material_blue_grey_800)
        )
        chartBalance.data = PieData(dataSet)
        chartBalance.description.text = ""
        chartBalance.invalidate()
        chartBalance.notifyDataSetChanged()

    //Chart Stock
        val entriesStock = listOf(
            Entry(1f, 10f),
            Entry(2f, 2f),
            Entry(3f, 7f),
            Entry(4f, 20f),
        )
        val vl = LineDataSet(entriesStock, "Test1")
        vl.lineWidth = 2f

        val entriesStock2 = listOf(
            Entry(1f, 30f),
            Entry(2f, 4f),
            Entry(3f, 100f),
            Entry(4f, 2f),
        )
        val vl2 = LineDataSet(entriesStock2, "Test2")
        vl2.color = R.color.red
        vl2.lineWidth = 2f

        chartStock.data = LineData(vl, vl2)
        chartStock.axisRight.isEnabled = false
        chartStock.setTouchEnabled(false)
        chartStock.setPinchZoom(true)
        chartStock.description.text = "Stock"
        chartStock.animateX(1000, Easing.EaseInExpo)

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
}
