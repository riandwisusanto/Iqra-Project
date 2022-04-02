package co.bayueka.iqra.mvvm.views.activities

import android.Manifest
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import co.bayueka.iqra.R
import co.bayueka.iqra.databinding.ActivityTestSpeakingBinding
import co.bayueka.iqra.databinding.PopupResultTestBinding
import co.bayueka.iqra.mvvm.models.HijaiyahModel
import co.bayueka.iqra.mvvm.viewmodels.HijaiyahViewModel
import co.bayueka.iqra.utils.SessionManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.takusemba.spotlight.OnSpotlightListener
import com.takusemba.spotlight.OnTargetListener
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.Target
import com.takusemba.spotlight.shape.RoundedRectangle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.math.roundToInt

private const val TAG = "TestSpeakingActivity"

@AndroidEntryPoint
class TestSpeakingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestSpeakingBinding
    @Inject lateinit var sessionManager: SessionManager
    private var loading: ProgressDialog? = null
    private val viewModel: HijaiyahViewModel by viewModels()
    private var hijaiyahDataQuestion: MutableList<HijaiyahModel> = mutableListOf()
    private val hijaiyahId: MutableList<String> = mutableListOf()
    private val hijaiyahHuruf: MutableList<String> = mutableListOf()
    private var number: Int = 1
    private var numberQuestion = 0
    private var answerModel: HijaiyahModel? = null
    private var score = 0
    private var delay = 1000L
    private lateinit var speechRecognizer: SpeechRecognizer
    private var isMicOn = false
    private val database = Firebase.database
    private val myRef = database.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestSpeakingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkPermission()

        initComponents()
        subscribeListeners()
        subscribeObservers()

        showLoading()
        viewModel.listHijaiyah("")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == TrainingActivity.RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults.size > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this@TestSpeakingActivity, "Permission Didapatkan", Toast.LENGTH_SHORT).show()
                } else {
                    checkPermission()
                }
            } else {
                checkPermission()
            }
        }
    }

    private fun initComponents() {
        binding.apply {
            toolbar.txtTitle.text = root.context.resources.getString(R.string.test_pengucapan_huruf_hijaiyah)
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
    }

    private fun subscribeListeners() {
        binding.apply {
            toolbar.imgBack.setOnClickListener {
                finish()
            }
            linearSkip.setOnClickListener {
                showQuestion()
            }

            myRef.child("hijaiyah").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    hijaiyahId.clear()
                    hijaiyahHuruf.clear()

                    hijaiyahId.add("0")
                    hijaiyahHuruf.add(resources.getString(R.string.pilih_huruf))
                    snapshot.children.forEach {
                        val hijaiyah = it.getValue(TrainingActivity.Hijaiyah::class.java)
                        hijaiyah?.let {
                            it.id?.let {
                                hijaiyahId.add(it)
                            }
                            it.huruf?.let {
                                hijaiyahHuruf.add(it)
                            }
                        }
                    }
                    hideLoading()
                    if (sessionManager.spotlightSpeaking) {
                        setEnableButton()
                        Handler(Looper.getMainLooper()).postDelayed({
                            startSpotlight()
                        }, 500)
                    } else {
                        setEnableButton(true)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Child Hijaiyah: Failed to read value.", error.toException())
                }

            })

            val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
            speechRecognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d(TAG, "speechRecognizerIntent: onReadyForSpeech: ")
                }

                override fun onBeginningOfSpeech() {
                    Log.d(TAG, "speechRecognizerIntent: onBeginningOfSpeech: ")
                    binding.txtRecord.setText(resources.getString(R.string.mendengarkan))
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
                    binding.imgMic.setImageResource(R.drawable.ic_baseline_mic)
                    binding.txtRecord.setText(resources.getString(R.string.tap_untuk_berbicara))
                }

                override fun onError(error: Int) {
                    Log.d(TAG, "speechRecognizerIntent: onError: $error")
                    if (error == 7) {
                        Toast.makeText(this@TestSpeakingActivity, "Suara tidak dapat dikenali", Toast.LENGTH_SHORT).show()
                    }
                    isMicOn = false
                    binding.imgMic.setImageResource(R.drawable.ic_baseline_mic)
                    binding.txtRecord.setText(resources.getString(R.string.tap_untuk_berbicara))
                }

                override fun onResults(results: Bundle?) {
                    Log.d(TAG, "speechRecognizerIntent: onResults: ")
                    binding.imgMic.setImageResource(R.drawable.ic_baseline_mic)
                    results?.let {
                        val data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        Log.d(TAG, "speechRecognizerIntent: onResults: data array $data")
                        data?.let {
                            val res = data.get(0).trim().toLowerCase(Locale.getDefault())
                            Log.d(TAG, "speechRecognizerIntent: onResults: data output ${res}")
                            checkData(res)
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
            binding.relativeMic.setOnClickListener {
                if (isMicOn) {
                    isMicOn = false
                    speechRecognizer.stopListening()
                    binding.imgMic.setImageResource(R.drawable.ic_baseline_mic)
                    binding.txtRecord.setText(resources.getString(R.string.tap_untuk_berbicara))
                } else {
                    isMicOn = true
                    speechRecognizer.startListening(speechRecognizerIntent)
                    binding.imgMic.setImageResource(R.drawable.ic_baseline_mic_red)
                }
            }
        }
    }

    private fun subscribeObservers() {
        lifecycleScope.launch {
            viewModel.hijaiyah.observe(this@TestSpeakingActivity, {
                if (it != null) {
                    hijaiyahDataQuestion = it
                    showQuestion()
                    viewModel.hijaiyah.value = null
                }
            })
        }
    }

    private fun checkData(input: String) {
        showLoading()

        val indexHijaiyahAnswer = hijaiyahHuruf.indexOf(answerModel!!.title)
        val hijaiyahIdAnswer = hijaiyahId[indexHijaiyahAnswer]
        val keywordHijaiyahId = "${input}_${hijaiyahIdAnswer}"

        Log.d(TAG, "checkData: $keywordHijaiyahId")

        myRef.child("training").orderByChild("keywordHijaiyahId").equalTo(keywordHijaiyahId).get().addOnSuccessListener {
            Log.d(TAG, "subscribeListeners: sudah ada ${it.value}")
            if (it.value == null) {
                hideLoading()
                binding.checkAnswer.visibility = View.VISIBLE
                binding.checkAnswer.setImageResource(R.drawable.ic_baseline_close)

                hijaiyahDataQuestion.removeAt(numberQuestion)
                Handler(Looper.getMainLooper()).postDelayed({
                    showQuestion()
                }, delay)
            } else {
                it.children.forEach {
                    Log.d(TAG, "subscribeListeners: ${it.key}")
                    it.key?.let { key ->
                        myRef.child("training").child(key).get().addOnSuccessListener { dataSnapshotSample ->
                            val sample = dataSnapshotSample.getValue(TrainingActivity.TrainingHijaiyahSample::class.java)
                            sample?.let { trainingHijaiyahSample ->
                                hideLoading()
                                binding.checkAnswer.visibility = View.VISIBLE
                                binding.checkAnswer.setImageResource(R.drawable.ic_baseline_check)
                                score++

                                hijaiyahDataQuestion.removeAt(numberQuestion)
                                Handler(Looper.getMainLooper()).postDelayed({
                                    showQuestion()
                                }, delay)
                            }
                        }.addOnFailureListener {
                            hideLoading()
                            Toast.makeText(this@TestSpeakingActivity, it.message, Toast.LENGTH_SHORT).show()
                            Log.e(TAG, "Child Training: subscribeListeners: Error getting data", it)
                        }
                    }
                }
            }
        }.addOnFailureListener {
            hideLoading()
            Toast.makeText(this@TestSpeakingActivity, it.message, Toast.LENGTH_SHORT).show()
            Log.e(TAG, "subscribeListeners: Error getting data", it)
        }
    }

    private fun showQuestion() {
        binding.apply {
            if (number <= 30) {
                txtNumber.text = String.format(binding.root.context.resources.getString(R.string._1_30), number)

                checkAnswer.visibility = View.GONE

                numberQuestion = (0 until hijaiyahDataQuestion.size).random()
                answerModel = hijaiyahDataQuestion[numberQuestion]

                imgAnswer.setImageResource(answerModel!!.img)

                number++
            } else {
                showPopupResult()
            }
        }
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO),
                    TrainingActivity.RECORD_AUDIO_REQUEST_CODE
                )
            }
        }
    }

    private fun showPopupResult() {
        val dialog = Dialog(this)
        val popupBinding = PopupResultTestBinding.inflate(layoutInflater)
        dialog.setContentView(popupBinding.root)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(false)
        dialog.show()

        popupBinding.txtTitle.text = resources.getString(R.string.test_pengucapan_huruf_hijaiyah)
        val pecentage = (score.toDouble() / 30.0 * 100.0).roundToInt().toString() + "%"
        popupBinding.txtPercentage.text = pecentage
        popupBinding.txtCorrectAnswer.text = String.format(resources.getString(R.string.jawaban_benar), score)
        popupBinding.txtIncorrectAnswer.text = String.format(resources.getString(R.string.jawaban_salah), (30 - score).toString())
        popupBinding.btnBack.setOnClickListener {
            finish()
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

    private fun startSpotlight() {
        val targets = ArrayList<Target>()

        val firstRow = binding.relativeMic
        val firstRoot = FrameLayout(this)
        val first = layoutInflater.inflate(R.layout.spotlight_answer_listening, firstRoot)
        val firstTarget = Target.Builder()
            .setAnchor(firstRow)
            .setShape(RoundedRectangle(firstRow.height.toFloat(), firstRow.width.toFloat(), 8F))
            .setOverlay(first)
            .setOnTargetListener(object : OnTargetListener {
                override fun onStarted() {
                    first.findViewById<TextView>(R.id.txtSpotlight).text = resources.getString(R.string.tekan_tombol_untuk_membuka_microphone_njika_microphone_belum_tertutup_silahkan_tekan_tombol_microphone_lagi_untuk_menutup)
                }

                override fun onEnded() {
                    //
                }
            })
            .build()

        targets.add(firstTarget)

        val secondRow = binding.linearSkip
        val secondRoot = FrameLayout(this)
        val second = layoutInflater.inflate(R.layout.spotlight_answer_listening, secondRoot)
        val secondTarget = Target.Builder()
            .setAnchor(secondRow)
            .setShape(RoundedRectangle(secondRow.height.toFloat(), secondRow.width.toFloat(), 8F))
            .setOverlay(second)
            .setOnTargetListener(object : OnTargetListener {
                override fun onStarted() {
                    second.findViewById<TextView>(R.id.txtSpotlight).text = resources.getString(R.string.tekan_tombol_skip_jika_anda_ingin_melanjutkan_pertanyaan_lain)
                }

                override fun onEnded() {
                    sessionManager.spotlightSpeaking = false
                }
            })
            .build()

        targets.add(secondTarget)

        val spotlight = Spotlight.Builder(this@TestSpeakingActivity)
            .setTargets(targets)
            .setBackgroundColorRes(R.color.spotlightBackground)
            .setDuration(1000L)
            .setAnimation(DecelerateInterpolator(2f))
            .setOnSpotlightListener(object : OnSpotlightListener {
                override fun onStarted() {
                    //
                }

                override fun onEnded() {
                    setEnableButton(true)
                }
            })
            .build()

        spotlight.start()

        first.findViewById<View>(R.id.nextTarget).setOnClickListener { spotlight.next() }
        //second.findViewById<View>(R.id.nextTarget).setOnClickListener { spotlight.next() }
        second.findViewById<View>(R.id.nextTarget).visibility=View.GONE


        first.findViewById<View>(R.id.closeSpotlight).setOnClickListener {
            sessionManager.spotlightSpeaking = false
            setEnableButton(true)
            spotlight.finish()
        }
        second.findViewById<View>(R.id.closeSpotlight).setOnClickListener {
            sessionManager.spotlightSpeaking = false
            setEnableButton(true)
            spotlight.finish()
        }
    }

    private fun setEnableButton(isEnabled: Boolean = false) {
        binding.apply {
            if (!isEnabled) {
                relativeMic.isEnabled = false
                linearSkip.isEnabled = false
                toolbar.imgBack.isEnabled = false
            } else {
                relativeMic.isEnabled = true
                linearSkip.isEnabled = true
                toolbar.imgBack.isEnabled = true
            }
        }
    }
}