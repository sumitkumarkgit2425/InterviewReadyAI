package com.example.interviewreadyai.core.auth

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.FragmentActivity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

data class UserProfile(val name: String?, val email: String, val photoUrl: String?)

class AuthManager(private val context: Context) {

    private val credentialManager = CredentialManager.create(context)
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    suspend fun signInWithGoogle(activity: FragmentActivity): UserProfile? {
        val googleIdOption =
                GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(
                                "965561974071-luphqsam6h4gln2scqki4v65ohpq6hk8.apps.googleusercontent.com"
                        )
                        .setAutoSelectEnabled(false)
                        .build()

        val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()

        return try {
            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential

            if (credential is CustomCredential &&
                            credential.type ==
                                    GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                try {
                    val authResult = kotlinx.coroutines.withTimeoutOrNull(15_000L) {
                        firebaseAuth.signInWithCredential(firebaseCredential).await()
                    }
                    if (authResult != null) {
                        val firebaseUser = firebaseAuth.currentUser
                        if (firebaseUser != null) {
                            return UserProfile(
                                    name = firebaseUser.displayName,
                                    email = firebaseUser.email ?: googleIdTokenCredential.id,
                                    photoUrl = firebaseUser.photoUrl?.toString()
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AuthManager", "Firebase auth failed, using fallback: ${e.message}")
                }

                
                UserProfile(
                        name = googleIdTokenCredential.displayName,
                        email = googleIdTokenCredential.id,
                        photoUrl = googleIdTokenCredential.profilePictureUri?.toString()
                )
            } else {
                Log.e("AuthManager", "Unexpected credential type: ${credential.type}")
                null
            }
        } catch (e: GetCredentialException) {
            Log.e("AuthManager", "GetCredentialException: ${e.message}", e)
            null
        } catch (e: Exception) {
            Log.e("AuthManager", "Exception during sign in: ${e.message}", e)
            null
        }
    }

    suspend fun signOut() {
        try {
            firebaseAuth.signOut()
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("AuthManager", "Error signing out: ${e.message}")
        }
    }

    

    fun showBiometricPrompt(
            activity: FragmentActivity,
            onSuccess: () -> Unit,
            onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt =
                BiometricPrompt(
                        activity,
                        executor,
                        object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationError(
                                    errorCode: Int,
                                    errString: CharSequence
                            ) {
                                super.onAuthenticationError(errorCode, errString)
                                onError(errString.toString())
                            }

                            override fun onAuthenticationSucceeded(
                                    result: BiometricPrompt.AuthenticationResult
                            ) {
                                super.onAuthenticationSucceeded(result)
                                onSuccess()
                            }

                            override fun onAuthenticationFailed() {
                                super.onAuthenticationFailed()
                                onError("Authentication failed")
                            }
                        }
                )

        val promptInfo =
                BiometricPrompt.PromptInfo.Builder()
                        .setTitle("App Lock Authentication")
                        .setSubtitle("Log in using your biometric or device credential")
                        .setAllowedAuthenticators(
                                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
                        )
                        .build()

        biometricPrompt.authenticate(promptInfo)
    }

    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        val authenticators =
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
        return biometricManager.canAuthenticate(authenticators) ==
                BiometricManager.BIOMETRIC_SUCCESS
    }
}

