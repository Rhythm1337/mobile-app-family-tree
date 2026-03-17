package com.example.familytree.repository

import com.example.familytree.network.ApiClient
import com.example.familytree.network.CreateMemberRequest
import com.example.familytree.network.MemberDto

class MemberRepository {
    suspend fun fetchMembers(): List<MemberDto> = ApiClient.api.getMembers()

    suspend fun fetchMember(id: Int): MemberDto = ApiClient.api.getMember(id)

    suspend fun addMember(request: CreateMemberRequest): MemberDto = ApiClient.api.createMember(request)
}
