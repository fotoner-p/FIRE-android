package moe.fotone.fire

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_join.*
import java.lang.Exception

class JoinActivity : AppCompatActivity() {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val database by lazy { FirebaseFirestore.getInstance()}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)

        registerBtn.setOnClickListener {
            val email = joinEmailText.text.toString()
            val name = joinNameText.text.toString()
            val pwd = joinPwdText.text.toString()
            val repwd = joinRePwdText.text.toString()

            if (pwd != repwd){
                Toast.makeText(this,"패스워드가 서로 틀립니다" , Toast.LENGTH_SHORT).show()
            } else if (pwd.isEmpty() || repwd.isEmpty() || name.isEmpty()){
                Toast.makeText(this,"누락이 존재합니다" , Toast.LENGTH_SHORT).show()
            }else {
                joinStart(email, name, pwd)
            }
        }
    }

    private fun joinStart(email: String, name: String, pwd: String){
        auth.createUserWithEmailAndPassword(email, pwd)
            .addOnCompleteListener { task ->
                if(!task.isSuccessful){
                    try {
                        throw task.exception!!
                    } catch (e: FirebaseAuthWeakPasswordException){
                        Toast.makeText(this,"비밀번호가 간단합니다" ,Toast.LENGTH_SHORT).show()
                    } catch (e: FirebaseAuthInvalidCredentialsException){
                        Toast.makeText(this,"Email 형식에 맞지 않습니다" ,Toast.LENGTH_SHORT).show()
                    } catch (e: FirebaseAuthUserCollisionException){
                        Toast.makeText(this,"이미 존재하는 Email 입니다" ,Toast.LENGTH_SHORT).show()
                    } catch(e: Exception){
                        Toast.makeText(this,"다시 확인해주세요" ,Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val user = hashMapOf(
                        "name" to name,
                        "email" to email
                    )

                    database.collection("user")
                        .document(auth.currentUser!!.uid)
                        .set(user)
                        .addOnSuccessListener {
                            Log.d(ContentValues.TAG, "DocumentSnapshot successfully written!")
                            Toast.makeText(this,"가입 성공" ,Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                                e -> Log.w(ContentValues.TAG, "Error writing document", e)
                        }
                }
            }
    }
}