package com.andorid.filepicker_native

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.andorid.filepicker.FilePicker
import com.andorid.filepicker_native.utils.Exfn.Companion.sizeInKb
import java.io.File

class MainActivity : AppCompatActivity() {


    lateinit var filePicker: FilePicker
    val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        filePicker = FilePicker(this, packageName)
        filePicker.setFileSelectedListener(object : FilePicker.OnFileSelectedListener {
            override fun onFileSelectSuccess(filePath: String) {

                Log.d("handleImageRequest", "fileSizeAfterCompress: ${File(filePath).sizeInKb}")
                Log.d(TAG, "onFileSelectSuccess: $filePath")

            }

            override fun onFileSelectFailure() {

                Log.d(TAG, "onFileSelectSuccess: Failed")

            } })


    }

    fun onClickButton(view: View) {

        // for taking image from camera
        //  filePicker.takePhotoFromCamera()

        // for taking video from camera
        //  filePicker.takeVideoFromCamera()


        // for taking image or video form gallery by selecting boolean value
        //  filePicker.takeFromGallery(allowPickImage = true, allowPickVideo = true)

        // for selecting all type of file
        //  filePicker.pickFile()

        // for selecting only pdf and image
        filePicker.takeFromGallery(shouldCrop = true)


        // here you can define the file type as your need to let user choose..
        //  filePicker.pickFile(allowImage = false, allowPickVideo = false, allowPdf = false, allowDoc = false, allowXCL = false, allowText = false)

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        filePicker.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }



}