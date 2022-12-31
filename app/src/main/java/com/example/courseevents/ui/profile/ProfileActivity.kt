package com.example.courseevents.ui.profile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.example.courseevents.ui.auth.LoginActivity
import com.example.courseevents.R
import com.example.courseevents.databinding.ActivityProfileBinding
import com.example.courseevents.ui.main.MainActivity
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class ProfileActivity : AppCompatActivity(), OnClickListener{
    private var _activityProfileBinding: ActivityProfileBinding? = null
    private val binding get() = _activityProfileBinding

    private var currentPhotoPath: String? = null
    private val RC_Take_Photo = 0
    private val RC_From_Gallery = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _activityProfileBinding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val user = FirebaseAuth.getInstance().currentUser

        supportActionBar?.title = "Profile"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding?.tvName?.text = user?.displayName.toString()
        binding?.tvEmail?.text = user?.email.toString()

        val storageRef = FirebaseStorage.getInstance().reference
        val fotoRef: StorageReference? = user?.let { storageRef.child(it.uid + "/image") }

        val listPageTask: Task<ListResult> = fotoRef!!.list(1)
        listPageTask.addOnSuccessListener {
            val items: List<StorageReference> = it.items
            if (items.isNotEmpty())
            {
                items[0].downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(this@ProfileActivity)
                        .load(uri)
                        .into(binding!!.ivProfile)
                }
            }
        }

        binding?.fabAddImage?.setOnClickListener(this)
        binding?.btnLogout?.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id)
        {
            R.id.fabAddImage -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED
                        || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
                    {
                        val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        requestPermissions(permissions, 100)
                    }
                    else
                    {
                        selectImage()
                    }
                }
                else
                {
                    selectImage()
                }
            }
            R.id.btnLogout -> {
                val alertDialogBuilder = AlertDialog.Builder(this)
                with(alertDialogBuilder)
                {
                    setTitle("Log Out")
                    setMessage("Are you sure want to log out from this account?")
                    setCancelable(false)
                    setPositiveButton("Yes") { _, _ ->
                        FirebaseAuth.getInstance().signOut()
                        startActivity(Intent(applicationContext, LoginActivity::class.java))
                        finish()
                    }
                    setNegativeButton("No") { dialog, _ -> dialog.cancel() }
                }
                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            selectImage()
        }
        else
        {
            Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectImage() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        with(alertDialogBuilder)
        {
            setCancelable(false)
            setTitle("Add Profile Photo")
            setItems(arrayOf("Take Photo", "From Gallery")) { _, pos ->
                if (pos == 0) // Take Photo
                {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    var photoFile: File? = null
                    try {
                        photoFile = createImageFile()
                    } catch (e: IOException) {
                        Toast.makeText(applicationContext, e.message.toString(), Toast.LENGTH_SHORT).show()
                    }

                    if (photoFile != null)
                    {
                        val photoUri: Uri = FileProvider.getUriForFile(this@ProfileActivity, "com.example.courseevents", photoFile)
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                        startActivityForResult(intent, RC_Take_Photo)
                    }
                }
                else if (pos == 1) // From Gallery
                {
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, RC_From_Gallery)
                }
            }
            setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_CANCELED) {
            when(requestCode)
            {
                RC_Take_Photo -> {
                    if (resultCode == RESULT_OK && currentPhotoPath != null)
                    {
                        Glide.with(this@ProfileActivity)
                            .load(File(currentPhotoPath))
                            .into(binding!!.ivProfile)

                        val f = File(currentPhotoPath)
                        val contentUri = Uri.fromFile(f)

                        uploadToStorage(contentUri)
                    }
                }
                RC_From_Gallery -> {
                    if (resultCode == RESULT_OK && data != null)
                    {
                        val selectedImage: Uri? = data.data
                        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                        if (selectedImage != null) {
                            val cursor: Cursor? = contentResolver.query(selectedImage, filePathColumn, null, null, null)
                            if (cursor != null) {
                                cursor.moveToFirst()

                                val columnIndex: Int = cursor.getColumnIndex(filePathColumn[0])
                                val picturePath: String = cursor.getString(columnIndex)

                                Glide.with(this@ProfileActivity)
                                    .load(File(picturePath))
                                    .into(binding!!.ivProfile)
                                cursor.close()

                                uploadToStorage(selectedImage)
                            }
                        }
                    }
                }
            }

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId)
        {
            android.R.id.home -> {
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("ddMMyyy_HHmm").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

        val image: File = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )

        currentPhotoPath = image.absolutePath
        return image
    }

    private fun uploadToStorage(file: Uri) {
        val user = FirebaseAuth.getInstance().currentUser
        val uploadTask: UploadTask
        val storageRef = FirebaseStorage.getInstance().reference
        val fotoRef: StorageReference? = user?.let { storageRef.child(it.uid + "/image/" + it.uid + ".jpg") }

        if (fotoRef != null) {
            uploadTask = fotoRef.putFile(file)
            Toast.makeText(applicationContext, "Uploading Image", Toast.LENGTH_SHORT).show()

            uploadTask.addOnFailureListener {
                Toast.makeText(applicationContext, "Can't Upload Image, " + it.message, Toast.LENGTH_SHORT).show()
            }

            uploadTask.addOnSuccessListener {
                Toast.makeText(applicationContext, "Image Uploaded", Toast.LENGTH_SHORT).show()
            }
        }
    }
}