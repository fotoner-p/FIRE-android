package moe.fotone.fire

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val database by lazy { FirebaseFirestore.getInstance()}
    private val storage by lazy { FirebaseStorage.getInstance() }
    private val home: FragmentHome by lazy { FragmentHome() }
    private val dashboard: FragmentUser by lazy { FragmentUser() }
    private val notifications: FragmentNotifications by lazy { FragmentNotifications() }
    private val PICK_PROFILE_FROM_ALBUM = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)

        val fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content, home).commitAllowingStateLoss()

        mainNavigation.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.navigation_home -> {
                    replaceFragment(home)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_dashboard -> {
                    replaceFragment(dashboard)
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.navigation_notifications -> {
                    replaceFragment(notifications)
                    return@setOnNavigationItemSelectedListener true
                }
            }
            return@setOnNavigationItemSelectedListener false
        }
    }

    override fun onStart() {
        super.onStart()

        if(auth.currentUser == null){
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_PROFILE_FROM_ALBUM && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data

            val uid = auth.currentUser!!.uid
            val storageRef = storage.reference.child("userProfileImages").child(uid)

            storageRef
                .putFile(imageUri!!)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnCompleteListener {task->
                        if (task.isSuccessful) {
                            val url = task.result.toString()
                            println(storageRef.downloadUrl)
                            val map = HashMap<String, Any>()
                            map["image"] = url
                            database.collection("profileImages").document(uid).set(map)
                        }
                    }
                }
        }
    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content, fragment).commitAllowingStateLoss();
    }
}