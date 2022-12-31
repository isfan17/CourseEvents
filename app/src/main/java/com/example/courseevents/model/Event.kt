package com.example.courseevents.model

data class Event(
    val id: String,
    val name: String,
    val date: String,
    val time: String,
    val course_id: String
) {
    constructor(): this("", "", "", "", "")
}