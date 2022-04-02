package co.bayueka.iqra.mvvm.views.activities

import android.content.Intent
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import co.bayueka.iqra.R
import co.bayueka.iqra.databinding.ActivityMainBinding
import co.bayueka.iqra.utils.SessionManager
import com.takusemba.spotlight.OnSpotlightListener
import com.takusemba.spotlight.OnTargetListener
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.Target
import com.takusemba.spotlight.shape.RoundedRectangle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_iqra.*
import kotlinx.android.synthetic.main.activity_main.view.*
import javax.inject.Inject

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subscribeListeners()

        if (sessionManager.spotlightMain) {
            binding.nsvLockable.setScrollingEnabled(false)
            Handler(Looper.getMainLooper()).postDelayed({
                startSpotlight()
            }, 500)
        } else {
            binding.nsvLockable.setScrollingEnabled(true)
        }

    }

    private fun subscribeListeners() {
        binding.apply {
            linearTraining.setOnClickListener {
                if (!sessionManager.spotlightMain) {
                    startActivity(Intent(this@MainActivity, TrainingActivity::class.java))
                }
            }
            linearHijaiyah.setOnClickListener {
                if (!sessionManager.spotlightMain) {
                    startActivity(Intent(this@MainActivity, HijaiyahActivity::class.java))
                }
            }
            linearIqro1.setOnClickListener {
                if (!sessionManager.spotlightMain) {
                    startActivity(
                        Intent(this@MainActivity, IqraActivity::class.java)
                            .putExtra(IqraActivity.IQRA, 1)
                    )
                }
            }
            linearIqro2.setOnClickListener {
                if (!sessionManager.spotlightMain) {
                    startActivity(
                        Intent(this@MainActivity, IqraActivity::class.java)
                            .putExtra(IqraActivity.IQRA, 2)
                    )
                }
            }
            linearIqro3.setOnClickListener {
                if (!sessionManager.spotlightMain) {
                    startActivity(
                        Intent(this@MainActivity, IqraActivity::class.java)
                            .putExtra(IqraActivity.IQRA, 3)
                    )
                }
            }
            linearIqro4.setOnClickListener {
                if (!sessionManager.spotlightMain) {
                    startActivity(
                        Intent(this@MainActivity, IqraActivity::class.java)
                            .putExtra(IqraActivity.IQRA, 4)
                    )
                }
            }
            linearIqro5.setOnClickListener {
                if (!sessionManager.spotlightMain) {
                    startActivity(
                        Intent(this@MainActivity, IqraActivity::class.java)
                            .putExtra(IqraActivity.IQRA, 5)
                    )
                }
            }
            linearIqro6.setOnClickListener {
                if (!sessionManager.spotlightMain) {
                    startActivity(
                        Intent(this@MainActivity, IqraActivity::class.java)
                            .putExtra(IqraActivity.IQRA, 6)
                    )
                }
            }
            linearTestListening.setOnClickListener {
                if (!sessionManager.spotlightMain) {
                    startActivity(
                        Intent(this@MainActivity, TestListeningActivity::class.java)
                    )
                }
            }
            linearTestSpeaking.setOnClickListener {
                if (!sessionManager.spotlightMain) {
                    startActivity(
                        Intent(this@MainActivity, TestSpeakingActivity::class.java)
                    )
                }
            }
        }
    }

    private fun startSpotlight() {
        val targets = ArrayList<Target>()

        val firstRoot = FrameLayout(this)
        val first = layoutInflater.inflate(R.layout.spotlight_main, firstRoot)
        val firstTarget = Target.Builder()
            .setAnchor(binding.linearHijaiyah)
            .setShape(
                RoundedRectangle(
                    binding.linearHijaiyah.height.toFloat(),
                    binding.linearHijaiyah.width.toFloat(),
                    8F
                )
            )
            .setOverlay(first)
            .setOnTargetListener(object : OnTargetListener {
                override fun onStarted() {
                    first.findViewById<TextView>(R.id.txtSpotlight).text =
                        resources.getString(R.string.spotlight_main_hijaiyah)
                }

                override fun onEnded() {

                }
            })
            .build()
        targets.add(firstTarget)

        val secondRoot = FrameLayout(this)
        val second = layoutInflater.inflate(R.layout.spotlight_main, secondRoot)
        val secondTarget = Target.Builder()
            .setAnchor(binding.linearIqro1)
            .setShape(
                RoundedRectangle(
                    binding.linearIqro1.height.toFloat(),
                    binding.linearIqro1.width.toFloat(),
                    8F
                )
            )
            .setOverlay(second)
            .setOnTargetListener(object : OnTargetListener {
                override fun onStarted() {
                    second.findViewById<TextView>(R.id.txtSpotlight).text =
                        resources.getString(R.string.spotlight_main_iqro1)
                }

                override fun onEnded() {

                }
            })
            .build()
        targets.add(secondTarget)

        val thirdRoot= FrameLayout(this)
        val third=layoutInflater.inflate(R.layout.spotlight_answer_listening,thirdRoot)
        val thirdTarget= Target.Builder()
            .setAnchor(binding.linearIqro2)
            .setShape(
                RoundedRectangle(
                    binding.linearIqro2.height.toFloat(),
                    binding.linearIqro2.width.toFloat(),
                    8f
                    )
            ).setOverlay(third)
            .setOnTargetListener(object : OnTargetListener {
                override fun onStarted() {
                    third.findViewById<TextView>(R.id.txtSpotlight).text=
                        resources.getString(R.string.spotlight_main_iqro2)
                }
                override fun onEnded() {

                }
            })
            .build()
        targets.add(thirdTarget)

        val fourRoot=FrameLayout(this)
        val four=layoutInflater.inflate(R.layout.spotlight_answer_listening,fourRoot)
        val fourTarget=Target.Builder()
            .setAnchor(binding.linearTestListening)
            .setShape(
                RoundedRectangle(
                    binding.linearTestListening.height.toFloat(),
                    binding.linearTestListening.width.toFloat(),
                    8f
                )
            ).setOverlay(four)
            .setOnTargetListener(object : OnTargetListener {
                override fun onStarted() {
                    four.findViewById<TextView>(R.id.txtSpotlight).text=
                        resources.getString(R.string.spotlight_main_testListening)
                }
                override fun onEnded() {

                }
            })
            .build()
        targets.add(fourTarget)

        val fiveRoot=FrameLayout(this)
        val five=layoutInflater.inflate(R.layout.spotlight_answer_listening,fiveRoot)
        val fiveTarget=Target.Builder()
            .setAnchor(binding.linearTestSpeaking)
            .setShape(
                RoundedRectangle(
                    binding.linearTestSpeaking.height.toFloat(),
                    binding.linearTestSpeaking.width.toFloat(),
                    8f
            )
        ).setOverlay(five)
            .setOnTargetListener(object : OnTargetListener{
                override fun onStarted() {
                    five.findViewById<TextView>(R.id.txtSpotlight).text=
                        resources.getString(R.string.spotlight_main_testSpeaking)
                }

                override fun onEnded() {
                    sessionManager.spotlightMain=false
                    binding.nsvLockable.setScrollingEnabled(true)
                }
            })
            .build()
        targets.add(fiveTarget)


        val spotlight = Spotlight.Builder(this@MainActivity)
            .setTargets(targets)
            .setBackgroundColorRes(R.color.spotlightBackground)
            .setDuration(1000L)
            .setAnimation(DecelerateInterpolator(2f))
            .setOnSpotlightListener(object : OnSpotlightListener {
                override fun onStarted() {
                    //
                }

                override fun onEnded() {
                    //
                }
            })
            .build()

        spotlight.start()


        first.findViewById<View>(R.id.nextTarget).setOnClickListener { spotlight.next() }
        second.findViewById<View>(R.id.nextTarget).setOnClickListener { spotlight.next() }
        third.findViewById<View>(R.id.nextTarget).setOnClickListener{spotlight.next()}
        four.findViewById<View>(R.id.nextTarget).setOnClickListener{spotlight.next()}
        //five.findViewById<View>(R.id.nextTarget).setOnClickListener { spotlight.next() }
        five.findViewById<View>(R.id.nextTarget).visibility = View.GONE


        first.findViewById<View>(R.id.closeSpotlight).setOnClickListener {
            sessionManager.spotlightMain = false
            binding.nsvLockable.setScrollingEnabled(true)
            spotlight.finish()
        }
        second.findViewById<View>(R.id.closeSpotlight).setOnClickListener {
            sessionManager.spotlightMain = false
            binding.nsvLockable.setScrollingEnabled(true)
            spotlight.finish()
        }
        third.findViewById<View>(R.id.closeSpotlight).setOnClickListener {
            sessionManager.spotlightMain=false
            binding.nsvLockable.setScrollingEnabled(true)
            spotlight.finish()
        }
        four.findViewById<View>(R.id.closeSpotlight).setOnClickListener {
            sessionManager.spotlightMain=false
            binding.nsvLockable.setScrollingEnabled(true)
            spotlight.finish()
        }
        five.findViewById<View>(R.id.closeSpotlight).setOnClickListener {
            sessionManager.spotlightMain=false
            binding.nsvLockable.setScrollingEnabled(true)
            spotlight.finish()
        }
    }

}