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

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    //TODO: fix runtime Error
    //private val chartBalance: PieChart = AppCompatActivity().findViewById(R.id.chart_balance)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root


        //Balance Chart
        /*
        chartBalance.setDrawHoleEnabled(true)
        chartBalance.setHoleColor(Color.WHITE)
        chartBalance.setTransparentCircleColor(Color.WHITE)
        chartBalance.setTransparentCircleAlpha(110)
        chartBalance.setHoleRadius(58f)
        chartBalance.setTransparentCircleRadius(61f)
        val entries = listOf(
            PieEntry(30f, "Einnahmen"),
            PieEntry(50f, "Saldo"),
            PieEntry(20f, "Ausgaben")
        )
        val dataSet = PieDataSet(entries, "TODO")
        dataSet.colors = listOf(
            color.darker_gray,
            color.holo_green_light,
            color.holo_red_light
        )
        val cbData = PieData(dataSet)
        chartBalance.data = cbData
        chartBalance.invalidate()
        */

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
