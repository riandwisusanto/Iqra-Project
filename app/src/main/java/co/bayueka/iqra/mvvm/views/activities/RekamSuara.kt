package co.bayueka.iqra.mvvm.views.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import co.bayueka.iqra.R
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_rekam_suara.*
import java.io.File
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Button
import co.bayueka.iqra.retrofit.DataRepository
import co.bayueka.iqra.retrofit.PostModel
import java.io.IOException
import com.android.volley.Response
import retrofit2.Call
import retrofit2.Callback

class RekamSuara : AppCompatActivity() {

    private var output: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false
    private val postURL: String="http://localhost/testing/test.php"
    private var audioData: ByteArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rekam_suara)

//        retrofit get data example
//        val postServices = DataRepository.create()
//        postServices.getPosts().enqueue(object : Callback<List<PostModel>> {
//            override fun onFailure(call: Call<List<PostModel>>, error: Throwable) {
//                Log.e("retrofit", "errornya ${error.message}")
//            }
//
//            override fun onResponse(
//                call: Call<List<PostModel>>,
//                response: retrofit2.Response<List<PostModel>>
//            ) {
//                if (response.isSuccessful) {
//                    val data = response.body()
//                    Log.d("retrofit", "responsennya ${data?.size}")
//
//                    data?.map {
//                        Log.d("retrofit", "datanya ${it.body}")
//                    }
//                }
//            }
//        })


        mediaRecorder = MediaRecorder()

        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

        val record = findViewById<Button>(R.id.record)
        val stop = findViewById<Button>(R.id.stop)
        val play = findViewById<Button>(R.id.play)

        record.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val permissions = arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                ActivityCompat.requestPermissions(this, permissions, 0)
            } else {
                startRecorder()
            }
        }
        stop.setOnClickListener {
            stopRecorder()
        }

        play.setOnClickListener {
            play()
        }

    }

    fun play()
    {
        val mp= MediaPlayer()
        val mFileName = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()+"/recording.mp3";
        mp.setDataSource(mFileName)
        mp.prepare()
        mp.start()
    }

    private fun startRecorder() {
        try {
            val mFileName = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()+"/recording.mp3";
            mediaRecorder?.setOutputFile(mFileName)
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            state = true
            Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    private fun stopRecorder() {
        if(state){
            mediaRecorder?.stop()
            mediaRecorder?.release()
            state = false
            Toast.makeText(this, "Recording Stop", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "You are not recording right now!", Toast.LENGTH_SHORT).show()
        }
    }


}


