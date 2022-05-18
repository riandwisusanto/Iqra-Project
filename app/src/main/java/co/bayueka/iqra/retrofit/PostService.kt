package co.bayueka.iqra.retrofit

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST


interface PostService {
    @POST("train")
    open fun uploadWav(
        @Header("Content-Type") contentType: String?,
        @Body body: MultipartBody?
    ): Call<PostModel>
}