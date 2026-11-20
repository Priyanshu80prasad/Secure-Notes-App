package com.priyanshu.securenotes.models

data class Note (
    val noteID : String = "",
    val title : String ="",
    val description : String ="",
    val createdAt : Long = 0L,
    val updatedAt: Long = 0L
)