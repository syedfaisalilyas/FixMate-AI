package com.fixmateai.data.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize

/**
 * A service request a customer sends to a provider, stored in the Firestore
 * `service_requests` collection. Both parties (customer + provider) read and
 * update the same document, and the in-app chat lives in its `messages`
 * subcollection.
 *
 * Names/trade are denormalized onto the document so list screens can render
 * without extra lookups (and so Firestore rules stay simple).
 */
@Parcelize
data class ServiceRequest(
    @DocumentId val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val providerId: String = "",
    val providerName: String = "",
    val trade: String = "",
    val title: String = "",
    val description: String = "",
    /** Optional local image path carried over from a saved diagnosis. */
    val imageUrl: String = "",
    /** Optional one-line AI diagnosis summary that seeded this request. */
    val diagnosisSummary: String = "",
    /** Optional AI-generated price range, e.g. "$120 – $200". */
    val aiCostEstimate: String = "",
    val status: String = STATUS_PENDING,
    /** True once the customer has left a review for a completed job. */
    val rated: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) : Parcelable {

    companion object {
        const val STATUS_PENDING = "Pending"
        const val STATUS_ACCEPTED = "Accepted"
        const val STATUS_DECLINED = "Declined"
        const val STATUS_COMPLETED = "Completed"
    }
}
