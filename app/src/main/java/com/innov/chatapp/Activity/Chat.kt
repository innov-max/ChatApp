package com.innov.chatapp.Activity

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.innov.chatapp.Activity.adapter.MessageAdapter
import com.innov.chatapp.R
import com.innov.chatapp.databinding.ActivityChatBinding

class Chat : AppCompatActivity() {
    var binding :ActivityChatBinding? = null
    var adapter : MessageAdapter? = null
    var messages : ArrayList<Message>? = null
    var senderRoom: String? = null
    var recieverRoom: String? = null
    var database :FirebaseDatabase?= null
    var storage : FirebaseStorage?= null
    var dialog: ProgressDialog? = null
    var senderUid: String? = null
    var recieverUid: String? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)

        setContentView(binding!!.root)
        setSupportActionBar(binding!!.toolbar)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        dialog = ProgressDialog(this@Chat)
        dialog!!.setMessage("Uploading Image...")
        dialog!!.setCancelable(false)
        messages = ArrayList()
        val name = intent.getStringExtra("name")
        val profile = intent.getStringExtra("image")
        binding!!.name.text = name
        Glide.with(this@Chat).load(profile)
            .placeholder(R.drawable.image)
            .into(binding!!.profile01)
        binding!!.imageView2.setOnClickListener {
            finish()
        }
        recieverUid = intent.getStringExtra("uid")
        senderUid = FirebaseAuth.getInstance().uid
        database!!.reference.child("Presence").child(recieverUid!!)
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                  if (snapshot.exists()){
                      val status =  snapshot.getValue(String::class.java)
                      if (status == "offline"){
                          binding!!.status.visibility = View.GONE
                      }
                      else{
                          binding!!.status.setText(status)
                          binding!!.status.visibility = View.VISIBLE
                      }
                  }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        senderRoom = senderUid +recieverUid
        recieverRoom = recieverUid + senderUid
        adapter = MessageAdapter(this@Chat, messages,senderRoom!!,recieverRoom!!)
        binding!!.recyclerView.layoutManager = LinearLayoutManager(this@Chat)
        binding!!.recyclerView.adapter = adapter
        database!!.reference.child("chats")
            .child(senderRoom!!)
            .child("message")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages!!.clear()
                    for (snapShot1 in snapshot.children){
                       val message :Message?= snapShot1.getValue(Message::class.java)
                        message!!.messageId = snapShot1.key
                        messages!!.add(messages)

                    }

                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

    }
}