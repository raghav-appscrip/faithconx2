package com.example.faithconx

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.faithconx.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val textTermsandPolicies =
            resources.getString(R.string.i_accept_terms_and_conditions_and_n_privacy_policies)
        binding.tvTermsAndConditions.text =
            Html.fromHtml(textTermsandPolicies, Html.FROM_HTML_MODE_LEGACY)


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


                validatePasswords()
            }


        })

        binding.etConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                validatePasswords()
            }
        })


    }

    fun validatePasswords() {
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (password.isEmpty() && confirmPassword.isEmpty()) {
            // Clear error messages if both fields are empty
            binding.tilPasswordSignUp.error = null
            binding.tilConfirmPasswordSignUp.error = null
        } else if (password != confirmPassword) {
            // Passwords do not match
            binding.tilPasswordSignUp.error =
                "Passwords do not match" // Clear error message for password
            binding.tilConfirmPasswordSignUp.error = "Passwords do not match"
            binding.tilConfirmPasswordSignUp.isErrorEnabled = true
        } else if (!isPasswordValid(password)) {
            // Password is not valid
            binding.tilPasswordSignUp.error =
                "Password must contain:\n" + "• At least one uppercase letter (A-Z)\n" + "• At least one special character (!@#\$%^&*)\n" + "• At least one alphanumeric character (0-9 or a-z or A-Z)\n" + "• Minimum length of 8 characters"
            binding.tilPasswordSignUp.isErrorEnabled = true
        } else {
            // Passwords match and are valid
            binding.tilPasswordSignUp.error = null
            binding.tilConfirmPasswordSignUp.error = null
            binding.tilPasswordSignUp.isErrorEnabled = false
            binding.tilConfirmPasswordSignUp.isErrorEnabled = false
        }

        //password validation done



        //adding user to the firebase

        binding.btnContinue.setOnClickListener {
            createAccount()
        }








    }

    private fun createAccount() {
        val fName = binding.firstNameEditText.text.toString().trim()
        val lname = binding.lastNameEditText.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        val progressDialog = ProgressDialog(this@SignUpActivity)
        progressDialog.setTitle("SignUp")
        progressDialog.setMessage("Please wait this may take a while")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()

        val mAuth = FirebaseAuth.getInstance()
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {task ->
                if (task.isSuccessful){
                    saveUserInfo(fName,lname,email,password,progressDialog)
                    Log.d("saveduserinfo","saveduserinfo")
                }else{
                    val message = task.exception!!
                    Toast.makeText(this,"Error: $message", Toast.LENGTH_LONG).show()
                    mAuth.signOut()
                    progressDialog.dismiss()
                }

            }








    }

    private fun saveUserInfo(
        fName: String,
        lname: String,
        email: String,
        password: String,
        progressDialog: ProgressDialog
    ) {
        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val userRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")
        val userMap = HashMap<String,Any>()
        userMap["uid"] = currentUserId
        userMap["fName"] = fName
        userMap["lName"] = lname
        userMap["email"] = email
        userMap["password"] = password

        userRef.child(currentUserId).setValue(userMap)
            .addOnCompleteListener {task ->
                if (task.isSuccessful){
                    progressDialog.dismiss()
                    Log.d("usersaved","usersaved")
                    Toast.makeText(this,"User Registration Successful",Toast.LENGTH_LONG).show()

                    val intent = Intent(this@SignUpActivity,HomeActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()

                }else{
                    val message = task.exception!!
                    Toast.makeText(this,"Error: $message",Toast.LENGTH_LONG).show()
                    FirebaseAuth.getInstance().signOut()
                    progressDialog.dismiss()
                }


            }

    }


    private fun isPasswordValid(password: String): Boolean {

        val pattern = "^(?=.*[A-Z])(?=.*[!@#\$%^&*])(?=.*[0-9a-zA-Z]).{8,}$".toRegex()

        return pattern.matches(password)

    }

    private fun validateEmailFormat(email: String) {
        if (email.isNotEmpty() && !isValidUserEmail(email)) {
            binding.tilEmailSignUp.error = "Invalid email format"
        } else {
            binding.tilEmailSignUp.error = null
        }
    }

    private fun isValidUserEmail(email: String): Boolean {
        val pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }

    fun onCloseButtonClickSignUp(view: View) {
        finish()
    }
}