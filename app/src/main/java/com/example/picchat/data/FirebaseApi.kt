package com.example.picchat.data

import com.example.picchat.other.Constants
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface FirebaseApi {

    @Headers("Authorization: key=${Constants.SERVER_KEY}", "Content-Type:${Constants.CONTENT_TYPE}")
    @POST("fcm/send")
    suspend fun postNotification(@Body notification: PushNotification): Response<ResponseBody>

}