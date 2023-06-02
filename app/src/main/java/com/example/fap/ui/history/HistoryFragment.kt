package com.example.fap.ui.history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fap.data.FapDatabase
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        val view: View = binding.root

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
            val payments = db.fapDao().getPayments(sharedPreferences.getCurUser(requireContext()))
            for (payment in payments) {
                historyData.add(HistoryItem(payment.id, payment.title, payment.category ?: "", payment.price))
            }
            historyAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
