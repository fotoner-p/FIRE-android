package moe.fotone.fire

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_write.*
import moe.fotone.fire.utils.ArticleDTO
import java.text.SimpleDateFormat
import java.util.*


class WriteActivity : AppCompatActivity() {
    private val storage by lazy { FirebaseStorage.getInstance() }
    private val database by lazy { FirebaseFirestore.getInstance()}
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val PICK_IMAGE_FROM_ALBUM = 0
    var photoUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write)

        sendBtn.setOnClickListener{
            articleUpload()
        }
        addPhotoBtn.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_FROM_ALBUM) {
            if(resultCode == Activity.RESULT_OK){
                photoUri = data?.data
                addPhotoImage.setImageURI(data?.data)
                addPhotoImage.visibility = View.VISIBLE
            }

            else{
                finish()
            }
        }
    }

    fun articleUpload(){
        val userRef = database.collection("user").document(auth.currentUser!!.uid)
        userRef.get()
            .addOnSuccessListener { document ->
                val article = ArticleDTO()
                article.uid = auth.currentUser!!.uid
                article.name = document["name"].toString()
                article.main = writeTextMain.text.toString()
                article.timestamp = System.currentTimeMillis()

                if (photoUri != null){
                    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                    val imageFileName = "JPEG_" + timeStamp + "_.png"

                    val storageRef = storage.reference.child("images").child(imageFileName)

                    storageRef
                        .putFile(photoUri!!).addOnSuccessListener {
                            storageRef.downloadUrl.addOnCompleteListener {task ->
                                article.imageUrl = task.result.toString()
                                database.collection("articles").document().set(article)
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                    }
                }
                else {
                    database.collection("articles").document().set(article)
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
    }
}