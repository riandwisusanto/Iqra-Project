package co.bayueka.iqra.mvvm.views.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import co.bayueka.iqra.R
import co.bayueka.iqra.databinding.ActivityTestBinding

class TestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTestBinding

    private lateinit var sharedPreferences: SharedPreferences

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("Score", Context.MODE_PRIVATE)

        binding.toolbar.txtTitle.text = resources.getString(R.string.test)
        binding.apply {
            val scorelistening = sharedPreferences.getInt("score_listening", 0)
            val scoreSpeaking  = sharedPreferences.getInt("score_speaking", 0)

            textListening.text = resources.getString(R.string.test_mendengarkan) + " (" + scorelistening + ")"
            textSpeaking.text   = resources.getString(R.string.test_pengucapan) + " (" + scoreSpeaking + ")"

            linearTestListening.setOnClickListener {
                startActivity(
                    Intent(this@TestActivity, TestListeningActivity::class.java)
                )
            }
            linearTestSpeaking.setOnClickListener {
                if (scorelistening > 79)
                    startActivity(
                        Intent(this@TestActivity, TestListeningActivity::class.java)
                    )
                else
                    Toast.makeText(this@TestActivity, "Capai score 80 untuk membuka test membaca", Toast.LENGTH_LONG).show()
            }

            toolbar.imgBack.setOnClickListener {
                finish()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()

        binding.apply {
            val scorelistening = sharedPreferences.getInt("score_listening", 0)
            val scoreSpeaking  = sharedPreferences.getInt("score_speaking", 0)

            textListening.text = resources.getString(R.string.test_mendengarkan) + " (" + scorelistening + ")"
            textSpeaking.text   = resources.getString(R.string.test_pengucapan) + " (" + scoreSpeaking + ")"
        }

    }
}