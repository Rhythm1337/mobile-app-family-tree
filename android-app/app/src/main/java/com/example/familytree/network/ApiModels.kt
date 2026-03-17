package com.example.familytree.network

import com.google.gson.annotations.SerializedName

data class MemberDto(
    val id: Int,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("parent_id") val parentId: Int?,
    @SerializedName("image_base64") val imageBase64: String?
)

data class CreateMemberRequest(
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("relation_type") val relationType: String?,
    @SerializedName("related_member_id") val relatedMemberId: Int?,
    @SerializedName("image_base64") val imageBase64: String?
)
