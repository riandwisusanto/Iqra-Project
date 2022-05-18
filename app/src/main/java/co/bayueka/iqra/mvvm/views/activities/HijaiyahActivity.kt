package co.bayueka.iqra.mvvm.views.activities

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import co.bayueka.iqra.R
import co.bayueka.iqra.customcomponents.LockableGridLayoutManager
import co.bayueka.iqra.databinding.ActivityHijaiyahBinding
import co.bayueka.iqra.helpers.GridSpacingItemDecorationHelper
import co.bayueka.iqra.mvvm.models.HijaiyahModel
import co.bayueka.iqra.mvvm.viewmodels.HijaiyahViewModel
import co.bayueka.iqra.mvvm.views.adapters.HIjaiyahAdapter
import co.bayueka.iqra.utils.SessionManager
import com.takusemba.spotlight.OnSpotlightListener
import com.takusemba.spotlight.OnTargetListener
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.Target
import com.takusemba.spotlight.shape.RoundedRectangle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HijaiyahActivity : AppCompatActivity() {

    private val viewModel: HijaiyahViewModel by viewModels()
    @Inject lateinit var sessionManager: SessionManager

    private lateinit var binding: ActivityHijaiyahBinding
    private lateinit var layoutManager: LockableGridLayoutManager
    private val adapter: HIjaiyahAdapter = HIjaiyahAdapter()
    private var mediaPlayer: MediaPlayer? = null
    private var positionClick = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHijaiyahBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponents()
        subscribeListeners()
        subscribeObservers()

        viewModel.listHijaiyah("")
        if (sessionManager.spotlightHijaiyah) {
            layoutManager.setScrollingEnabled(false)
            Handler(Looper.getMainLooper()).postDelayed({
                startSpotlight()
            }, 500)
        } else {
            layoutManager.setScrollingEnabled(true)
        }

    }

    private fun initComponents() {
        binding.apply {
            layoutManager = LockableGridLayoutManager(this@HijaiyahActivity, 5)
            rvHijaiyah.layoutManager = layoutManager
            rvHijaiyah.setHasFixedSize(true)
            rvHijaiyah.setItemViewCacheSize(15)
            rvHijaiyah.addItemDecoration(GridSpacingItemDecorationHelper(5, 4, true))
            rvHijaiyah.adapter = adapter
            toolbar.txtTitle.text = root.context.resources.getString(R.string.hijaiyah)
        }
    }

    private fun subscribeListeners() {
        adapter.setListener(object : HIjaiyahAdapter.OnListener {
            override fun onClick(position: Int, model: HijaiyahModel) {
                if (!sessionManager.spotlightHijaiyah) {
                    if (model.sound != HijaiyahModel.NO_SOUND) {
                        if (mediaPlayer != null) {
                            if (position == positionClick && mediaPlayer!!.isPlaying) {
                                //
                            } else {
                                mediaPlayer = MediaPlayer.create(this@HijaiyahActivity, model.sound)
                                mediaPlayer!!.start()
                            }
                        } else {
                            mediaPlayer = MediaPlayer.create(this@HijaiyahActivity, model.sound)
                            mediaPlayer!!.start()
                        }
                        positionClick = position
                    } else {
                        Toast.makeText(this@HijaiyahActivity, model.title, Toast.LENGTH_SHORT).show()
                    }
                }
            }

        })
        binding.apply {
            toolbar.imgBack.setOnClickListener {
                finish()
            }
        }
    }

    private fun subscribeObservers() {
        lifecycleScope.launch {
            viewModel.hijaiyah.observe(this@HijaiyahActivity, {
                if (it != null) {
                    adapter.submitList(it)
                    viewModel.hijaiyah.value = null
                }
            })
        }
    }

    private fun startSpotlight() {
        val targets = ArrayList<Target>()
        val firstRoot = FrameLayout(this)
        val first = layoutInflater.inflate(R.layout.spotlight_main, firstRoot)
        val firstTarget = Target.Builder()
            .setAnchor(binding.rvHijaiyah)
            .setShape(RoundedRectangle(binding.rvHijaiyah.height.toFloat(), binding.rvHijaiyah.width.toFloat(), 8F))
            .setOverlay(first)
            .setOnTargetListener(object : OnTargetListener {
                override fun onStarted() {
                    first.findViewById<TextView>(R.id.txtSpotlight).text = ""
                }

                override fun onEnded() {
                    //
                }
            })
            .build()

        targets.add(firstTarget)

        val firstRow = binding.rvHijaiyah.findViewHolderForAdapterPosition(4)?.itemView!!
        val secondRoot = FrameLayout(this)
        val second = layoutInflater.inflate(R.layout.spotlight_main, secondRoot)
        val secondTarget = Target.Builder()
            .setAnchor(firstRow)
            .setShape(RoundedRectangle(firstRow.height.toFloat(), firstRow.width.toFloat(), 8F))
            .setOverlay(second)
            .setOnTargetListener(object : OnTargetListener {
                override fun onStarted() {
                    second.findViewById<TextView>(R.id.txtSpotlight).text = resources.getString(R.string.spotlight_hijaiyah_suara)
                }

                override fun onEnded() {
                    sessionManager.spotlightHijaiyah = false
                    layoutManager.setScrollingEnabled(true)
                }
            })
            .build()

        targets.add(secondTarget)

        val spotlight = Spotlight.Builder(this@HijaiyahActivity)
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
        //second.findViewById<View>(R.id.nextTarget).setOnClickListener { spotlight.next() }
        second.findViewById<View>(R.id.nextTarget).visibility=View.GONE



        first.findViewById<View>(R.id.closeSpotlight).setOnClickListener {
            sessionManager.spotlightHijaiyah = false
            layoutManager.setScrollingEnabled(true)
            spotlight.finish()
        }
        second.findViewById<View>(R.id.closeSpotlight).setOnClickListener {
            sessionManager.spotlightHijaiyah = false
            layoutManager.setScrollingEnabled(true)
            spotlight.finish()
        }
    }
}