package com.beatsnake.data.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.beatsnake.domain.*
import io.ktor.auth.jwt.*
import java.util.*

class JwtManager {

    companion object {

        private const val EXPIRATION_INTERVAL = 1 * 60_000

         fun JWTAuthenticationProvider.Configuration.verifyToken() {
            verifier(
                JWT.require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )
        }

        fun JWTAuthenticationProvider.Configuration.validateToken() {
            validate { credential ->
                if (credential.payload.getClaim(USERNAME).asString().isNotEmpty()) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }

    }

    private val expirationDate = Date(System.currentTimeMillis() + EXPIRATION_INTERVAL)

    fun generateToken(name: String, email: String): String = JWT.create()
        .withAudience(audience)
        .withIssuer(issuer)
        .withClaim(USERNAME, name)
        .withClaim(EMAIL, email)
        .withExpiresAt(expirationDate)
        .sign(Algorithm.HMAC256(secret))

}