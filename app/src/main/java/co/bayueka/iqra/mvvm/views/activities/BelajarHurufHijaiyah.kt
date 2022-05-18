package co.bayueka.iqra.mvvm.views.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import co.bayueka.iqra.R
import co.bayueka.iqra.databinding.ActivityBelajarHurufBinding
import co.bayueka.iqra.mvvm.views.activities.InputDataSpeakingActivity.Companion.RECORD_AUDIO_REQUEST_CODE
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.arthenica.mobileffmpeg.FFmpeg;
import java.io.*
import java.util.*

private const val TAG = "TrainingActivity"

class BelajarHurufHijaiyah : AppCompatActivity() {

    private lateinit var binding: ActivityBelajarHurufBinding
    private var loading: ProgressDialog? = null
    private val hijaiyahId: MutableList<String> = mutableListOf()
    private val hijaiyahHuruf: MutableList<String> = mutableListOf()
    private val hijaiyahImg: MutableList<Int> = mutableListOf()
    private var selectedPosition = 0
    private lateinit var hijaiyahAdapter: ArrayAdapter<String>
    private lateinit var speechRecognizer: SpeechRecognizer
    private var isMicOn = false

    private var mediaRecorder: MediaRecorder? = null

    private val database = Firebase.database
    private val myRef = database.reference

    private var lastNumber = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBelajarHurufBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkPermission()

        initComponents()
        subscribeListeners()
    }

    private fun initComponents() {
        binding.toolbar.txtTitle.text = resources.getString(R.string.latihan_huruf_hijaiyah)

        hijaiyahId.add("0")
        hijaiyahHuruf.add(resources.getString(R.string.pilih_huruf))
        hijaiyahImg.add(0)

        hijaiyahAdapter = ArrayAdapter(this, R.layout.item_spinner_arrow_selected, hijaiyahHuruf)
        hijaiyahAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        binding.spinnerHijaiyah.adapter = hijaiyahAdapter

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        if(!Python.isStarted())
            Python.start(AndroidPlatform(this))

        val py = Python.getInstance()
        val pyObj = py.getModule("python")

        val pyCall = pyObj.callAttr("train", File( Environment.getExternalStorageDirectory(), "audio.wav").absolutePath)
        Log.d("train", pyCall.toString())
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults.size > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Didapatkan", Toast.LENGTH_SHORT).show()
                } else {
                    checkPermission()
                }
            } else {
                checkPermission()
            }
        }
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_REQUEST_CODE)
            }
        }
    }

    private fun subscribeListeners() {
//        load hijaiyah
        myRef.child("hijaiyah").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                hijaiyahId.clear()
                hijaiyahHuruf.clear()

                hijaiyahId.add("0")
                hijaiyahHuruf.add(resources.getString(R.string.pilih_huruf))
                snapshot.children.forEach {
                    val hijaiyah = it.getValue(InputDataSpeakingActivity.Hijaiyah::class.java)
                    hijaiyah?.let {
                        it.id?.let {
                            hijaiyahId.add(it)
                        }
                        it.huruf?.let {
                            hijaiyahHuruf.add(it)
                        }
                    }
                }

                hijaiyahAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("erorr", "Child Hijaiyah: Failed to read value.", error.toException())
            }

        })

