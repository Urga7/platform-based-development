package si.uni_lj.fri.pbd.lab2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import si.uni_lj.fri.pbd.lab2.databinding.ActivityProfileBinding
import si.uni_lj.fri.pbd.lab2.databinding.ActivityRegistrationBinding

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get the name from the intent
        val intent = intent as Intent
        val fullName = intent.getStringExtra(RegistrationActivity.EXTRA_NAME) as String

        binding.textView.text = fullName
    }

    fun showToast(view: View) {
        val msg = binding.editMsg.text.toString()
        val duration = Toast.LENGTH_LONG
        Toast.makeText(applicationContext, msg, duration).show()
    }
}