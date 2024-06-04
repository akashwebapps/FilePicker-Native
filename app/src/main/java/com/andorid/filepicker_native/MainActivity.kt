package com.andorid.filepicker_native

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import com.andorid.filepicker.FilePicker
import com.andorid.filepicker_native.utils.Exfn.Companion.sizeInKb
import com.canhub.cropper.CropImageView
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {


    val TAG = "MainActivity"


    private lateinit var filePicker: FilePicker

    private lateinit var imageview: CropImageView

    private var fileUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageview = findViewById(R.id.imageView)
        filePicker = FilePicker(this, packageName)
        imageview.setAspectRatio(2,3)


    }

    fun onClickCameraButton(view: View) {
      //  showCamera()
        filePicker.takePhotoFromCamera(
            shouldCrop = true,
            filePathUri = {
                imageview.setImageUriAsync(it)
            },
            filePath = {
                imageview.setImageUriAsync(Uri.parse(it))
            }
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        filePicker.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun onClickGalleryButton(view: View) {

    }





}