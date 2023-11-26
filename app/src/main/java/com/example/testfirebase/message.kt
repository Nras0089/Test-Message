package com.example.testfirebase

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testfirebase.databinding.ActivityMainBinding
import com.example.testfirebase.databinding.ActivityMessageBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.functions.FirebaseFunctions

class message : AppCompatActivity() {
    lateinit var binding: ActivityMessageBinding
    lateinit var auth: FirebaseAuth
    lateinit var adapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация FirebaseAuth
        auth = FirebaseAuth.getInstance()


        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("users")
        binding.bSend.setOnClickListener(){
            reference.child(reference.push().key ?: "Neizvestno").setValue(User(auth.currentUser?.displayName, binding.edMessage.text.toString()))

        }

        onChangeListaner(reference)
        initRcView()
    }

    private fun initRcView() = with(binding){
        adapter = UserAdapter()
        rcView.layoutManager= LinearLayoutManager(this@message)
        rcView.adapter = adapter
    }

    private fun onChangeListaner(dRef: DatabaseReference){
        dRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = ArrayList<User>()

                for (s in snapshot.children){
                    val user = s.getValue(User::class.java)
                    if (user != null)list.add(user)
                    list
                }
                adapter.submitList(list)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

}