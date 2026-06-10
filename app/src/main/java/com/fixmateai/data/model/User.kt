package com.fixmateai.data.model

/**
 * Represents a user profile stored in the Firestore `users` collection.
 *
 * Firestore needs a no-arg constructor to deserialize documents, which Kotlin
 * provides automatically because every field has a default value.
 */
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val photoUrl: String = "",
    /**
     * Account type: [ROLE_CUSTOMER] (homeowner who diagnoses repairs) or
     * [ROLE_PROVIDER] (tradesperson who receives and answers service requests).
     * Chosen at sign-up and used to route the user to the correct dashboard.
     */
    val role: String = ROLE_CUSTOMER,
    val createdAt: Long = 0L
) {
    companion object {
        const val ROLE_CUSTOMER = "customer"
        const val ROLE_PROVIDER = "provider"
    }
}
