package com.firebase.pushnotification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.firebase.pushnotification.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    private lateinit var database: FirebaseFirestore
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        database = FirebaseFirestore.getInstance()

        binding.btnAddToFirebase.setOnClickListener {
            binding.loader.visibility = View.VISIBLE
            val name = binding.edtName.text.toString()
            val email = binding.edtEmail.text.toString()

            val data = UserDetailModel(name,email)
            database.collection("user").document().set(data).addOnCompleteListener { task->

                if (task.isSuccessful){
                    Toast.makeText(this, "Data sent successfully", Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
                binding.loader.visibility = View.GONE
            }
        }
    }

    data class UserDetailModel(
        val name: String = "",
        val email: String = "",
    )
}