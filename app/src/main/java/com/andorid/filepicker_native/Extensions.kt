package com.andorid.filepicker_native

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Checks whether the any camera activity is available or not to handle the intent.
 * If there is camera activity open the camera
 */
internal fun Context.dispatchTakePictureIntent(onGetImageFromCameraActivityResult: ActivityResultLauncher<Intent>): Uri? {
    Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
        packageManager?.run {
            takePictureIntent.resolveActivity(this)?.also {
                try {
                    val timeStamp: String = SimpleDateFormat(
                        "yyyyMMdd_HHmmss",
                        Locale.getDefault()
                    ).format(Date())
                    createImageFile(timeStamp).apply {
                        var photoURI: Uri?
                        also { photo ->
                            photoURI = FileProvider.getUriForFile(
                                this@dispatchTakePictureIntent,
                                "${applicationContext.packageName}.provider",
                                photo
                            )
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                            onGetImageFromCameraActivityResult.launch(takePictureIntent)
                        }
                        return photoURI
                    }
                } catch (ex: IOException) {
                    ex.printStackTrace()
                    return null
                }
            }
        }
    }
    return null
}

/**
 * Create image file in the picture's directory of external files directory.
 */
internal fun Context.createImageFile(name: String = ""): File {
    val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile("JPEG_${name}_", ".jpg", storageDir)
}

/**
 * Extension function to register activity result intent
 */
internal fun ComponentActivity.registerActivityResult(
    name: String,
    errorCallback: (ActivityResult) -> Unit = {},
    successCallBack: (ActivityResult) -> Unit
): ActivityResultLauncher<Intent> {
    return activityResultRegistry.register(name, ActivityResultContracts.StartActivityForResult()) {
        it?.let { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                successCallBack(activityResult)
            } else {
                errorCallback(activityResult)
            }
        }
    }
}