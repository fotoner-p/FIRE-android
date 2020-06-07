package moe.fotone.fire

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_write.*
import moe.fotone.fire.utils.FirebaseHelper
import java.time.LocalDateTime

class WriteActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private lateinit var name: String

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        database.getReference("/users/").child(currentUser!!.uid).child("name").addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                name = dataSnapshot.value.toString()
            }
        })

        sendBtn.setOnClickListener{
            val base = FirebaseHelper()
            val current = LocalDateTime.now().toString()

            base.createArticle(currentUser!!.uid, name, current, writeTextMain.text.toString())
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}