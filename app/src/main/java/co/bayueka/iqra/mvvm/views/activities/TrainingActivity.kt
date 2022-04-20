package co.bayueka.iqra.mvvm.views.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
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
import co.bayueka.iqra.databinding.ActivityTrainingBinding
import co.bayueka.iqra.mvvm.models.SpeakModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.parcel.Parcelize
import java.io.File
import java.util.*


private const val TAG = "TrainingActivity"

@AndroidEntryPoint
class TrainingActivity : AppCompatActivity() {

    companion object {
        val RECORD_AUDIO_REQUEST_CODE = 1
    }

    private lateinit var binding: ActivityTrainingBinding

    private var loading: ProgressDialog? = null
    private val hijaiyahId: MutableList<String> = mutableListOf()
    private val hijaiyahHuruf: MutableList<String> = mutableListOf()
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
        binding = ActivityTrainingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkPermission()

        initComponents()
        subscribeListeners()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults.size > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this@TrainingActivity, "Permission Didapatkan", Toast.LENGTH_SHORT).show()
                } else {
                    checkPermission()
                }
            } else {
                checkPermission()
            }
        }
    }

    private fun initComponents() {
        binding.toolbar.txtTitle.text = resources.getString(R.string.training_huruf_hijaiyah)

        hijaiyahId.add("0")
        hijaiyahHuruf.add(resources.getString(R.string.pilih_huruf))

        hijaiyahAdapter = ArrayAdapter(this, R.layout.item_spinner_arrow_selected, hijaiyahHuruf)
        hijaiyahAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        binding.spinnerHijaiyah.adapter = hijaiyahAdapter

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
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
                    val hijaiyah = it.getValue(Hijaiyah::class.java)
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
                Log.w(TAG, "Child Hijaiyah: Failed to read value.", error.toException())
            }

        })

        binding.spinnerHijaiyah.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedPosition = position
                lastNumber = 0
                getLastNumber()
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
                if (error == 7) {
                    Toast.makeText(this@TrainingActivity, "Suara tidak dapat dikenali", Toast.LENGTH_SHORT).show()
                }
                isMicOn = false
                binding.imgMic.setImageResource(R.drawable.record_btn_recording)
                binding.txtRecord.setText(resources.getString(R.string.tap_untuk_berbicara))
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
                        saveData(res)
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
                        this@TrainingActivity,
                        "Harap Pilih Huruf Hijaiyah !",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    if (isMicOn) {
                        isMicOn = false
                        stopRecorder()
                        speechRecognizer.stopListening()
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
        binding.recordList.setOnClickListener {
            startActivity(Intent(this, ListRecordActivity::class.java))
        }
    }

    private fun saveData(input: String) {
        showLoading()
        val id = myRef.push().key
        id?.let {
            val sample = SpeakModel(
                it,
                hijaiyahId[selectedPosition],
                hijaiyahHuruf[selectedPosition],
                input,
                lastNumber,
                hijaiyahHuruf[selectedPosition]+"_"+lastNumber.toString()
            )
            myRef.child("spech").child(hijaiyahHuruf[selectedPosition]).child(id).setValue(sample)
                .addOnSuccessListener {
                    hideLoading()
                    lastNumber++
                }
            Toast.makeText(this, "Berhasil menyimpan data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_REQUEST_CODE)
            }
        }
    }

    fun showLoading() {
        if (loading == null) {
            loading = ProgressDialog.show(this, null, "Harap Tunggu ...", true, false)
        }
    }

    fun hideLoading() {
        if (loading != null) {
            loading!!.dismiss()
            loading = null
        }
    }

    @SuppressLint("NewApi")
    private fun startRecorder() {
        try {
            mediaRecorder = MediaRecorder()

            mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
//            val fileName = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()+
//                    "/" + hijaiyahHuruf[selectedPosition] + "_" + lastNumber +".3gp"
            val fileName = File( Environment.getExternalStorageDirectory(),hijaiyahHuruf[selectedPosition] + "_" + lastNumber +".3gp")
            mediaRecorder?.setOutputFile(fileName.absolutePath)
            mediaRecorder?.prepare()
            mediaRecorder?.start()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    private fun stopRecorder() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    fun getLastNumber(){
        showLoading()
        myRef.child("spech").child(hijaiyahHuruf[selectedPosition]).orderByChild("number")
            .limitToLast(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                hideLoading()
                if (snapshot.exists()){
                    for (vl in snapshot.children){
                        val value = vl.getValue(SpeakModel::class.java)
                        value.let {
                            lastNumber = (it!!.number!! + 1)
                            Log.d(TAG, it.number.toString())
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Child Training: Failed to read value.", error.toException())
            }
        })
    }

    @Parcelize
    data class Hijaiyah(
        var id: String? = null,
        var huruf: String? = null
    ) : Parcelable {}
}