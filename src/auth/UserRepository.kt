package org.camuthig.auth

import org.camuthig.auth.social.SocialIdentity

interface UserRepository {
    /**
     * Get a user by the ID
     */
    fun getUser(id: Int): User?

    /**
     * Get a user by their identifying email address
     */
    fun getUserByEmail(email: String): User?

    /**
     * Add a given user to the repository
     */
    fun addUser(user: User)

    /**
     * Find the user related to a given social identity
     */
    fun getUser(provider: String, socialIdentity: SocialIdentity): User?

    /**
     * Add the identity as a user, by either linking to an existing user or creating a new user
     */
    fun linkIdentity(provider: String, socialIdentity: SocialIdentity): User
}