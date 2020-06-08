package moe.fotone.fire

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_write.*
import moe.fotone.fire.utils.FirebaseHelper

class WriteActivity : AppCompatActivity() {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write)

        sendBtn.setOnClickListener{
            val base = FirebaseHelper()

            base.createArticle(auth.currentUser!!.uid, writeTextMain.text.toString())
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}