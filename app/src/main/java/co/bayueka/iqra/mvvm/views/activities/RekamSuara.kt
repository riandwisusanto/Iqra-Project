package co.bayueka.iqra.mvvm.views.activities

import android.Manifest
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
import java.io.IOException
import com.android.volley.Response

class RekamSuara : AppCompatActivity() {

    private var output: String? = null
    private var mediaRecorder: MediaRecorder? = null
    private var state: Boolean = false
    private val postURL: String="http://localhost/testing/test.php"
    private var audioData: ByteArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rekam_suara)


        mediaRecorder = MediaRecorder()
        //output = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString()+"/recording.mp3"
        output=getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()+"/recording.mp3"
            //Environment.getExternalStorageDirectory().absolutePath + "/recording.mp3"
        System.out.print("file output " + output)


        var rekam:TextView=findViewById<TextView>(R.id.rekam)
        rekam.setText(output)

        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setOutputFile(output)

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
                simpan()
            }
        }
        stop.setOnClickListener {
            stop()
        }

    }
    private fun uploadAudio() {
        var bytes= File(output).readBytes()

//        audioData?: return
//        val request = object : VolleyFileUploadRequest(
//            Request.Method.POST,
//            postURL,
//            Response.Listener {
//                println("response is: $it")
//            },
//            Response.ErrorListener {
//                println("error is: $it")
//            }
//        ) {
//            override fun getByteData(): MutableMap<String, FileDataPart> {
//                var params = HashMap<String, FileDataPart>()
//                params["imageFile"] = FileDataPart("image", audioData!!, "jpeg")
//                return params
//            }
//        }
//        Volley.newRequestQueue(this).add(request)


    }



    public fun play(v:View)
    {
        var mp= MediaPlayer()
        mp.setDataSource(output)
        mp.prepare()
        mp.start()
    }

    private fun simpan() {
        startRecorder()
    }

    private fun startRecorder() {
        try {
            Toast.makeText(this, "Recording started!", Toast.LENGTH_SHORT).show()
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            state = true
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stop() {
        stopRecorder()
    }

    private fun stopRecorder() {
        if(state){
            mediaRecorder?.stop()
            mediaRecorder?.release()
            state = false
        }else{
            Toast.makeText(this, "You are not recording right now!", Toast.LENGTH_SHORT).show()
        }
    }


}


