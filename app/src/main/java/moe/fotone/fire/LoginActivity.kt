package moe.fotone.fire

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*
import java.lang.Exception

class LoginActivity : AppCompatActivity(){
    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        inBtn.setOnClickListener {
            val email = loginEmailText.text.toString()
            val pwd = loginPwdText.text.toString()

            loginApp(email, pwd)
        }
        upBtn.setOnClickListener {
            startActivity(Intent(this, JoinActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        currentUser = auth.currentUser
        if(currentUser != null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun loginApp(email:String, pwd:String){
        auth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener{ task ->
            if(!task.isSuccessful){
                try {
                    throw task.exception!!;
                } catch (e: FirebaseAuthInvalidUserException){
                     Toast.makeText(this,"존재하지 않는 id 입니다" ,Toast.LENGTH_SHORT).show()
                } catch (e: FirebaseAuthInvalidCredentialsException){
                    Toast.makeText(this,"이메일 형식이 맞지 않습니다" ,Toast.LENGTH_SHORT).show()
                } catch (e: FirebaseNetworkException){
                    Toast.makeText(this,"Firebase network error" ,Toast.LENGTH_SHORT).show()
                } catch (e: Exception){
                    Toast.makeText(this,"Unknown error" ,Toast.LENGTH_SHORT).show()
                }
            }else{
                currentUser = auth.currentUser
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}