package co.bayueka.iqra.mvvm.views.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import co.bayueka.iqra.BuildConfig
import co.bayueka.iqra.R
import co.bayueka.iqra.databinding.ActivityIqraBinding
import co.bayueka.iqra.mvvm.models.IqraModel
import co.bayueka.iqra.mvvm.viewmodels.IqraViewModel
import co.bayueka.iqra.mvvm.views.adapters.IqraAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class IqraActivity : AppCompatActivity() {

    companion object {
        val IQRA = "${BuildConfig.APPLICATION_ID}_${IqraActivity::class.java.simpleName}_IQRA"
    }

    private lateinit var binding: ActivityIqraBinding
    private val viewModel: IqraViewModel by viewModels()
    private val adapter: IqraAdapter = IqraAdapter()
    private var iqra: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIqraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        iqra = intent?.getIntExtra(IQRA, 1)!!

        initComponents()
        subscribeListeners()
        subscribeObservers()

        viewModel.listIqra(iqra)
    }

    private fun initComponents() {
        binding.apply {
            rvIqra.layoutManager = LinearLayoutManager(this@IqraActivity)
            rvIqra.setHasFixedSize(true)
            rvIqra.setItemViewCacheSize(20)
            rvIqra.adapter = adapter
            toolbar.txtTitle.text = String.format(binding.root.context.resources.getString(R.string.iqra), iqra)
        }
    }

    private fun subscribeListeners() {
        binding.apply {
            toolbar.imgBack.setOnClickListener {
                finish()
            }
            adapter.setListener(object: IqraAdapter.OnListener {
                override fun onClick(position: Int, model: IqraModel) {
                    //
                }

            })
        }
    }
    fun  getPage(i:Int):String
    {
        if (i<10)
        {
            return "0"+i.toString()
        }
        else {
            return i.toString()
        }
    }

    private fun subscribeObservers() {
        lifecycleScope.launch {
            viewModel.iqra.observe(this@IqraActivity, {
                if (it != null) {
                    for (i in 1..it.size - 1) {

                        if (iqra == 1) {
                            var hal = "iqra_1_hal_" + getPage(i)
                            it.get(i).img = resources.getIdentifier(hal, "drawable", packageName)
                            System.out.println("page " + hal)
                        }
                        if (iqra == 2) {
                            var hal = "iqra_2_hal_" + getPage(i)
                            it.get(i).img = resources.getIdentifier(hal, "drawable", packageName)
                            System.out.println("page " + hal)
                        }

                    }
                    adapter.submitList(it)
                    viewModel.iqra.value = null
                }
            })
        }
    }
}