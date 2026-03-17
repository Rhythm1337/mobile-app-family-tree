package com.example.familytree.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("members")
    suspend fun getMembers(): List<MemberDto>

    @GET("members/{id}")
    suspend fun getMember(@Path("id") id: Int): MemberDto

    @POST("members")
    suspend fun createMember(@Body request: CreateMemberRequest): MemberDto
}
