package co.bayueka.iqra.mvvm.views.activities

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.bayueka.iqra.R
import co.bayueka.iqra.databinding.ActivityListRecordBinding
import co.bayueka.iqra.mvvm.models.SpeakModel
import co.bayueka.iqra.mvvm.views.adapters.RecordAdapter
import co.bayueka.iqra.retrofit.DataRepository
import co.bayueka.iqra.retrofit.PostModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_list_record.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import java.io.File

class ListRecordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListRecordBinding
    lateinit var recyclerView: RecyclerView
    lateinit var data: ArrayList<SpeakModel>
    lateinit var dataLength: ArrayList<Int>
    lateinit var adapter: RecordAdapter
    lateinit var trainBtn: Button
    private val myRef = Firebase.database.reference
    private var loading: ProgressDialog? = null

    private var baseUrl = ""
    private val TAG = "ListRecordActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponent()
    }
    private fun initComponent(){
        binding.toolbar.txtTitle.text = resources.getString(R.string.listrecord)

        data = arrayListOf()
        dataLength = arrayListOf()
        loadData()

        recyclerView = findViewById(R.id.listRecord)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RecordAdapter(data, this)
        recyclerView.adapter = adapter

        myRef.child("baseUrl").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (vl in snapshot.children){
                        val value = vl.value
                        baseUrl = value.toString()
                        Log.d("base_url", value.toString())
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Child Training: Failed to read value.", error.toException())
            }
        })
        binding.toolbar.imgBack.setOnClickListener {
            finish()
        }


        trainBtn = findViewById<Button>(R.id.startTraining)
        trainBtn.setOnClickListener {
            var tempData   = "1"
            var jumlahData = 0
            data.forEach {
                if(!tempData.equals(it.hijaiyahId)) {
                    dataLength.add(jumlahData)
                    tempData   = it.hijaiyahId!!
                    jumlahData = 1
                }
                else
                    jumlahData++
            }.run {
                dataLength.add(jumlahData)

                if(dataLength.size == dataLength.count {
                        it == jumlahData })
                    train()
                else
                    Toast.makeText(this@ListRecordActivity, "Jumlah suara per Hijaiyah tidak sama", Toast.LENGTH_LONG).show()
            }
//            train()
        }
    }

    private fun loadData(){
        data = arrayListOf()

        myRef.child("hijaiyah")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()) {
                        snapshot.children.forEach {
                            val hijaiyah = it.getValue(InputDataSpeakingActivity.Hijaiyah::class.java)

                            myRef.child("spech").child(hijaiyah!!.huruf.toString())
                                .addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(p0: DataSnapshot) {
                                        p0.children.forEach { p0t ->
                                            val speak = p0t.getValue(SpeakModel::class.java)

                                            data.add(speak!!)
                                        }
                                        adapter.notifyDataSetChanged()
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.w(
                                            "log",
                                            "Child Hijaiyah: Failed to read value.",
                                            error.toException()
                                        )
                                    }

                                })
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG, "Child Hijaiyah: Failed to read value.", error.toException())
                }

            })
    }

    private fun train(){
        trainBtn.text = "TRAINING..."
        trainBtn.isEnabled = false
        txtAkurasi.visibility= View.VISIBLE
        val multipartBody = MultipartBody.Builder()
        for (row in data){
            val fileUri = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS+"/"+row.fileName+".3gp")?.path
            val file = File(fileUri)
            val fileBody = RequestBody.create(MediaType.parse("3gp"), file)

            multipartBody.addFormDataPart("sound[]", row.fileName+".3gp", fileBody)
        }

        val postServices = DataRepository.create(baseUrl)
        val body = multipartBody.build()
        postServices.uploadWav(
            "multipart/form-data; boundary=" + body.boundary(),
            body
        ).enqueue(object : Callback<PostModel> {
            override fun onResponse(call: Call<PostModel>, response: retrofit2.Response<PostModel>) {
                if(response.isSuccessful){
                    trainBtn.text = "START TRAINING"
                    trainBtn.isEnabled = true
                    val data = response.body()
                    txtAkurasi.text="Hasil akurasi train = ${data?.output} %"
                    Toast.makeText(this@ListRecordActivity, "Hasil akurasi train = ${data?.output} %", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<PostModel>, t: Throwable) {
                trainBtn.text = "START TRAINING"
                trainBtn.isEnabled = true
                Toast.makeText(this@ListRecordActivity, "errornya ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("retrofit", "errornya ${t.message}")
            }

        })
    }

    fun showLoading() {
        if (loading == null) {
            loading = ProgressDialog.show(this, null, "Harap Tunggu ...", true, false)
        }
    }
}