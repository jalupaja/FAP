package com.example.fap.ui.history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fap.data.FapDatabase
import com.example.fap.data.Payment
import com.example.fap.databinding.FragmentHistoryBinding
import com.example.fap.utils.SharedPreferencesManager
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.

    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferencesManager
    private var historyData = ArrayList<HistoryItem>()
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var categoryHistory: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val view: View = binding.root
        categoryHistory = arguments?.getString("categoryNameHistory", "showAll").toString()
        Log.d("FAP", categoryHistory + "zap")
        sharedPreferences = SharedPreferencesManager.getInstance(requireContext())

        val recyclerView = binding.historyRecyclerview
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        historyAdapter = HistoryAdapter(historyData)
        recyclerView.adapter = historyAdapter

        return view
    }

    override fun onResume() {
        super.onResume()
        historyData.clear()
        lifecycleScope.launch {
            val db = FapDatabase.getInstance(requireContext())
            val payments = if (categoryHistory == "showAll") {
                db.fapDao().getPayments(sharedPreferences.getCurUser(requireContext()))
            } else {
                db.fapDao().getPaymentsByCategory(sharedPreferences.getCurUser(requireContext()), categoryHistory)
            }
            for (payment in payments) {
                historyData.add(HistoryItem(payment.id, payment.title, payment.category ?: "", payment.price, payment.isPayment))
            }
            if (payments.isEmpty()) {
                binding.lblHistoryEmpty.visibility = View.VISIBLE
            } else {
                binding.lblHistoryEmpty.visibility = View.GONE
            }
            historyAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
