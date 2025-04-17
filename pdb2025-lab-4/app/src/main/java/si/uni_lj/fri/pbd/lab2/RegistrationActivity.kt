package si.uni_lj.fri.pbd.lab2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import si.uni_lj.fri.pbd.lab2.databinding.ActivityRegistrationBinding

class RegistrationActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_NAME = "si.uni_lj.fri.pbd.lab2.FULL_NAME"
    }

    private lateinit var binding: ActivityRegistrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun registerUser(view: View) {
        val fullName = binding.editName.text.toString()
        if (fullName.isEmpty()) {
            binding.editName.error = getString(R.string.reg_full_name_error)
            return
        }

        val intent = Intent(applicationContext, ProfileActivity::class.java)
        intent.putExtra(EXTRA_NAME, fullName)
        startActivity(intent)
    }
}