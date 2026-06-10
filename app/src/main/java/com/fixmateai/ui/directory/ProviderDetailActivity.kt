package com.fixmateai.ui.directory

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fixmateai.R
import com.fixmateai.data.model.ServiceProvider
import com.fixmateai.databinding.ActivityProviderDetailBinding
import com.fixmateai.ui.requests.CreateRequestActivity
import com.fixmateai.utils.Constants
import com.fixmateai.utils.Resource
import com.fixmateai.utils.show
import com.fixmateai.viewmodel.ProviderViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Shows a provider's public profile (trade, rating, bio, verified badge) and
 * their reviews, with a "Request Service" button that starts a new request.
 * May carry a diagnosis summary/cost forward into the request.
 */
@AndroidEntryPoint
class ProviderDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProviderDetailBinding
    private val viewModel: ProviderViewModel by viewModels()
    private val reviewAdapter = ReviewAdapter()

    private var provider: ServiceProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProviderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        provider = intent.getParcelableExtra(Constants.EXTRA_PROVIDER)
        val p = provider
        if (p == null) {
            finish()
            return
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.recyclerReviews.layoutManager = LinearLayoutManager(this)
        binding.recyclerReviews.adapter = reviewAdapter

        bindProvider(p)

        binding.btnRequest.setOnClickListener {
            val intent = Intent(this, CreateRequestActivity::class.java).apply {
                putExtra(Constants.EXTRA_PROVIDER, p)
                putExtra(Constants.EXTRA_DIAGNOSIS_SUMMARY,
                    getIntent().getStringExtra(Constants.EXTRA_DIAGNOSIS_SUMMARY).orEmpty())
            }
            startActivity(intent)
        }

        observe()
        viewModel.loadProviderDetail(p.uid)
    }

    private fun bindProvider(p: ServiceProvider) {
        binding.tvName.text = p.name
        binding.tvTrade.text = p.trade
        binding.ivVerified.show(p.verified)
        binding.tvBio.text = p.bio.ifBlank { "This pro hasn't added a bio yet." }

        val parts = mutableListOf("★ ${p.ratingLabel}")
        if (p.experienceYears > 0) parts.add(getString(R.string.experience_years, p.experienceYears))
        if (p.rate.isNotBlank()) parts.add(p.rate)
        if (p.city.isNotBlank()) parts.add(p.city)
        binding.tvMeta.text = parts.joinToString("   •   ")
    }

    private fun observe() {
        // Refresh with the freshest copy (ratings may have changed).
        viewModel.providerDetail.observe(this) { state ->
            if (state is Resource.Success) bindProvider(state.data)
        }
        viewModel.reviews.observe(this) { state ->
            if (state is Resource.Success) {
                reviewAdapter.submitList(state.data)
                binding.tvNoReviews.show(state.data.isEmpty())
            }
        }
    }
}
