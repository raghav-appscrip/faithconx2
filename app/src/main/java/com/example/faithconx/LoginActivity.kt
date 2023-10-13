package com.example.faithconx


import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.faithconx.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private var verificationId: String? = null

    private val auth = FirebaseAuth.getInstance()


    override fun onStart() {
        super.onStart()

        if (FirebaseAuth.getInstance().currentUser != null) {

            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()

        }


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var isEmail = true


        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseAuth.getInstance().firebaseAuthSettings
            .setAppVerificationDisabledForTesting(true)


        val text = resources.getString(R.string.don_t_have_an_account_create_one)
        binding.tvNoAccount.text = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)



        binding.btnPhone.setOnClickListener {
            isEmail = !isEmail
            changeLayout(isEmail)

        }

        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                validateEmailFormat(s.toString())
            }

        })

        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                if (password.isEmpty()) {
                    binding.tilPassword.error = null // Clear error message

                } else if (!isPasswordValid(password)) {
                    binding.tilPassword.error =
                        "Password must contain:\n" + "• At least one uppercase letter (A-Z)\n" + "• At least one special character (!@#\$%^&*)\n" + "• At least one alphanumeric character (0-9 or a-z or A-Z)\n" + "• Minimum length of 8 characters"

                    binding.tilPassword.isErrorEnabled = true
                } else {
                    binding.tilPassword.error = null // Clear error message
                    binding.tilPassword.isErrorEnabled = false
                    binding.btnLogin.isEnabled = true
                    val btnLogincolor = Color.parseColor("#040404")
                    binding.btnLogin.setTextColor(btnLogincolor)
                }
            }



        })

        binding.btnLogin.setOnClickListener {

            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            val progressDialog = ProgressDialog(this@LoginActivity)
            progressDialog.setTitle("Login")
            progressDialog.setMessage("Please wait,this may take a while..")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()


            val mAuth = FirebaseAuth.getInstance()
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    progressDialog.dismiss()
                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                } else {
                    val message = task.exception!!
                    Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
                    FirebaseAuth.getInstance().signOut()
                    progressDialog.dismiss()


                }
            }
        }







        binding.tvNoAccount.setOnClickListener {
            startActivity(Intent(this@LoginActivity, SignUpActivity::class.java))
        }



        val phoneAuthCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                Log.d("phoneLogin", "onVerificationCompleted")
                Toast.makeText(applicationContext,"onVerificationCompleted", Toast.LENGTH_SHORT).show()
                phoneAuthCredential.let {
                    signInWithPhoneAuthCredential(it)
                }
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                Log.d("phoneLogin", "onVerificationFailed: ${exception.message}")
                Toast.makeText(applicationContext, exception.message!!, Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(
                verificationId: String, token: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(verificationId, token)
                Log.d("phoneLogin", "onCodeSent")
                Toast.makeText(applicationContext,"Code request sent successfully !", Toast.LENGTH_SHORT).show()
                this@LoginActivity.verificationId = verificationId
            }

        }

        binding.tvGetOtp.setOnClickListener {

            binding.etOTP.requestFocus()

            val vrfyColor = Color.parseColor("#040404")
            val newColor = Color.parseColor("#858AA8")

            binding.etOTP.isEnabled = true
            binding.btnVerify.isEnabled = true
            binding.btnVerify.setTextColor(vrfyColor)
            binding.tvGetOtp.setTextColor(newColor)
            val phone = binding.etPhone.text.toString().trim()
            if (phone.isEmpty() || phone.length != 10) {
                binding.etPhone.error = "Enter a Valid Phone Number"
                binding.etPhone.requestFocus()
                return@setOnClickListener
            }

            val phoneNumber = binding.CountryCodePicker.selectedCountryCodeWithPlus + phone

            PhoneAuthProvider.getInstance()
                .verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, this, phoneAuthCallbacks)


        }



        binding.btnVerify.setOnClickListener {

            val code = binding.etOTP.text.toString().trim()
            if (code.isEmpty()) {

                binding.etOTP.error = "OTP required"
                binding.etOTP.requestFocus()
                return@setOnClickListener
            }




            verificationId?.let {
                val credential = PhoneAuthProvider.getCredential(it, code)
                signInWithPhoneAuthCredential(credential)
            }
        }


    }



    fun onCloseButtonClick(view: View) {
        finish()
    }

    private fun validateEmailFormat(email: String) {
        if (email.isNotEmpty() && !isValidUserEmail(email)) {
            binding.tilEmail.error = "Invalid email format"
        } else {
            binding.tilEmail.error = null
        }
    }

    private fun isValidUserEmail(email: String): Boolean {
        val pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }

    private fun isPasswordValid(password: String): Boolean {

        val pattern = "^(?=.*[A-Z])(?=.*[!@#\$%^&*])(?=.*[0-9a-zA-Z]).{8,}$".toRegex()

        return pattern.matches(password)

    }







    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {


       auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    Toast.makeText(applicationContext,"signInWithCredential Success", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))


                    val user = task.result?.user
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(applicationContext,"Invalid credential , please enter correct otp", Toast.LENGTH_SHORT).show()
                    }

                }
            }
    }



    private fun changeLayout(isEmail: Boolean) {

        if (isEmail) {

            binding.tilEmail.visibility = View.VISIBLE
            binding.tilPhone.visibility = View.GONE
            binding.tilPassword.visibility = View.VISIBLE
            binding.tilOTP.visibility = View.GONE
            binding.tvForgotPassword.visibility = View.VISIBLE
            binding.tvGetOtp.visibility = View.GONE
            binding.btnLogin.visibility = View.VISIBLE
            binding.btnVerify.visibility = View.GONE
            binding.CountryCodePicker.visibility = View.GONE
            binding.btnPhone.text = getString(R.string.login_with_phone_number)
            binding.btnPhone.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.baseline_local_phone_24,
                0,
                0,
                0
            )
            binding.etPhone.text?.clear()
            binding.etOTP.text?.clear()


        } else {

            binding.tilEmail.visibility = View.GONE
            binding.tilPhone.visibility = View.VISIBLE
            binding.tilPassword.visibility = View.GONE
            binding.tilOTP.visibility = View.VISIBLE
            binding.tvForgotPassword.visibility = View.GONE
            binding.tvGetOtp.visibility = View.VISIBLE
            binding.btnLogin.visibility = View.GONE
            binding.btnVerify.visibility = View.VISIBLE
            binding.CountryCodePicker.visibility = View.VISIBLE
            binding.btnPhone.text = getString(R.string.login_with_email)
            binding.btnPhone.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.baseline_email_24,
                0,
                0,
                0
            )

            binding.etEmail.text?.clear()
            binding.etPassword.text?.clear()

        }


    }
}