//        add hijaiyah img
        hijaiyahImg.add(R.drawable.alif)
        hijaiyahImg.add(R.drawable.ba)
        hijaiyahImg.add(R.drawable.ta)
        hijaiyahImg.add(R.drawable.tsa)
        hijaiyahImg.add(R.drawable.jim)
        hijaiyahImg.add(R.drawable.kha)
        hijaiyahImg.add(R.drawable.kho)
        hijaiyahImg.add(R.drawable.dal)
        hijaiyahImg.add(R.drawable.dzal)
        hijaiyahImg.add(R.drawable.ra)
        hijaiyahImg.add(R.drawable.za)
        hijaiyahImg.add(R.drawable.sin)
        hijaiyahImg.add(R.drawable.syin)
        hijaiyahImg.add(R.drawable.shod)
        hijaiyahImg.add(R.drawable.dhod)
        hijaiyahImg.add(R.drawable.tho)
        hijaiyahImg.add(R.drawable.dhlo)
        hijaiyahImg.add(R.drawable.ain)
        hijaiyahImg.add(R.drawable.ghoin)
        hijaiyahImg.add(R.drawable.fa)
        hijaiyahImg.add(R.drawable.qof)
        hijaiyahImg.add(R.drawable.kaf)
        hijaiyahImg.add(R.drawable.lam)
        hijaiyahImg.add(R.drawable.mim)
        hijaiyahImg.add(R.drawable.nun)
        hijaiyahImg.add(R.drawable.wawu)
        hijaiyahImg.add(R.drawable.ha)
        hijaiyahImg.add(R.drawable.hamzah)
        hijaiyahImg.add(R.drawable.ya)

        binding.spinnerHijaiyah.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedPosition = position
                binding.hurufSelect.setImageResource(hijaiyahImg[position])
            }
        }

        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,10000)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "speechRecognizerIntent: onReadyForSpeech: ")
            }

            override fun onBeginningOfSpeech() {
                Log.d(TAG, "speechRecognizerIntent: onBeginningOfSpeech: ")
                binding.txtRecord.setText(resources.getString(R.string.mendengarkan))
                startRecorder()
            }

            override fun onRmsChanged(rmsdB: Float) {
                //
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                Log.d(TAG, "speechRecognizerIntent: onBufferReceived: $buffer")
            }

            override fun onEndOfSpeech() {
                Log.d(TAG, "speechRecognizerIntent: onEndOfSpeech: ")
                isMicOn = false
                speechRecognizer.stopListening()
                binding.imgMic.setImageResource(R.drawable.record_btn_recording)
                binding.txtRecord.setText(resources.getString(R.string.tap_untuk_berbicara))
                stopRecorder()
            }

            override fun onError(error: Int) {
                Log.d(TAG, "speechRecognizerIntent: onError: $error")
//                if (error == 7) {
//                    Toast.makeText(this@BelajarHurufHijaiyah, "Suara tidak dapat dikenali", Toast.LENGTH_SHORT).show()
//                }
                isMicOn = false
                binding.imgMic.setImageResource(R.drawable.record_btn_recording)
                binding.txtRecord.setText(resources.getString(R.string.tap_untuk_berbicara))
                stopRecorder()
            }

            override fun onResults(results: Bundle?) {
                Log.d(TAG, "speechRecognizerIntent: onResults: ")
                binding.imgMic.setImageResource(R.drawable.record_btn_recording)
                results?.let {
                    val data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    Log.d(TAG, "speechRecognizerIntent: onResults: data array $data")
                    data?.let {
                        val res = data.get(0).trim().toLowerCase(Locale.getDefault())
                        Log.d(TAG, "speechRecognizerIntent: onResults: data output ${res}")
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                Log.d(TAG, "speechRecognizerIntent: onPartialResults: ")
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                Log.d(TAG, "speechRecognizerIntent: onEvent: ")
            }

        })

        binding.toolbar.imgBack.setOnClickListener {
            finish()
        }
        binding.imgMic.setOnClickListener {
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
                if (selectedPosition == 0) {
                    Toast.makeText(
                        this,
                        "Harap Pilih Huruf Hijaiyah !",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    if (isMicOn) {
                        isMicOn = false
                        speechRecognizer.stopListening()
                        stopRecorder()
                        binding.imgMic.setImageResource(R.drawable.record_btn_recording)
                        binding.txtRecord.setText(resources.getString(R.string.tap_untuk_berbicara))
                    } else {
                        isMicOn = true
                        speechRecognizer.startListening(speechRecognizerIntent)
                        binding.imgMic.setImageResource(R.drawable.record_btn_stopped)
                    }
                }
            }
        }
    }

    @SuppressLint("NewApi")
    private fun startRecorder() {
        try {
            mediaRecorder = MediaRecorder()

            mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            val fileName = File( Environment.getExternalStorageDirectory(), "audio_temp.mp3")
            mediaRecorder?.setOutputFile(fileName.absolutePath)
            mediaRecorder?.prepare()
            mediaRecorder?.start()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    private fun stopRecorder() {
        if(mediaRecorder != null){
            try {
                mediaRecorder?.stop()
                mediaRecorder?.release()
                mediaRecorder = null
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }

        FFmpeg.execute("-i "+ File( Environment.getExternalStorageDirectory(), "audio_temp.mp3").absolutePath + " " + File( Environment.getExternalStorageDirectory(), "audio.wav").absolutePath)
        if(!Python.isStarted())
            Python.start(AndroidPlatform(this))

        val py = Python.getInstance()
        val pyObj = py.getModule("python")

        val pyCall = pyObj.callAttr("train", File( Environment.getExternalStorageDirectory(), "audio.wav").absolutePath)
        Log.d("train", pyCall.toString())
    }
}