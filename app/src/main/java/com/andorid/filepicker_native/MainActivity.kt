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
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {


    val TAG = "MainActivity"


    private lateinit var filePicker: FilePicker

    private lateinit var imageview: ImageView

    private var fileUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageview = findViewById(R.id.imageView)
        filePicker = FilePicker(this, packageName)


    }

    fun onClickCameraButton(view: View) {
      //  showCamera()
        filePicker.takePhotoFromCamera(
            shouldCrop = true,
            filePathUri = {
                imageview.setImageURI(it)
            },
            filePath = {
                imageview.setImageURI(Uri.parse(it))
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


    private fun showCamera() {
        fileUri = dispatchTakePictureIntent(onGetImageFromCameraActivityResult)
    }

    /**
     * Apps targeting for Android 13 or higher requires to declare READ_MEDIA_* to request the media that other apps have created.
     * The READ_EXTERNAL_STORAGE will not work on Android 13 and higher to access the media created by other apps.
     * If the user previously granted app the READ_EXTERNAL_STORAGE permission, the system automatically grants the granular media permission.
     * [More Details](https://developer.android.com/about/versions/13/behavior-changes-13#granular-media-permissions)
     * So for Android 13+ we are asking for permission READ_MEDIA_IMAGES and below that READ_EXTERNAL_STORAGE to get the images from device.
     */


    private val onGetImageFromCameraActivityResult =
        registerActivityResult("Camera", errorCallback = { createSingleSelectionResult(null) }) {
             fileUri?.let { uri -> checkForCropping(uri) } ?: createSingleSelectionResult(null)
        }

        private fun checkForCropping(imageUri: Uri) {
            createSingleSelectionResult(imageUri)
           
            /* if (pickerConfig.openCropOptions || pickerConfig.compressImage) {
                val date =
                    SimpleDateFormat(dateFormatForTakePicture, Locale.getDefault()).format(Date())
                val imageFile = createImageFile(date)
                val cropIntent = UCrop.of(imageUri, Uri.fromFile(imageFile))
                    .withOptions(getUCropOptions()).getIntent(this)
                onCropImageActivityResult.launch(cropIntent)
            } else {
                createSingleSelectionResult(imageUri)
            }*/
        }

    /**
     * Cropping option for the crop screen. Changing colors and setting ui controls.
     */
    private fun createSingleSelectionResult(uri: Uri?) {
        val intent = Intent()
        intent.data = uri
        Log.d(TAG, "createSingleSelectionResult: ${uri?.path}")
        imageview.setImageURI(uri)
       // sendResult(intent)
    }

    private fun sendResult(intent: Intent) {
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onDestroy() {
        //  onCropImageActivityResult.unregister()
        onGetImageFromCameraActivityResult.unregister()
        super.onDestroy()
    }


}