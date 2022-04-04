package co.bayueka.iqra.mvvm.views.activities

import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.bayueka.iqra.R
import co.bayueka.iqra.databinding.ActivityListRecordBinding
import co.bayueka.iqra.mvvm.views.adapters.RecordAdapter
import java.io.File

class ListRecordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListRecordBinding
    lateinit var recyclerView: RecyclerView
    lateinit var data: ArrayList<String>
    lateinit var adapter: RecordAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadData()
        initComponent()
    }

    private fun initComponent(){
        binding.toolbar.txtTitle.text = resources.getString(R.string.listrecord)

        recyclerView = findViewById(R.id.listRecord)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RecordAdapter(data, this)
        recyclerView.adapter = adapter

        binding.toolbar.imgBack.setOnClickListener {
            finish()
        }
    }

    private fun loadData(){
        data = arrayListOf()

        val storageDirectory =getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath

        File(storageDirectory)
            .walk()
            .forEach {
                if (!it.name.equals("Download"))
                    data.add(it.name)
            }
    }
}