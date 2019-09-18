package com.r0adkll.gossip.arch.domain.user

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class User(
    val id: String = "",
    val name: String = "",
    val avatarUrl: String = ""
) : Parcelable
