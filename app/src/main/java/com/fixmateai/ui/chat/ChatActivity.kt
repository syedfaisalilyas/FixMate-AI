package com.fixmateai.ui.chat

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fixmateai.R
import com.fixmateai.data.model.ServiceRequest
import com.fixmateai.data.model.User
import com.fixmateai.databinding.ActivityChatBinding
import com.fixmateai.ui.requests.ReviewActivity
import com.fixmateai.utils.Constants
import com.fixmateai.utils.bindRequestStatus
import com.fixmateai.utils.gone
import com.fixmateai.utils.show
import com.fixmateai.utils.toast
import com.fixmateai.utils.visible
import com.fixmateai.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Shared real-time chat between a customer and a provider for a single service
 * request. Messages update live via Firestore snapshot listeners. The action row
 * adapts to the viewer's role and the request status:
 *  - Provider + Pending  → Accept / Decline
 *  - Provider + Accepted → Mark Complete
 *  - Customer + Completed (unrated) → Leave a Review
 */
@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val viewModel: ChatViewModel by viewModels()

    @Inject lateinit var auth: FirebaseAuth

    private lateinit var senderRole: String
    private lateinit var adapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val requestId = intent.getStringExtra(Constants.EXTRA_REQUEST).orEmpty()
        senderRole = intent.getStringExtra(Constants.EXTRA_PROVIDER) ?: User.ROLE_CUSTOMER
        if (requestId.isBlank()) {
            finish()
            return
        }

        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = ChatAdapter(auth.currentUser?.uid.orEmpty())
        binding.recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        binding.recyclerView.adapter = adapter

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString()
            if (text.isNotBlank()) {
                viewModel.send(senderRole, text)
                binding.etMessage.text?.clear()
            }
        }

        viewModel.bind(requestId)
        observe()
    }

    private fun observe() {
        viewModel.messages.observe(this) { messages ->
            adapter.submitList(messages) {
                if (messages.isNotEmpty()) {
                    binding.recyclerView.scrollToPosition(messages.size - 1)
                }
            }
            binding.emptyView.show(messages.isEmpty())
        }

        viewModel.request.observe(this) { request ->
            if (request != null) bindHeader(request)
        }
    }

    private fun bindHeader(r: ServiceRequest) {
        binding.tvTitle.text = r.title
        binding.tvStatus.bindRequestStatus(r.status)
        binding.tvCost.show(r.aiCostEstimate.isNotBlank())
        binding.tvCost.text = r.aiCostEstimate

        // Title shows the counterparty for quick context.
        val who = if (senderRole == User.ROLE_PROVIDER) r.customerName else r.providerName
        if (who.isNotBlank()) binding.toolbar.subtitle = who

        bindActions(r)
    }

    private fun bindActions(r: ServiceRequest) {
        val isProvider = senderRole == User.ROLE_PROVIDER
        when {
            isProvider && r.status == ServiceRequest.STATUS_PENDING -> {
                binding.actionRow.visible()
                binding.btnPrimary.visible()
                binding.btnSecondary.visible()
                binding.btnPrimary.setText(R.string.accept)
                binding.btnSecondary.setText(R.string.decline)
                binding.btnPrimary.setOnClickListener {
                    viewModel.updateStatus(ServiceRequest.STATUS_ACCEPTED)
                    toast(getString(R.string.request_accepted))
                }
                binding.btnSecondary.setOnClickListener {
                    viewModel.updateStatus(ServiceRequest.STATUS_DECLINED)
                    toast(getString(R.string.request_declined))
                }
            }
            isProvider && r.status == ServiceRequest.STATUS_ACCEPTED -> {
                binding.actionRow.visible()
                binding.btnPrimary.visible()
                binding.btnSecondary.gone()
                binding.btnPrimary.setText(R.string.mark_complete)
                binding.btnPrimary.setOnClickListener {
                    viewModel.updateStatus(ServiceRequest.STATUS_COMPLETED)
                    toast(getString(R.string.request_completed))
                }
            }
            !isProvider && r.status == ServiceRequest.STATUS_COMPLETED && !r.rated -> {
                binding.actionRow.visible()
                binding.btnPrimary.visible()
                binding.btnSecondary.gone()
                binding.btnPrimary.setText(R.string.leave_review)
                binding.btnPrimary.setOnClickListener {
                    val intent = Intent(this, ReviewActivity::class.java)
                    intent.putExtra(Constants.EXTRA_REQUEST, r)
                    startActivity(intent)
                }
            }
            else -> binding.actionRow.gone()
        }
    }
}
