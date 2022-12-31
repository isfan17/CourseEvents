package com.example.courseevents.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.courseevents.R
import com.example.courseevents.databinding.ActivityMainBinding
import com.example.courseevents.model.Course
import com.example.courseevents.ui.detail.DetailActivity
import com.example.courseevents.ui.insert.CourseAddUpdateActivity
import com.example.courseevents.ui.profile.ProfileActivity
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private var _activityMainBinding: ActivityMainBinding? = null
    private val binding get() = _activityMainBinding

    private var courseList = ArrayList<Course>()
    lateinit var adapter: CourseAdapter

    private var user: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        supportActionBar?.hide();

        user = FirebaseAuth.getInstance().currentUser

        if (user != null)
        {
            binding?.tvName?.text = "Hi, ${user?.displayName}"
        }
        else
        {
            binding?.tvName?.text = "Login Failed"
        }

        val layoutManager = LinearLayoutManager(this)
        binding?.rvCourses?.layoutManager = layoutManager
        binding?.rvCourses?.setHasFixedSize(true)

        // Load Image Profile
        val storageRef = FirebaseStorage.getInstance().reference
        val fotoRef: StorageReference? = user?.let { storageRef.child(it.uid + "/image") }

        val listPageTask: Task<ListResult> = fotoRef!!.list(1)
        listPageTask.addOnSuccessListener {
            val items: List<StorageReference> = it.items
            if (items.isNotEmpty())
            {
                items[0].downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(this@MainActivity)
                        .load(uri)
                        .into(binding!!.ivProfile)
                }
            }
        }

        // Read Database
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null)
        {
            showLoading(true)
            FirebaseDatabase.getInstance().getReference("course").child(user.uid).addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists())
                    {
                        courseList.clear()
                        for (item in snapshot.children)
                        {
                            val course = item.getValue(Course::class.java)
                            if (course != null)
                            {
                                courseList.add(course)
                            }
                        }
                        adapter = CourseAdapter(courseList)
                        binding?.rvCourses?.adapter = adapter
                        binding?.ivEmpty?.visibility = View.GONE
                        binding?.tvEmpty?.visibility = View.GONE

                        adapter.setOnItemClickCallback(object : CourseAdapter.OnItemClickCallback {
                            override fun onItemClicked(data: Course) {
                                val intent = Intent(this@MainActivity, DetailActivity::class.java)
                                intent.putExtra(DetailActivity.EXTRA_ID, data.id)
                                intent.putExtra(DetailActivity.EXTRA_NAME, data.name)
                                intent.putExtra(DetailActivity.EXTRA_DAY, data.day)
                                intent.putExtra(DetailActivity.EXTRA_START_TIME, data.startTime)
                                intent.putExtra(DetailActivity.EXTRA_END_TIME, data.endTime)
                                startActivity(intent)
                            }
                        })
                        showLoading(false)
                    }
                    else
                    {
                        adapter = CourseAdapter(courseList)
                        if (adapter.itemCount == 0)
                        {
                            showLoading(false)
                            binding?.ivEmpty?.visibility = View.VISIBLE
                            binding?.tvEmpty?.visibility = View.VISIBLE
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(applicationContext, "Failed to Load Course Data", Toast.LENGTH_SHORT).show()
                    showLoading(false)
                }
            })
        }

        // Search Bar Function
        binding?.svSearch?.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val searchResultList = newText?.let { searchList(it, courseList) }
                adapter = searchResultList?.let { CourseAdapter(it) }!!
                binding?.rvCourses?.adapter = adapter

                if (adapter.itemCount == 0)
                {
                    binding?.ivEmpty?.visibility = View.VISIBLE
                    binding?.tvEmpty?.text = "course not found"
                    binding?.tvEmpty?.visibility = View.VISIBLE
                }
                else
                {
                    binding?.ivEmpty?.visibility = View.GONE
                    binding?.tvEmpty?.text = getString(R.string.empty_course)
                    binding?.tvEmpty?.visibility = View.GONE
                }

                adapter.setOnItemClickCallback(object : CourseAdapter.OnItemClickCallback {
                    override fun onItemClicked(data: Course) {
                        val intent = Intent(this@MainActivity, DetailActivity::class.java)
                        intent.putExtra(DetailActivity.EXTRA_ID, data.id)
                        intent.putExtra(DetailActivity.EXTRA_NAME, data.name)
                        intent.putExtra(DetailActivity.EXTRA_DAY, data.day)
                        intent.putExtra(DetailActivity.EXTRA_START_TIME, data.startTime)
                        intent.putExtra(DetailActivity.EXTRA_END_TIME, data.endTime)
                        startActivity(intent)
                    }
                })
                return true
            }
        })

        // Sort and Filter Function
        binding?.btnFilter?.setOnClickListener {
            setFilterData()
        }

        binding?.fabAdd?.setOnClickListener {
            startActivity(Intent(applicationContext, CourseAddUpdateActivity::class.java))
        }

        binding?.ivProfile?.setOnClickListener {
            startActivity(Intent(applicationContext, ProfileActivity::class.java))
        }
    }

    private fun setFilterData() {
        val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_filter, null)

        val bottomSheetDialog = BottomSheetDialog(this@MainActivity)
        bottomSheetDialog.setContentView(dialogView)
        bottomSheetDialog.show()

        val dateComparatorAsc: Comparator<Course> = object : Comparator<Course> {
            override fun compare(o1: Course, o2: Course): Int {
                try {
                    val format = SimpleDateFormat("EEE")
                    val d1: Date = format.parse(o1.day) as Date
                    val d2: Date = format.parse(o2.day) as Date
                    if (d1 == d2)
                    {
                        return o1.startTime.compareTo(o2.startTime)
                    }
                    else
                    {
                        val cal1: Calendar = Calendar.getInstance()
                        val cal2: Calendar = Calendar.getInstance()
                        cal1.time = d1
                        cal2.time = d2

                        var cal1Value = cal1.get(Calendar.DAY_OF_WEEK)
                        var cal2Value = cal2.get(Calendar.DAY_OF_WEEK)
                        if (cal1Value == 1) cal1Value = 8
                        if (cal2Value == 1) cal2Value = 8

                        return cal1Value - cal2Value
                    }
                } catch (pe: ParseException) {
                    throw RuntimeException(pe)
                }
            }
        }

        val dateComparatorDesc: Comparator<Course> = object : Comparator<Course> {
            override fun compare(o1: Course, o2: Course): Int {
                try {
                    val format = SimpleDateFormat("EEE")
                    val d1: Date = format.parse(o1.day) as Date
                    val d2: Date = format.parse(o2.day) as Date
                    if (d1 == d2)
                    {
                        return o1.startTime.compareTo(o2.startTime)
                    }
                    else
                    {
                        val cal1: Calendar = Calendar.getInstance()
                        val cal2: Calendar = Calendar.getInstance()
                        cal1.time = d1
                        cal2.time = d2

                        var cal1Value = cal1.get(Calendar.DAY_OF_WEEK)
                        var cal2Value = cal2.get(Calendar.DAY_OF_WEEK)
                        if (cal1Value == 1) cal1Value = 8
                        if (cal2Value == 1) cal2Value = 8

                        return cal2Value - cal1Value
                    }
                } catch (pe: ParseException) {
                    throw RuntimeException(pe)
                }
            }
        }

        val rgFilter: RadioGroup = dialogView.findViewById(R.id.rgFilter)
        rgFilter.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.rbDayAsc -> {
                    Collections.sort(courseList, dateComparatorAsc)
                    adapter.notifyDataSetChanged()
                }
                R.id.rbDayDesc -> {
                    Collections.sort(courseList, dateComparatorDesc)
                    adapter.notifyDataSetChanged()
                }
                R.id.rbNameAZ -> {
                    courseList.sortWith { o1, o2 ->
                        o1.name.compareTo(o2.name)
                    }
                    adapter.notifyDataSetChanged()
                }
                R.id.rbNameZA -> {
                    courseList.sortWith { o1, o2 ->
                        o2.name.compareTo(o1.name)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
        }

    }

    private fun searchList(text: String, courseList: ArrayList<Course>): ArrayList<Course> {
        val searchList = ArrayList<Course>()
        courseList.forEach {
            if (it.name.toLowerCase().contains(text.toLowerCase()))
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
}