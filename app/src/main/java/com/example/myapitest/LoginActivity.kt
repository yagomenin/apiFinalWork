package com.example.myapitest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapitest.databinding.ActivityLoginBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private var verificationId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        setupView()
    }


    fun setupView() {

        binding.btSendSms.setOnClickListener() {
            sendVerificationCode()
        }

        binding.btVerifySms.setOnClickListener() {
            verifySmsCode()
        }

        prefixAndLenthNumerPhone()
    }

    fun prefixAndLenthNumerPhone() {
        binding.cellphone.length()
        binding.cellphone.setText("+55")

        binding.cellphone.setSelection(binding.cellphone.text!!.length)
        binding.cellphone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {

                if (text != null) {
                    if (!text.toString().startsWith("+55")) {
                        binding.cellphone.removeTextChangedListener(this)
                        binding.cellphone.setText("+55")
                        binding.cellphone.setSelection(binding.cellphone.text!!.length)
                        binding.cellphone.addTextChangedListener(this)
                    }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    fun sendVerificationCode() {
        val phoneNumber = binding.cellphone.text.toString()
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifySmsCode () {
        val verificationCode = binding.verifySms.text.toString()
        val credential = PhoneAuthProvider.getCredential(verificationId, verificationCode)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            onCredentialComplete(task)
        }
    }
    fun onCredentialComplete(task: Task<AuthResult>) {
        if (task.isSuccessful) {
            val user = auth.currentUser
            Log.d(TAG_LOGIN, "userLoginIsSucess, ${user?.uid}")
            startActivity(
                MainActivity.newIntent(this)
            )
        } else {
            Toast.makeText(this@LoginActivity, "Erro ao completar o login",
                Toast.LENGTH_SHORT).show()
        }
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            auth.signInWithCredential(credential)
                .addOnCompleteListener() { task ->
                    onCredentialComplete(task)
                }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Toast.makeText(this@LoginActivity, "ERROR, ${e}",
                Toast.LENGTH_SHORT).show()
        }

        override fun onCodeSent(verificationId: String, Token: PhoneAuthProvider.ForceResendingToken) {
            this@LoginActivity.verificationId = verificationId
            Toast.makeText(this@LoginActivity,
                "Código de verificação enviado",
                Toast.LENGTH_SHORT)
                .show()
            binding.itVerifySms.visibility = View.VISIBLE
            binding.btVerifySms.visibility = View.VISIBLE
        }
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, LoginActivity::class.java)
        val TAG_LOGIN: String = "LOGIN"
    }
}