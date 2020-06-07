package moe.fotone.fire

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var home: FragmentHome
    private lateinit var dashboard: FragmentDashboard
    private lateinit var notifications: FragmentNotifications

    private var currentUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        home = FragmentHome()
        dashboard = FragmentDashboard()
        notifications = FragmentNotifications()

        auth = FirebaseAuth.getInstance()

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

    private fun replaceFragment(fragment: Fragment){
        val fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content, fragment).commitAllowingStateLoss();
    }
}