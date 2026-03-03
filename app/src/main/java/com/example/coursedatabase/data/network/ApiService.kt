package com.example.coursedatabase.data.network

import com.example.coursedatabase.data.dto.PostDto
import retrofit2.http.GET

interface ApiService {

    @GET("posts")
    suspend fun getPosts(): List<PostDto>
}
