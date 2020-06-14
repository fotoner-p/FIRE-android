package moe.fotone.fire

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val database by lazy { FirebaseFirestore.getInstance()}
    private val storage by lazy { FirebaseStorage.getInstance() }
    //private var beforeSelected: Int = R.id.navigation_home
    private val fragmentMap: HashMap<Int, Fragment?> = HashMap()
    private val PICK_PROFILE_FROM_ALBUM = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainProgressBar.visibility = View.VISIBLE
        /*
        fragmentMap[R.id.navigation_home] = FragmentHome()
        fragmentMap[R.id.navigation_dashboard] = FragmentDashboard()
        fragmentMap[R.id.navigation_notifications] = FragmentNotifications()
        fragmentMap[R.id.navigation_account] = FragmentUser()


        val bundle = Bundle()
        bundle.putString("destinationUid", auth.currentUser!!.uid)
        fragmentMap[R.id.navigation_account]?.arguments = bundle
        */

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            1
        )

        mainNavigation.setOnNavigationItemSelectedListener(this)
        mainNavigation.selectedItemId = R.id.navigation_home

        registerPushToken()
        mainProgressBar.visibility = View.VISIBLE
    }

    override fun onStart() {
        super.onStart()

        if(auth.currentUser == null){
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.navigation_home -> {
                supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.content, FragmentHome())
                    .commitAllowingStateLoss()

                return true
            }
            R.id.navigation_dashboard -> {
                supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.content, FragmentDashboard())
                    .commitAllowingStateLoss()

                return true
            }
            R.id.navigation_notifications -> {
                supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.content, FragmentNotifications())
                    .commitAllowingStateLoss()

                return true
            }
            R.id.navigation_account -> {
                supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                val fragment = FragmentUser()
                val bundle = Bundle()
                bundle.putString("destinationUid", auth.currentUser!!.uid)
                fragment.arguments = bundle
                supportFragmentManager.beginTransaction()
                    .replace(R.id.content, fragment)
                    .commitAllowingStateLoss()

                return true
            }
        }

        return false
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
                            val map = HashMap<String, Any>()
                            map["image"] = url
                            database.collection("profileImages").document(uid).set(map)
                        }
                    }
                }
        }
    }

    fun registerPushToken(){
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
            val pushToken = task.result?.token
            val uid = auth.currentUser?.uid
            val map = mutableMapOf<String,Any>()
            map["pushtoken"] = pushToken!!
            database.collection("pushtokens").document(uid!!).set(map)
        }
    }
}