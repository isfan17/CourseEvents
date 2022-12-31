package com.example.courseevents.ui.detail

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.courseevents.R
import com.example.courseevents.databinding.ActivityDetailBinding
import com.example.courseevents.model.Event
import com.example.courseevents.ui.insert.CourseAddUpdateActivity
import com.example.courseevents.ui.insert.EventAddUpdateActivity
import com.example.courseevents.ui.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*


class DetailActivity : AppCompatActivity() {
    private var _activityDetailBinding: ActivityDetailBinding? = null
    private val binding get() = _activityDetailBinding

    private var id: String? = null
    private var name: String? = null
    private var day: String? = null
    private var startTime: String? = null
    private var endTime: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _activityDetailBinding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        id = intent.getStringExtra(EXTRA_ID)
        name = intent.getStringExtra(EXTRA_NAME)
        day = intent.getStringExtra(EXTRA_DAY)
        startTime = intent.getStringExtra(EXTRA_START_TIME)
        endTime = intent.getStringExtra(EXTRA_END_TIME)

        binding?.apply {
            tvCourseName.text = name
            tvCourseDay.text = day
            tvCourseStartTime.text = startTime
            tvCourseEndTime.text = endTime
        }

        val layoutManager = LinearLayoutManager(this)
        binding?.rvEvents?.layoutManager = layoutManager
        binding?.rvEvents?.setHasFixedSize(true)

        // Read Database
        val user = FirebaseAuth.getInstance().currentUser
        val listEvents = ArrayList<Event>()
        if (user != null && id != null) {
            showLoading(true)
            FirebaseDatabase.getInstance().getReference("event").child(user.uid).child(id!!).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists())
                    {
                        listEvents.clear()
                        for (item in snapshot.children)
                        {
                            val event = item.getValue(Event::class.java)
                            if (event != null)
                            {
                                listEvents.add(event)
                            }

                            listEvents.sortWith { o1, o2 ->
                                o1.date.compareTo(o2.date)
                            }

                            val adapter = EventAdapter(this@DetailActivity, listEvents)
                            binding?.rvEvents?.adapter = adapter
                            binding?.ivEmpty?.visibility = View.GONE
                            binding?.tvEmpty?.visibility = View.GONE
                        }
                        showLoading(false)
                    }
                    else
                    {
                        val adapter = EventAdapter(this@DetailActivity, listEvents)
                        if (adapter.itemCount == 0)
                        {
                            showLoading(false)
                            binding?.ivEmpty?.visibility = View.VISIBLE
                            binding?.tvEmpty?.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(applicationContext, "Failed to Load Event Data", Toast.LENGTH_SHORT).show()
                    showLoading(false)
                }

            })
        }

        // Search Bar Function
        binding?.svSearch?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val searchResultList = newText?.let { searchList(it, listEvents) }
                val adapter = searchResultList?.let { EventAdapter(this@DetailActivity, it) }
                binding?.rvEvents?.adapter = adapter

                if (adapter?.itemCount == 0)
                {
                    binding?.ivEmpty?.visibility = View.VISIBLE
                    binding?.tvEmpty?.text = "event not found"
                    binding?.tvEmpty?.visibility = View.VISIBLE
                }
                else
                {
                    binding?.ivEmpty?.visibility = View.GONE
                    binding?.tvEmpty?.text = getString(R.string.empty_event)
                    binding?.tvEmpty?.visibility = View.GONE
                }

                return true
            }
        })

        binding?.fabAdd?.setOnClickListener {
            val intent = Intent(applicationContext, EventAddUpdateActivity::class.java)
            intent.putExtra(EventAddUpdateActivity.EXTRA_COURSE_ID, id)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_detail, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId)
        {
            R.id.action_update -> {
                val intent = Intent(applicationContext, CourseAddUpdateActivity::class.java)
                intent.putExtra(CourseAddUpdateActivity.EXTRA_ID, id)
                intent.putExtra(CourseAddUpdateActivity.EXTRA_NAME, name)
                intent.putExtra(CourseAddUpdateActivity.EXTRA_DAY, day)
                intent.putExtra(CourseAddUpdateActivity.EXTRA_START_TIME, startTime)
                intent.putExtra(CourseAddUpdateActivity.EXTRA_END_TIME, endTime)
                startActivity(intent)
            }
            R.id.action_delete -> {
                val alertDialogBuilder = AlertDialog.Builder(this)
                with(alertDialogBuilder)
                {
                    setTitle("Delete Course")
                    setMessage("Are you sure want to delete this course and its events?")
                    setCancelable(false)
                    setPositiveButton("Yes") { _, _ ->
                        deleteCourse()
                        startActivity(Intent(applicationContext, MainActivity::class.java))
                        finish()
                    }
                    setNegativeButton("No") { dialog, _ -> dialog.cancel() }
                }
                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
            }
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteCourse() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null)
        {
            val courseRef = FirebaseDatabase.getInstance().getReference("course").child(user.uid).child(id!!)
            val eventRef = FirebaseDatabase.getInstance().getReference("event").child(user.uid).child(id!!)
            courseRef.removeValue()
            eventRef.removeValue()
        }
        Toast.makeText(applicationContext, "Course Successfully Deleted", Toast.LENGTH_SHORT).show()
    }

    private fun searchList(text: String, eventList: ArrayList<Event>): ArrayList<Event> {
        val searchList = ArrayList<Event>()
        eventList.forEach {
            if (it.name.toLowerCase().contains(text.lowercase()))
            {
                searchList.add(it)
            }
        }
        return searchList
    }

    private fun showLoading(boolean: Boolean) {
        if (boolean)
        {
            binding?.progressBar?.visibility = View.VISIBLE
        }
        else
        {
            binding?.progressBar?.visibility = View.GONE
        }
    }

    companion object {
        const val EXTRA_ID = "extra_id"
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_DAY = "extra_day"
        const val EXTRA_START_TIME = "extra_start_time"
        const val EXTRA_END_TIME = "extra_end_time"
    }

}