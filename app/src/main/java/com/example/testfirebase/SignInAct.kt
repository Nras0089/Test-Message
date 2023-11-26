package com.example.testfirebase

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.testfirebase.databinding.ActivitySignInBinding
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.example.testfirebase.AnotherActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SignInAct : AppCompatActivity() {
    lateinit var launcher: ActivityResultLauncher<Intent>
    lateinit var auth: FirebaseAuth
    lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        auth.currentUser
        launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    if (account != null) {
                        firebaseAuthWithGoogle(account.idToken!!)
                    }
                } catch (e: ApiException) {
                    Log.d("MyLog", "API Exception: ${e.message}")
                }
            }
        binding.bSignIn.setOnClickListener {
            signInWithGoogle()
        }

        checkAuthState()
    }

    private fun getClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(this, gso)
    }

    private fun signInWithGoogle() {
        val signInClient = getClient()
        launcher.launch(signInClient.signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener() {
            if (it.isSuccessful) {
                Log.d("MyLog", "Google sign-in successful")
                // Пользователь успешно вошел, теперь мы можем инициализировать его в Firebase Database
                val user = auth.currentUser
                initializeUserInDatabase(user?.uid)

                checkAuthState()
            } else {
                Log.d("MyLog", "Google sign-in error: ${it.exception?.message}")
            }
        }
    }

    private fun initializeUserInDatabase(userUID: String?) {
        userUID ?: return

        val databaseReference = FirebaseDatabase.getInstance().getReference("users")
        databaseReference.child(userUID).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // Если пользователь не существует, создаем новую запись
                    databaseReference.child(userUID).setValue(UserAccount(balance = 0))
                }
                // Если пользователь уже существует, ничего не делаем
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Обработка ошибок
            }
        })
    }

    data class UserAccount(val balance: Int = 0)

    private fun checkAuthState() {
        Log.d("MyLog", "Checking auth state")
        if (auth.currentUser != null) {
            Log.d("MyLog", "User is authenticated, navigating to AnotherActivity")
            val intent = Intent(this, AnotherActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Log.d("MyLog", "User is not authenticated")
        }
    }
}
