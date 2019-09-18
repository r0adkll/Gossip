package com.r0adkll.gossip.arch.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val id: String = "",
    val name: String = "",
    val avatarUrl: String = ""
): Parcelable
