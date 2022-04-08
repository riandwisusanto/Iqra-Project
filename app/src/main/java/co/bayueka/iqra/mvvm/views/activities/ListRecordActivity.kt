package co.bayueka.iqra.mvvm.views.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.bayueka.iqra.R
import co.bayueka.iqra.databinding.ActivityListRecordBinding
import co.bayueka.iqra.mvvm.models.TrainingHijaiyahModel
import co.bayueka.iqra.mvvm.views.adapters.RecordAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ListRecordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListRecordBinding
    lateinit var recyclerView: RecyclerView
    lateinit var data: ArrayList<TrainingHijaiyahModel>
    lateinit var adapter: RecordAdapter
    private val myRef = Firebase.database.reference

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

//        myRef.child("baseUrl").addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.exists()){
//                    for (vl in snapshot.children){
//                        val value = vl.value
//                        baseUrl = value.toString()
//                    }
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Log.w(TAG, "Child Training: Failed to read value.", error.toException())
//            }
//        })

        binding.toolbar.imgBack.setOnClickListener {
            finish()
        }
    }

    private fun loadData(){
        data = arrayListOf()

        myRef.child("training").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val training = it.getValue(TrainingHijaiyahModel::class.java)

                    data.add(training!!)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("log", "Child Hijaiyah: Failed to read value.", error.toException())
            }

        })
//        val storageDirectory =getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath
//
//        File(storageDirectory)
//            .walk()
//            .forEach {
//                if (!it.name.equals("Download"))
//                    data.add(it.name)
//            }
    }
}