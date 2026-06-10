package com.fixmateai.data.model

import com.google.firebase.firestore.DocumentId

/**
 * A single chat message inside a service request's `messages` subcollection.
 * Powers the real-time conversation between a customer and a provider.
 *
 * [senderId] identifies who sent it (used to decide left/right bubble alignment),
 * and [senderRole] records whether it came from the customer or the provider.
 */
data class ChatMessage(
    @DocumentId val id: String = "",
    val senderId: String = "",
    val senderRole: String = "",
    val text: String = "",
    val timestamp: Long = 0L
)
