package moe.fotone.fire.utils

import com.company.howl.howlstagram.model.PushDTO
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

class FcmPush() {
    val JSON = MediaType.parse("application/json; charset=utf-8")
    val url = "https://fcm.googleapis.com/fcm/send"
    val serverKey = "AAAARKUlMGM:APA91bG7mh9WvdA-vV7z_TQasJ06Lxi9OjlawWpNL0ClFE3iBxLn5GIzfRym31Xggrr2Zrg7j7XWbYXrA9WIhq_8iE8aVgLwfTiGXc6E5Nv20hKroIjmp5kyJG3mMpsmX1At76IbkALs"

    var okHttpClient: OkHttpClient? = null
    var gson: Gson? = null
    init {
        gson = Gson()
        okHttpClient = OkHttpClient()
    }

    fun sendMessage(destinationUid: String, title: String, message: String) {
        FirebaseFirestore.getInstance().collection("pushtokens").document(destinationUid).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result?.get("pushtoken")?.toString()
                println(token)
                val pushDTO = PushDTO()
                pushDTO.to = token
                pushDTO.notification?.title = title
                pushDTO.notification?.body = message

                val body = RequestBody.create(JSON, gson?.toJson(pushDTO))
                val request = Request
                    .Builder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "key=" + serverKey)
                    .url(url)
                    .post(body)
                    .build()
                okHttpClient?.newCall(request)?.enqueue(object : Callback {
                    override fun onFailure(call: Call?, e: IOException?) {
                    }
                    override fun onResponse(call: Call?, response: Response?) {
                        println(response?.body()?.string())
                    }
                })
            }
        }
    }
}