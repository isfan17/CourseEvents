package com.example.courseevents.ui.insert

import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import com.example.courseevents.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.courseevents.databinding.ActivityCourseAddUpdateBinding
import com.example.courseevents.model.Course
import com.example.courseevents.ui.main.MainActivity
import java.text.SimpleDateFormat
import java.util.Calendar


class CourseAddUpdateActivity : AppCompatActivity() {
    private var _activityCourseAddUpdateBinding: ActivityCourseAddUpdateBinding? = null
    private val binding get() = _activityCourseAddUpdateBinding

    private var isEdit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _activityCourseAddUpdateBinding = ActivityCourseAddUpdateBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val id = intent.getStringExtra(EXTRA_ID)
        val name = intent.getStringExtra(EXTRA_NAME)
        val day = intent.getStringExtra(EXTRA_DAY)
        val startTime = intent.getStringExtra(EXTRA_START_TIME)
        val endTime = intent.getStringExtra(EXTRA_END_TIME)

        if (id != null) {
            isEdit = true
        }

        val actionBarTitle: String
        val btnTitle: String

        if (isEdit)
        {
            actionBarTitle = "Update Course Form"
            btnTitle = "Update Course"
            binding?.edtName?.setText(name)
            binding?.atvDay?.setText(day)
            binding?.edtStartTime?.setText(startTime)
            binding?.edtEndTime?.setText(endTime)
        }
        else
        {
            actionBarTitle = "Create Course Form"
            btnTitle = "Create Course"
        }

        supportActionBar?.title = actionBarTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Input Day Dropdown
        val dayItems = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val arrayAdapter = ArrayAdapter(this, R.layout.item_dropdown_day, dayItems)
        binding?.atvDay?.setAdapter(arrayAdapter)

        // Start Time TimePicker
        binding?.edtStartTime?.inputType = InputType.TYPE_NULL
        binding?.edtStartTime?.setOnFocusChangeListener { _, b ->
            if (b) { showTimeDialog(binding!!.edtStartTime) }
        }
        binding?.edtStartTime?.setOnClickListener{
            showTimeDialog(binding!!.edtStartTime)
        }

        // End Time TimePicker
        binding?.edtEndTime?.inputType = InputType.TYPE_NULL
        binding?.edtEndTime?.setOnFocusChangeListener { _, b ->
            if (b) { showTimeDialog(binding!!.edtEndTime) }
        }
        binding?.edtEndTime?.setOnClickListener {
            showTimeDialog(binding!!.edtEndTime)
        }

        binding?.btnSubmit?.text = btnTitle
        binding?.btnSubmit?.setOnClickListener {
            val nameInput = binding?.edtName?.text.toString().trim()
            val dayInput = binding?.atvDay?.text.toString().trim()
            val startTimeInput = binding?.edtStartTime?.text.toString().trim()
            val endTimeInput = binding?.edtEndTime?.text.toString().trim()
            when
            {
                nameInput.isEmpty() -> {
                    binding?.edtName?.error = "Field can not be blank"
                }
                dayInput.isEmpty() -> {
                    binding?.atvDay?.error = "Field can not be blank"
                }
                startTimeInput.isEmpty() -> {
                    binding?.edtStartTime?.error = "Field can not be blank"
                }
                endTimeInput.isEmpty() -> {
                    binding?.edtEndTime?.error = "Field can not be blank"
                }
                else -> {
                    if (isEdit)
                    {
                        if (id != null) {
                            updateCourseData(id, nameInput, dayInput, startTimeInput, endTimeInput)
                        }
                    }
                    else
                    {
                        createCourseData(nameInput, dayInput, startTimeInput, endTimeInput)
                    }
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId)
        {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createCourseData(name: String, day: String, startTime: String, endTime: String) {
        val ref = FirebaseDatabase.getInstance().getReference("course")
        val user = FirebaseAuth.getInstance().currentUser

        val courseId = user?.let { ref.child(it.uid).push().key }
        val course = Course(courseId, name, day, startTime, endTime)

        if (user != null && courseId != null) {
            ref.child(user.uid).child(courseId).setValue(course)
            Toast.makeText(applicationContext, "Course Successfully Added", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCourseData(id: String, name: String, day: String, startTime: String, endTime: String) {
        val ref = FirebaseDatabase.getInstance().getReference("course")
        val user = FirebaseAuth.getInstance().currentUser
        val course = Course(id, name, day, startTime, endTime)

        if (user != null) {
            ref.child(user.uid).child(id).setValue(course)
            Toast.makeText(applicationContext, "Course Successfully Updated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showTimeDialog(time: EditText) {
        val timePicker: TimePickerDialog
        val currentTime = Calendar.getInstance()
        val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
        val currentMinute = currentTime.get(Calendar.MINUTE)

        timePicker = TimePickerDialog(this@CourseAddUpdateActivity, object : TimePickerDialog.OnTimeSetListener {
            override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                currentTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                currentTime.set(Calendar.MINUTE, minute)

                time.setText(String.format("%02d:%02d", hourOfDay, minute))
            }
        }, currentHour, currentMinute, false)

        timePicker.show()
    }

    companion object {
        const val EXTRA_ID = "extra_id"
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_DAY = "extra_day"
        const val EXTRA_START_TIME = "extra_start_time"
        const val EXTRA_END_TIME = "extra_end_time"
    }
}