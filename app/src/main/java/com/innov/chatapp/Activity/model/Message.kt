package com.innov.chatapp.Activity.model

class Message {

    var messageId : String? = null
    var senderId : String? = null
    var timeStamp : Long = 0
    var message : String? = null
    var imageUrl : String? = null
    constructor(){}
    constructor(
        messageId : String,
        message: String?,
        senderId : String?,
        timeStamp : Long
    ){
        this.message = message
        this.senderId = senderId
        this.timeStamp = timeStamp
        this.messageId = messageId
    }
}