package com.innov.chatapp.Activity

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import com.innov.chatapp.Activity.model.Message
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
import java.util.Calendar
import java.util.Date

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
                        messages!!.add(message)

                    }
                        adapter!!.notifyDataSetChanged()

                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
        binding!!.sendBtn.setOnClickListener {
            val messageTxt : String = binding!!. messageBox.text.toString()
            val date = Date()
            val message = Message(messageTxt,senderUid,date.time)
            binding!!.messageBox.setText("")
            val randomkey = database!!.reference.push().key
            val lastMsObj = HashMap<String,Any>()
            lastMsObj["lastMsg"] = message.message!!
            lastMsObj["lastMsg"] = date.time
            database!!.reference.child("chats").child(senderRoom!!)
                .updateChildren(lastMsObj)
            database!!.reference.child("chats").child(recieverRoom!!)
                .updateChildren(lastMsObj)
            database!!.reference.child("chats").child(senderRoom!!)
                .child("messages")
                .child(randomkey!!)
                .setValue(message).addOnSuccessListener {
                    database!!.reference.child("chats")
                        .child(recieverRoom!!)
                        .child("message")
                        .child(randomkey)
                        .setValue(message)
                        .addOnSuccessListener {  }

                }


        }
        binding!!.attachment.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent,25)

        }
        val handler = Handler()
        binding!!.messageBox.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
               database!!.reference.child("Presence")
                   .child(senderUid!!)
                   .setValue("typing...")
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStoppedTyping, 1000)
            }
            var userStoppedTyping = Runnable {
                database!!.reference.child("Presence")
                    .child(senderUid!!)
                    .setValue("Online")
            }

        })
        supportActionBar!!.setDisplayShowTitleEnabled(false)



    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 25){
            if (data != null){
                if (data.data != null){
                   val selectedImage = data.data
                   val calendar = Calendar.getInstance()
                    val refence = storage!!.reference.child("chats")
                        .child(calendar.timeInMillis.toString() + "")
                    dialog!!.show()
                    refence.putFile(selectedImage!!).addOnCompleteListener{task->
                        dialog!!.dismiss()
                        if (task.isSuccessful){
                            refence.downloadUrl.addOnSuccessListener {uri ->
                                val filePath = uri.toString()
                                val messageTxt : String = binding!!.messageBox.text.toString()
                                val date = Date()
                                val message = Message(messageTxt,senderUid,date.time)
                                message.message = "photo"
                                message.imageUrl = filePath
                                binding!!.messageBox.setText("")
                                val randomkey = database!!.reference.push().key
                                val lastMsgObj = HashMap<String, Any>()
                                lastMsgObj["lastMsg"] = message.message!!
                                lastMsgObj["lastMsgTime"] = date.time
                                database!!.reference.child("chats")
                                    .updateChildren(lastMsgObj)
                                database!!.reference.child("child")
                                    .child(recieverRoom!!)
                                    .updateChildren(lastMsgObj)
                                database!!.reference.child("chats")
                                    .child("messages")
                                    .child(randomkey!!)
                                    .setValue(message).addOnSuccessListener {
                                        database!!.reference.child("chats")
                                            .child(recieverRoom!!)
                                            .child("messages")
                                            .child(randomkey)
                                            .setValue(message)
                                            .addOnSuccessListener {

                                            }
                                    }


                            }
                        }

                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("Presence")
            .child(currentId!!)
            .setValue("Online")
    }

    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("Presence")
            .child(currentId!!)
            .setValue("offline")
    }
}