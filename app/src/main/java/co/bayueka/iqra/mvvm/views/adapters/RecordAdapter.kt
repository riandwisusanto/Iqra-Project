package co.bayueka.iqra.mvvm.views.adapters

import android.content.Context
import android.media.MediaPlayer
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.bayueka.iqra.R
import co.bayueka.iqra.mvvm.models.SpeakModel

class RecordAdapter (
    val listData: List<SpeakModel>, context: Context) : RecyclerView.Adapter<RecordAdapter.ViewHolder>() {
    val context = context
    var onMic = false
    lateinit var mediaPlayer: MediaPlayer

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_single_list, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = listData[position]

//        mp.setDataSource(mFileName)
//        mp.prepareAsync()
//        mp.setOnPreparedListener {
//
//        }
//        mp.setOnCompletionListener {
//            holder.btn_play.setImageResource(R.drawable.list_play_btn)
//        }

        holder.title.text = data.fileName
//        holder.hasil.text = "Hasil Suara = "+ data.keyword
        holder.hasil.text = data.date
        holder.btn_play.setOnClickListener {
            val audio = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString()+"/"+data.fileName+".3gp";
            if(onMic){
                this.onMic = false
                holder.btn_play.setImageResource(R.drawable.list_play_btn)

                mediaPlayer.stop()
                mediaPlayer.reset()
                mediaPlayer.release()
            }else{
                this.onMic = true
                holder.btn_play.setImageResource(R.drawable.list_pause_btn)

                mediaPlayer = MediaPlayer()
                mediaPlayer.setDataSource(audio)
                mediaPlayer.prepareAsync()
                mediaPlayer.setOnPreparedListener{ mp -> mp.start() }
                mediaPlayer.setOnCompletionListener {
                    this.onMic = false
                    holder.btn_play.setImageResource(R.drawable.list_play_btn)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val btn_play = ItemView.findViewById<ImageView>(R.id.btnAudio)
        val title = ItemView.findViewById<TextView>(R.id.list_title)
        val hasil = ItemView.findViewById<TextView>(R.id.list_date)
    }
}