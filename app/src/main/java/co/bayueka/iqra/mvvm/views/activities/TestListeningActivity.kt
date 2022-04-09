package co.bayueka.iqra.mvvm.views.activities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import co.bayueka.iqra.R
import co.bayueka.iqra.databinding.ActivityTestListeningBinding
import co.bayueka.iqra.databinding.PopupResultTestBinding
import co.bayueka.iqra.mvvm.models.HijaiyahModel
import co.bayueka.iqra.mvvm.viewmodels.HijaiyahViewModel
import co.bayueka.iqra.utils.SessionManager
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

private const val TAG = "TestListeningActivity"

@AndroidEntryPoint
class TestListeningActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestListeningBinding
    @Inject lateinit var sessionManager: SessionManager
    private val viewModel: HijaiyahViewModel by viewModels()
    private var hijaiyahData: MutableList<HijaiyahModel> = mutableListOf()
    private var hijaiyahDataQuestion: MutableList<HijaiyahModel> = mutableListOf()
    private var number: Int = 1
    private var numberQuestion = 0
    private var questions: MutableList<Int> = mutableListOf()
    private var answerModel: HijaiyahModel? = null
    private var mediaPlayer: MediaPlayer? = null
    private var score = 0
    private var delay = 2000L

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestListeningBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("Score", Context.MODE_PRIVATE)

        initComponents()
        subscribeListeners()
        subscribeObservers()

        viewModel.listHijaiyah("")
        if (sessionManager.spotlightListening) {
            setEnableButton()
            Handler(Looper.getMainLooper()).postDelayed({
                startSpotlight()
            }, 500)
        } else {
            setEnableButton(true)
        }
    }

    private fun initComponents() {
        binding.apply {
            toolbar.txtTitle.text = root.context.resources.getString(R.string.test_mendengarkan_huruf_hijaiyah)
        }
    }

    private fun subscribeListeners() {
        binding.apply {
            toolbar.imgBack.setOnClickListener {
                finish()
            }
            linearPlay.setOnClickListener {
                playHijaiyah()
            }
            constraintAnswer1.setOnClickListener {
                answerModel?.let { model ->
                    if (model.title.equals(hijaiyahDataQuestion[questions[0]].title)) {
                        checkAnswer1.visibility = View.VISIBLE
                        checkAnswer1.setImageResource(R.drawable.ic_baseline_check)

                        imgjwb.visibility=View.VISIBLE
                        imgjwb.setImageResource(R.drawable.benar)
                        score++
                    } else {
                        checkAnswer1.visibility = View.VISIBLE
                        checkAnswer1.setImageResource(R.drawable.ic_baseline_close)

                        imgjwb.visibility=View.VISIBLE
                        imgjwb.setImageResource(R.drawable.salah)

                        val indexOfAnswer = questions.indexOf(numberQuestion)
                        if (indexOfAnswer == 1) {
                            checkAnswer2.visibility = View.VISIBLE
                            checkAnswer2.setImageResource(R.drawable.ic_baseline_check)

                            imgjwb.visibility=View.VISIBLE
                            imgjwb.setImageResource(R.drawable.salah)
                        }
                        if (indexOfAnswer == 2) {
                            checkAnswer3.visibility = View.VISIBLE
                            checkAnswer3.setImageResource(R.drawable.ic_baseline_check)

                            imgjwb.visibility=View.VISIBLE
                            imgjwb.setImageResource(R.drawable.salah)
                        }
                        if (indexOfAnswer == 3) {
                            checkAnswer4.visibility = View.VISIBLE
                            checkAnswer4.setImageResource(R.drawable.ic_baseline_check)

                            imgjwb.visibility=View.VISIBLE
                            imgjwb.setImageResource(R.drawable.salah)
                        }
                    }
                    hijaiyahDataQuestion.removeAt(numberQuestion)
                    Handler(Looper.getMainLooper()).postDelayed({
                        showQuestion()
                    }, delay)
                }
            }
            constraintAnswer2.setOnClickListener {
                answerModel?.let { model ->
                    if (model.title.equals(hijaiyahDataQuestion[questions[1]].title)) {
                        checkAnswer2.visibility = View.VISIBLE
                        checkAnswer2.setImageResource(R.drawable.ic_baseline_check)

                        imgjwb.visibility=View.VISIBLE
                        imgjwb.setImageResource(R.drawable.benar)
                        score++
                    } else {
                        checkAnswer2.visibility = View.VISIBLE
                        checkAnswer2.setImageResource(R.drawable.ic_baseline_close)

                        imgjwb.visibility=View.VISIBLE
                        imgjwb.setImageResource(R.drawable.salah)
                        val indexOfAnswer = questions.indexOf(numberQuestion)
                        if (indexOfAnswer == 0) {
                            checkAnswer1.visibility = View.VISIBLE
                            checkAnswer1.setImageResource(R.drawable.ic_baseline_check)

                            imgjwb.visibility=View.VISIBLE
                            imgjwb.setImageResource(R.drawable.salah)
                        }
                        if (indexOfAnswer == 2) {
                            checkAnswer3.visibility = View.VISIBLE
                            checkAnswer3.setImageResource(R.drawable.ic_baseline_check)

                            imgjwb.visibility=View.VISIBLE
                            imgjwb.setImageResource(R.drawable.salah)
                        }
                        if (indexOfAnswer == 3) {
                            checkAnswer4.visibility = View.VISIBLE
                            checkAnswer4.setImageResource(R.drawable.ic_baseline_check)

                            imgjwb.visibility=View.VISIBLE
                            imgjwb.setImageResource(R.drawable.salah)
                        }
                    }
                    hijaiyahDataQuestion.removeAt(numberQuestion)
                    Handler(Looper.getMainLooper()).postDelayed({
                        showQuestion()
                    }, delay)
                }
            }
            constraintAnswer3.setOnClickListener {
                answerModel?.let { model ->
                    if (model.title.equals(hijaiyahDataQuestion[questions[2]].title)) {
                        checkAnswer3.visibility = View.VISIBLE
                        checkAnswer3.setImageResource(R.drawable.ic_baseline_check)

                        imgjwb.visibility=View.VISIBLE
                        imgjwb.setImageResource(R.drawable.benar)
                        score++
                    } else {
                        checkAnswer3.visibility = View.VISIBLE
                        checkAnswer3.setImageResource(R.drawable.ic_baseline_close)

                        imgjwb.visibility=View.VISIBLE
                        imgjwb.setImageResource(R.drawable.salah)
                        val indexOfAnswer = questions.indexOf(numberQuestion)
                        if (indexOfAnswer == 0) {
                            checkAnswer1.visibility = View.VISIBLE
                            checkAnswer1.setImageResource(R.drawable.ic_baseline_check)

                            imgjwb.visibility=View.VISIBLE
                            imgjwb.setImageResource(R.drawable.salah)
                        }
                        if (indexOfAnswer == 1) {
                            checkAnswer2.visibility = View.VISIBLE
                            checkAnswer2.setImageResource(R.drawable.ic_baseline_check)

                            imgjwb.visibility=View.VISIBLE
                            imgjwb.setImageResource(R.drawable.salah)
                        }
                        if (indexOfAnswer == 3) {
                            checkAnswer4.visibility = View.VISIBLE
                            checkAnswer4.setImageResource(R.drawable.ic_baseline_check)

                            imgjwb.visibility=View.VISIBLE
                            imgjwb.setImageResource(R.drawable.salah)
                        }
                    }
                    hijaiyahDataQuestion.removeAt(numberQuestion)
                    Handler(Looper.getMainLooper()).postDelayed({
                        showQuestion()
                    }, delay)
                }
            }
            constraintAnswer4.setOnClickListener {
                answerModel?.let { model ->
                    if (model.title.equals(hijaiyahDataQuestion[questions[3]].title)) {
                        checkAnswer4.visibility = View.VISIBLE
                        checkAnswer4.setImageResource(R.drawable.ic_baseline_check)

                        imgjwb.visibility=View.VISIBLE
                        imgjwb.setImageResource(R.drawable.benar)
                        score++
                    } else {
                        checkAnswer4.visibility = View.VISIBLE
                        checkAnswer4.setImageResource(R.drawable.ic_baseline_close)

                        imgjwb.visibility=View.VISIBLE
                        imgjwb.setImageResource(R.drawable.salah)
                        val indexOfAnswer = questions.indexOf(numberQuestion)
                        if (indexOfAnswer == 0) {
                            checkAnswer1.visibility = View.VISIBLE
                            checkAnswer1.setImageResource(R.drawable.ic_baseline_check)

                            imgjwb.visibility=View.VISIBLE
                            imgjwb.setImageResource(R.drawable.salah)
                        }
                        if (indexOfAnswer == 1) {
                            checkAnswer2.visibility = View.VISIBLE
                            checkAnswer2.setImageResource(R.drawable.ic_baseline_check)

                            imgjwb.visibility=View.VISIBLE
                            imgjwb.setImageResource(R.drawable.salah)
                        }
                        if (indexOfAnswer == 2) {
                            checkAnswer3.visibility = View.VISIBLE
                            checkAnswer3.setImageResource(R.drawable.ic_baseline_check)

                            imgjwb.visibility=View.VISIBLE
                            imgjwb.setImageResource(R.drawable.salah)
                        }
                    }
                    hijaiyahDataQuestion.removeAt(numberQuestion)
                    Handler(Looper.getMainLooper()).postDelayed({
                        showQuestion()
                    }, delay)
                }
            }
            linearSkip.setOnClickListener {
                showQuestion()
            }
        }
    }

    private fun subscribeObservers() {
        lifecycleScope.launch {
            viewModel.hijaiyah.observe(this@TestListeningActivity, {
                if (it != null) {
                    hijaiyahData = it
                    hijaiyahDataQuestion = it
                    showQuestion()
                    viewModel.hijaiyah.value = null
                }
            })
        }
    }

    private fun showQuestion() {
        binding.apply {
            if (number <= 30) {
                txtNumber.text = String.format(binding.root.context.resources.getString(R.string._1_30), number)

                checkAnswer1.visibility = View.GONE
                checkAnswer2.visibility = View.GONE
                checkAnswer3.visibility = View.GONE
                checkAnswer4.visibility = View.GONE
                imgjwb.visibility=View.GONE

                numberQuestion = getRandomNumber()
                answerModel = hijaiyahDataQuestion[numberQuestion]
                Log.d(TAG, "showQuestion: answerModel ${answerModel!!.title}")
                questions = getQuestionNumber()

                imgAnswer1.setImageResource(hijaiyahData[questions[0]].img)
                imgAnswer2.setImageResource(hijaiyahData[questions[1]].img)
                imgAnswer3.setImageResource(hijaiyahData[questions[2]].img)
                imgAnswer4.setImageResource(hijaiyahData[questions[3]].img)

                if (!sessionManager.spotlightListening) {
                    playHijaiyah()
                }
                number++
            } else {
                showPopupResult()
            }
        }
    }

    private fun getRandomNumber(): Int {
        return (0 until hijaiyahDataQuestion.size).random()
    }

    private fun getQuestionNumber(): MutableList<Int> {
        var isQuestionGenerated = false
        val indexOfAnswer = hijaiyahData.indexOf(answerModel)
        val answerNumbers: MutableList<Int> = mutableListOf()
        answerNumbers.add(indexOfAnswer)

        while (!isQuestionGenerated) {
            val random = getRandomAnswerNumber()
            if (answerNumbers.indexOf(random) == -1) {
                answerNumbers.add(random)
            }

            if (answerNumbers.size == 4) {
                isQuestionGenerated = true
            }
        }

        answerNumbers.shuffle()
        return answerNumbers
    }

    private fun getRandomAnswerNumber(): Int {
        return (0 until hijaiyahData.size).random()
    }

    private fun playHijaiyah() {
        answerModel?.let { model ->
            if (model.sound != HijaiyahModel.NO_SOUND) {
                if (mediaPlayer != null) {
                    mediaPlayer!!.stop()
                    mediaPlayer!!.reset()
                    mediaPlayer!!.release()
                    mediaPlayer = null

                    mediaPlayer = MediaPlayer.create(this@TestListeningActivity, model.sound)
                    mediaPlayer!!.start()
                } else {
                    mediaPlayer = MediaPlayer.create(this@TestListeningActivity, model.sound)
                    mediaPlayer!!.start()
                }
            } else {
                Toast.makeText(this@TestListeningActivity, model.title, Toast.LENGTH_SHORT).show()
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

        val pecentage = (score.toDouble() / 30.0 * 100.0).roundToInt().toString() + "%"
        popupBinding.txtPercentage.text = pecentage
        popupBinding.txtCorrectAnswer.text = String.format(resources.getString(R.string.jawaban_benar), score)
        popupBinding.txtIncorrectAnswer.text = String.format(resources.getString(R.string.jawaban_salah), (30 - score).toString())

//        save score in sp
        val editor = sharedPreferences.edit()
        editor.putInt("score_listening", (score.toDouble() / 30.0 * 100.0).roundToInt())
        editor.apply()

        popupBinding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun startSpotlight() {
        val targets = ArrayList<Target>()

        val firstRow = binding.linearAnswer
        val firstRoot = FrameLayout(this)
        val first = layoutInflater.inflate(R.layout.spotlight_answer_listening, firstRoot)
        val firstTarget = Target.Builder()
            .setAnchor(firstRow)
            .setShape(RoundedRectangle(firstRow.height.toFloat(), firstRow.width.toFloat(), 8F))
            .setOverlay(first)
            .setOnTargetListener(object : OnTargetListener {
                override fun onStarted() {
                    first.findViewById<TextView>(R.id.txtSpotlight).text = resources.getString(R.string.pilih_jawaban_yang_yang_benar_dari_4_pilihan_ini)
                }

                override fun onEnded() {
                    //
                }
            })
            .build()

        targets.add(firstTarget)

        val secondRow = binding.linearPlay
        val secondRoot = FrameLayout(this)
        val second = layoutInflater.inflate(R.layout.spotlight_main, secondRoot)
        val secondTarget = Target.Builder()
            .setAnchor(secondRow)
            .setShape(RoundedRectangle(secondRow.height.toFloat(), secondRow.width.toFloat(), 8F))
            .setOverlay(second)
            .setOnTargetListener(object : OnTargetListener {
                override fun onStarted() {
                    second.findViewById<TextView>(R.id.txtSpotlight).text = resources.getString(R.string.tekan_tombol_play_untuk_memainkan_suara)
                }

                override fun onEnded() {
                    //
                }
            })
            .build()

        targets.add(secondTarget)

        val thirdRow = binding.linearSkip
        val thirdRoot = FrameLayout(this)
        val third = layoutInflater.inflate(R.layout.spotlight_answer_listening, thirdRoot)
        val thirdTarget = Target.Builder()
            .setAnchor(thirdRow)
            .setShape(RoundedRectangle(thirdRow.height.toFloat(), thirdRow.width.toFloat(), 8F))
            .setOverlay(third)
            .setOnTargetListener(object : OnTargetListener {
                override fun onStarted() {
                    third.findViewById<TextView>(R.id.txtSpotlight).text = resources.getString(R.string.tekan_tombol_skip_jika_anda_ingin_melanjutkan_pertanyaan_lain)
                }

                override fun onEnded() {
                    sessionManager.spotlightListening = false
                }
            })
            .build()

        targets.add(thirdTarget)

        val spotlight = Spotlight.Builder(this@TestListeningActivity)
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
        second.findViewById<View>(R.id.nextTarget).setOnClickListener { spotlight.next() }
        //third.findViewById<View>(R.id.nextTarget).setOnClickListener { spotlight.next() }
        third.findViewById<View>(R.id.nextTarget).visibility=View.GONE

        first.findViewById<View>(R.id.closeSpotlight).setOnClickListener {
            sessionManager.spotlightListening = false
            playHijaiyah()
            setEnableButton(true)
            spotlight.finish()
        }
        second.findViewById<View>(R.id.closeSpotlight).setOnClickListener {
            sessionManager.spotlightListening = false
            playHijaiyah()
            setEnableButton(true)
            spotlight.finish()
        }
        third.findViewById<View>(R.id.closeSpotlight).setOnClickListener {
            sessionManager.spotlightListening = false
            playHijaiyah()
            setEnableButton(true)
            spotlight.finish()
        }
    }

    private fun setEnableButton(isEnabled: Boolean = false) {
        binding.apply {
            if (!isEnabled) {
                constraintAnswer1.isEnabled = false
                constraintAnswer2.isEnabled = false
                constraintAnswer3.isEnabled = false
                constraintAnswer4.isEnabled = false
                linearPlay.isEnabled = false
                linearSkip.isEnabled = false
                linierHasilJ.isEnabled=false
                toolbar.imgBack.isEnabled = false
            } else {
                constraintAnswer1.isEnabled = true
                constraintAnswer2.isEnabled = true
                constraintAnswer3.isEnabled = true
                constraintAnswer4.isEnabled = true
                linearPlay.isEnabled = true
                linearSkip.isEnabled = true
                linierHasilJ.isEnabled=false
                toolbar.imgBack.isEnabled = true
            }
        }
    }
}