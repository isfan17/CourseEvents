package com.example.courseevents.ui.insert

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.courseevents.databinding.ActivityEventAddUpdateBinding
import com.example.courseevents.model.Event
import java.util.*

class EventAddUpdateActivity : AppCompatActivity() {
    private var _activityEventAddUpdateBinding: ActivityEventAddUpdateBinding? = null
    private val binding get() = _activityEventAddUpdateBinding

    private var isEdit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _activityEventAddUpdateBinding = ActivityEventAddUpdateBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val id = intent.getStringExtra(EXTRA_EVENT_ID)
        val name = intent.getStringExtra(EXTRA_EVENT_NAME)
        val date = intent.getStringExtra(EXTRA_EVENT_DATE)
        val time = intent.getStringExtra(EXTRA_EVENT_TIME)

        if (id != null) {
            isEdit = true
        }

        val actionBarTitle: String
        val btnTitle: String

        if (isEdit)
        {
            actionBarTitle = "Update Event Form"
            btnTitle = "Update Event"
            binding?.edtName?.setText(name)
            binding?.edtDate?.setText(date)
            binding?.edtTime?.setText(time)

            binding?.btnDelete?.visibility = View.VISIBLE
            binding?.btnDelete?.setOnClickListener {
                val alertDialogBuilder = AlertDialog.Builder(this)
                with(alertDialogBuilder)
                {
                    setTitle("Delete Event")
                    setMessage("Are you sure want to delete this event?")
                    setCancelable(false)
                    setPositiveButton("Yes") { _, _ ->
                        if (id != null) {
                            deleteEventData(id)
                        }
                        finish()
                    }
                    setNegativeButton("No") { dialog, _ -> dialog.cancel() }
                }
                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
            }
        }
        else
        {
            actionBarTitle = "Create Event Form"
            btnTitle = "Create Event"
        }

        supportActionBar?.title = actionBarTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Event Date DatePicker
        binding?.edtDate?.inputType = InputType.TYPE_NULL
        binding?.edtDate?.setOnFocusChangeListener { _, b ->
            if (b) { showDateDialog(binding!!.edtDate) }
        }
        binding?.edtDate?.setOnClickListener {
            showDateDialog(binding!!.edtDate)
        }

        // Event Time TimePicker
        binding?.edtTime?.inputType = InputType.TYPE_NULL
        binding?.edtTime?.setOnFocusChangeListener { _, b ->
            if (b) { showTimeDialog(binding!!.edtTime) }
        }
        binding?.edtTime?.setOnClickListener{
            showTimeDialog(binding!!.edtTime)
        }

        binding?.btnSubmit?.text = btnTitle
        binding?.btnSubmit?.setOnClickListener {
            val nameInput = binding?.edtName?.text.toString().trim()
            val dateInput = binding?.edtDate?.text.toString().trim()
            val timeInput = binding?.edtTime?.text.toString().trim()
            when
            {
                nameInput.isEmpty() -> {
                    binding?.edtName?.error = "Field can not be blank"
                }
                dateInput.isEmpty() -> {
                    binding?.edtDate?.error = "Field can not be blank"
                }
                timeInput.isEmpty() -> {
                    binding?.edtTime?.error = "Field can not be blank"
                }
                else -> {
                    if (isEdit)
                    {
                        if (id != null) {
                            updateEventData(id, nameInput, dateInput, timeInput)
                        }
                    }
                    else
                    {
                        createEventData(nameInput, dateInput, timeInput)
                    }
                    finish()
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

    private fun createEventData(name: String, date: String, time: String) {
        val courseId = intent.getStringExtra(EXTRA_COURSE_ID)
        val ref = FirebaseDatabase.getInstance().getReference("event")
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null && courseId != null) {
            val eventId = ref.child(user.uid).child(courseId).push().key
            val event = eventId?.let { Event(it, name, date, time, courseId) }

            if (eventId != null) {
                ref.child(user.uid).child(courseId).child(eventId).setValue(event)
            }
            Toast.makeText(applicationContext, "Event Successfully Added", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateEventData(id: String, name: String, date: String, time: String) {
        val ref = FirebaseDatabase.getInstance().getReference("event")
        val user = FirebaseAuth.getInstance().currentUser
        val courseId = intent.getStringExtra(EXTRA_COURSE_ID)
        val event = courseId?.let { Event(id, name, date, time, it) }

        if (user != null && courseId != null) {
            ref.child(user.uid).child(courseId).child(id).setValue(event)
            Toast.makeText(applicationContext, "Event Successfully Updated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteEventData(id: String) {
        val user = FirebaseAuth.getInstance().currentUser
        val courseId = intent.getStringExtra(EXTRA_COURSE_ID)
        if (user != null && courseId != null)
        {
            val eventRef = FirebaseDatabase.getInstance().getReference("event").child(user.uid).child(courseId).child(id)
            eventRef.removeValue()
        }
        Toast.makeText(applicationContext, "Event Successfully Deleted", Toast.LENGTH_SHORT).show()
    }

    private fun showDateDialog(date: EditText) {
        val datePicker: DatePickerDialog
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        datePicker = DatePickerDialog(this@EventAddUpdateActivity, object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                date.setText(String.format("%02d/%02d/%04d", dayOfMonth, month, year))
            }

        }, currentYear, currentMonth, currentDay)

        datePicker.show()
    }

    private fun showTimeDialog(time: EditText) {
        val timePicker: TimePickerDialog
        val currentTime = Calendar.getInstance()
        val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
        val currentMinute = currentTime.get(Calendar.MINUTE)

        timePicker = TimePickerDialog(this@EventAddUpdateActivity, object : TimePickerDialog.OnTimeSetListener {
            override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                currentTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                currentTime.set(Calendar.MINUTE, minute)

                time.setText(String.format("%02d:%02d", hourOfDay, minute))
            }
        }, currentHour, currentMinute, false)

        timePicker.show()
    }

    companion object {
        const val EXTRA_COURSE_ID = "extra_course_id"
        const val EXTRA_EVENT_ID = "extra_event_id"
        const val EXTRA_EVENT_NAME = "extra_event_name"
        const val EXTRA_EVENT_DATE = "extra_event_date"
        const val EXTRA_EVENT_TIME = "extra_event_time"
    }
}