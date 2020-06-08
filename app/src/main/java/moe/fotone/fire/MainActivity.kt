package moe.fotone.fire

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val home: FragmentHome by lazy { FragmentHome() }
    private val dashboard: FragmentDashboard by lazy { FragmentDashboard() }
    private val  notifications: FragmentNotifications by lazy { FragmentNotifications() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        if(auth.currentUser != null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
    private fun replaceFragment(fragment: Fragment){
        val fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content, fragment).commitAllowingStateLoss();
    }
}