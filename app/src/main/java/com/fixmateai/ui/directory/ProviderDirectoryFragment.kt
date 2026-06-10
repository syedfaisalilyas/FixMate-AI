package com.fixmateai.ui.directory

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.fixmateai.data.model.ServiceProvider
import com.fixmateai.databinding.FragmentProviderDirectoryBinding
import com.fixmateai.utils.Constants
import com.fixmateai.utils.Resource
import com.fixmateai.utils.show
import com.fixmateai.utils.toast
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint

/**
 * Customer "Find a Pro" directory. Lists signed-up providers with trade filter
 * chips and a client-side name/trade search. Tapping a provider opens their
 * detail page. May be launched with a trade filter pre-selected (from a
 * diagnosis "Send to a Pro" action).
 */
@AndroidEntryPoint
class ProviderDirectoryFragment : Fragment() {

    private var _binding: FragmentProviderDirectoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: com.fixmateai.viewmodel.ProviderViewModel by viewModels()

    private var allProviders: List<ServiceProvider> = emptyList()
    private var selectedTrade: String? = null

    private val adapter = ProviderAdapter { provider ->
        val intent = Intent(requireContext(), ProviderDetailActivity::class.java)
        intent.putExtra(Constants.EXTRA_PROVIDER, provider)
        // Carry a diagnosis summary forward (set when opened via "Send to a Pro").
        arguments?.getString(Constants.EXTRA_DIAGNOSIS_SUMMARY)?.let {
            intent.putExtra(Constants.EXTRA_DIAGNOSIS_SUMMARY, it)
        }
        startActivity(intent)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProviderDirectoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        binding.swipeRefresh.setOnRefreshListener { viewModel.loadDirectory() }

        // Optional incoming trade filter (e.g. from a diagnosis).
        selectedTrade = arguments?.getString(Constants.EXTRA_TRADE_FILTER)

        buildChips()
        setupSearch()
        observe()
        viewModel.loadDirectory()
    }

    private fun buildChips() {
        binding.chipGroup.removeAllViews()
        val trades = listOf(getString(com.fixmateai.R.string.all_trades)) + Constants.TRADES
        trades.forEach { label ->
            val chip = Chip(requireContext()).apply {
                text = label
                isCheckable = true
                isChecked = (label == selectedTrade) ||
                    (selectedTrade == null && label == getString(com.fixmateai.R.string.all_trades))
                setOnClickListener {
                    selectedTrade = if (label == getString(com.fixmateai.R.string.all_trades)) null else label
                    applyFilters()
                }
            }
            binding.chipGroup.addView(chip)
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(
            afterTextChanged = { applyFilters() }
        )
    }

    private fun applyFilters() {
        val query = binding.etSearch.text.toString().trim().lowercase()
        val filtered = allProviders.filter { p ->
            (selectedTrade == null || p.trade == selectedTrade) &&
                (query.isBlank() ||
                    p.name.lowercase().contains(query) ||
                    p.trade.lowercase().contains(query))
        }
        adapter.submitList(filtered)
        binding.emptyView.show(filtered.isEmpty())
    }

    private fun observe() {
        viewModel.directory.observe(viewLifecycleOwner) { state ->
            binding.swipeRefresh.isRefreshing = state is Resource.Loading
            when (state) {
                is Resource.Success -> {
                    allProviders = state.data
                    applyFilters()
                }
                is Resource.Error -> {
                    toast(state.message)
                    binding.emptyView.show(true)
                }
                else -> Unit
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadDirectory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
