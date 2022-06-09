package co.bayueka.iqra.mvvm.views.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.MediaRecorder
import android.net.Uri
import android.os.*
import android.provider.MediaStore
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
import co.bayueka.iqra.retrofit.DataRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
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
    var audioUri: Uri? = null
    private var baseUrl = "http://11.11.11.251:5000/"

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

        binding.imgRestart.setOnClickListener {
            toTrainPython()
        }
    }

    @SuppressLint("NewApi")
    private fun startRecorder() {
        try {
            mediaRecorder = MediaRecorder()

            mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                mediaRecorder!!.setOutputFile(getFilePath())
            } else {
                mediaRecorder!!.setOutputFile(getFileDescriptor())
            }
            mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mediaRecorder?.prepare()
            mediaRecorder?.start()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    private fun stopRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
        }
    }

    private fun toTrainPython(){
        showLoading()
        val multipartBody = MultipartBody.Builder()
        val file = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) getFilePath() else getFileDescriptor2().toString()
        val fileBody = RequestBody.create(MediaType.parse("mp3"), file)

        multipartBody.addFormDataPart("sound", "audio.mp3", fileBody)

        val postServices = DataRepository.create(baseUrl)
        val body = multipartBody.build()
        postServices.uploadWav(
            "multipart/form-data; boundary=" + body.boundary(),
            body
        ).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: retrofit2.Response<String>) {
                if(response.isSuccessful){
                    hideLoading()
                    val data = response.body()
                    Toast.makeText(this@BelajarHurufHijaiyah, "Hasil = ${data}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                hideLoading()
                Toast.makeText(this@BelajarHurufHijaiyah, "errornya ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("retrofit", "errornya ${t.message}")
            }

        })
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

    private fun getFileDescriptor(): FileDescriptor {
        var displayName: String? = null
        var relativePath: String? = null
        var media_id: String? = null
        val filePathColumn =
            arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME)
        var uri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val selection = "${MediaStore.Audio.Media.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf("audio.mp3")
        var cursor: Cursor? = null
        try {
            cursor = getContentResolver().query(uri, filePathColumn, selection, selectionArgs, null)
            if (cursor != null) {
                //cursor.moveToFirst()
                while (cursor.moveToNext()) {
                    var idColumn: Int = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    var nameColumn: Int =
                        cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                    val uri: Uri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                    relativePath =
                        ContentUris.withAppendedId(uri, cursor.getLong(idColumn)).toString()
                    displayName = cursor.getString(nameColumn)
                    media_id = cursor.getLong(idColumn).toString()

                }
            }
        } finally {
            if (cursor != null) {
                cursor.close()
            }
        }

        var resolver: ContentResolver = applicationContext.contentResolver
        var audioCollection: Uri = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "audio.mp3")
            //put(MediaStore.Audio.Media.TITLE, "test_audio")
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/*")
            put(MediaStore.Audio.Media.RELATIVE_PATH, getAudioDirectoryPath())
            //put(MediaStore.Audio.Media.IS_PENDING, 1)
        }
        if (!relativePath.isNullOrEmpty()) {
            audioUri = Uri.parse(relativePath)
        } else {
            audioUri = resolver.insert(audioCollection, values)
        }
        var parcelFileDescriptor: ParcelFileDescriptor =
            resolver.openFileDescriptor(audioUri!!, "wt")!!
        return parcelFileDescriptor.fileDescriptor
    }

    private fun getFileDescriptor2(): FileDescriptor {
        var parcelFileDescriptor: ParcelFileDescriptor =
            contentResolver.openFileDescriptor(audioUri!!, "r")!!
        return parcelFileDescriptor.fileDescriptor
    }

    private fun getFilePath(): String {
        var directory: File? =
            getAppSpecificAlbumStorageDir(this, Environment.DIRECTORY_MUSIC, "iqra")
        var file: File = File(directory, "audio.mp3")
        return file.absolutePath
    }

    fun getAudioDirectoryPath(): String{
        return Environment.DIRECTORY_MUSIC + File.separator + "iqra" + File.separator
    }

    fun getAppSpecificAlbumStorageDir(context: Context, albumName: String, subAlbumName: String): File? {
        // Get the pictures directory that's inside the app-specific directory on
        // external storage.
        val file = File(
            context.getExternalFilesDir(
                albumName
            ), subAlbumName
        )
        if (!file?.mkdirs()) {
            Log.e("fssfsf", "Directory not created")
        }

        return file
    }
}