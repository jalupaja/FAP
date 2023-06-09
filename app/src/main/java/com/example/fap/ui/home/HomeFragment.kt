package com.example.fap.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.fap.R
import com.example.fap.data.FapDatabase
import com.example.fap.databinding.FragmentHomeBinding
import com.example.fap.utils.SharedCurrencyManager
import com.example.fap.utils.SharedPreferencesManager
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferencesManager
    private lateinit var sharedCurrency: SharedCurrencyManager

    private var wallets = ArrayList<WalletInfo>()
    private lateinit var walletAdapter: HomeAdapter
    private lateinit var viewpager: ViewPager2
    private lateinit var indicatorText: TextView
    private lateinit var indicatorLeft: ImageView
    private lateinit var indicatorRight: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        sharedPreferences = SharedPreferencesManager.getInstance(requireContext())
        sharedCurrency = SharedCurrencyManager.getInstance(requireContext())

        val view = binding.root

        viewpager = binding.viewpagerHome
        indicatorText = binding.indicatorText
        indicatorLeft = binding.indicatorLeft
        indicatorRight = binding.indicatorRight

        walletAdapter = HomeAdapter(wallets)
        viewpager.adapter = walletAdapter

        viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateIndicator(position)
            }
        })

        indicatorRight.setOnClickListener {
            viewpager.setCurrentItem(viewpager.currentItem + 1, true)
        }

        indicatorLeft.setOnClickListener {
            viewpager.setCurrentItem(viewpager.currentItem - 1, true)
        }

        return view
    }

    override fun onResume() {
        super.onResume()

        var totalSpent = 0.0
        var totalIncome = 0.0
        var totalSpentMonth = 0.0
        var totalIncomeMonth = 0.0
        val currency = sharedCurrency.getCurrency()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)

        lifecycleScope.launch {

            val db = FapDatabase.getInstance(requireContext())
            val listOfPaymentsByWallets = db.fapDaoPayment().getPaymentsByWallets(sharedPreferences.getCurUser(requireContext()))

            val categories = db.fapDaoPayment().getTotalAmountByCategory(sharedPreferences.getCurUser(requireContext()))


            wallets.clear()
            for (paymentsByWallet in listOfPaymentsByWallets) {
                var totalSpentWallet = 0.0
                var totalIncomeWallet = 0.0
                var totalSpentMonthWallet = 0.0
                var totalIncomeMonthWallet = 0.0
                val walletName = paymentsByWallet.wallet.name

                val categoriesWallet = db.fapDaoPayment().getTotalAmountByCategoryByWallet(sharedPreferences.getCurUser(requireContext()), walletName)

                for (payment in paymentsByWallet.payments) {
                    val paymentYear = Calendar.getInstance().apply { time = payment.date }.get(Calendar.YEAR)
                    val paymentMonth = Calendar.getInstance().apply { time = payment.date }.get(Calendar.MONTH)

                    if (paymentYear == currentYear && paymentMonth == currentMonth) {
                        if (payment.isPayment) {
                            totalSpentMonthWallet += payment.price
                        } else {
                            totalIncomeMonthWallet += payment.price
                        }
                    }
                    if (payment.isPayment) {
                        totalSpentWallet += payment.price
                    } else {
                        totalIncomeWallet += payment.price
                    }
                }
                totalIncomeMonth += totalIncomeMonthWallet
                totalSpentMonth += totalSpentMonthWallet
                totalIncome += totalIncomeWallet
                totalSpent += totalSpentWallet

                wallets.add(WalletInfo(walletName, totalIncomeWallet, totalSpentWallet, totalIncomeMonthWallet, totalSpentMonthWallet, currency, categoriesWallet))
            }
            wallets.add(0, WalletInfo("", totalIncome, totalSpent, totalIncomeMonth, totalSpentMonth, currency, categories))

            walletAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateIndicator(position: Int) {
        val amount = wallets.size

        indicatorText.text = wallets[position].walletName

        if (position == 0) {
            // position 0 always means "All Wallets"
            indicatorText.text = getString(R.string.all_wallets)

            indicatorLeft.visibility = View.INVISIBLE
        } else {
            indicatorLeft.visibility = View.VISIBLE
        }

        if (position == amount - 1) {
            indicatorRight.visibility = View.INVISIBLE
        } else {
            indicatorRight.visibility = View.VISIBLE
        }
    }
}
