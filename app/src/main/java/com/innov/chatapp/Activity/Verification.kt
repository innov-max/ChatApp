package com.innov.chatapp.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.innov.chatapp.R
import com.innov.chatapp.databinding.ActivityMainBinding
import com.innov.chatapp.databinding.ActivityVerificationBinding

class Verification : AppCompatActivity() {

    var binding :ActivityVerificationBinding? = null
    var auth : FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        auth = FirebaseAuth.getInstance()
        if (auth!!.currentUser != null){
            val intent = Intent(this@Verification,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        supportActionBar?.hide()
        binding!!.editNumber.requestFocus()
        binding!!.continueBtn.setOnClickListener {
            val intent = Intent(this@Verification,OTPActivity::class.java)
            intent.putExtra("phoneNumber",binding!!.editNumber.text.toString())
            startActivity(intent)

        }
    }
}