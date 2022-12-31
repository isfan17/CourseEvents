package com.example.courseevents.model

data class Course(
    val id: String?,
    val name: String,
    val day: String,
    val startTime: String,
    val endTime: String
) {
    constructor(): this("", "", "", "", "") {}
}