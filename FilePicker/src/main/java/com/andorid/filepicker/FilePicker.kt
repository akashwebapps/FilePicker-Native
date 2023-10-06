package com.andorid.filepicker

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.FileProvider
import com.andorid.filepicker.utils.Constant
import com.andorid.filepicker.utils.Constant.MIME_TYPE_DOC_DOC
import com.andorid.filepicker.utils.Constant.MIME_TYPE_DOC_DOCX
import com.andorid.filepicker.utils.Constant.MIME_TYPE_PDF
import com.andorid.filepicker.utils.Constant.MIME_TYPE_PPT_PPT
import com.andorid.filepicker.utils.Constant.MIME_TYPE_PPT_PPTX
import com.andorid.filepicker.utils.Constant.MIME_TYPE_TEXT
import com.andorid.filepicker.utils.Constant.MIME_TYPE_XL
import com.andorid.filepicker.utils.Constant.MIME_TYPE_XLS
import com.andorid.filepicker.utils.ImageCompression
import com.andorid.filepicker_native.utils.Exfn.Companion.openAppSystemSettings
import com.andorid.filepicker.utils.compressImageFile
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.ArrayList


interface ImagePickerContract {
    var filePickerHelper: FilePickerHelper
    fun takePhotoFromCamera(
        shouldCrop: Boolean = false,
        isCropBoxOval: Boolean = false,
        cropBoxRatio: Pair<Int, Int>? = null,
        filePath: (path: String) -> Unit
    )

    fun pickFile(
        allowImage: Boolean = false,
        allowPickVideo: Boolean = false,
        allowPdf: Boolean = false,
        allowDoc: Boolean = false,
        allowXCL: Boolean = false,
        allowPPT: Boolean = false,
        allowText: Boolean = false,
        filePath: (path: String) -> Unit
    )

    fun takeVideoFromCamera(filePath: (path: String) -> Unit)
    fun takeFromGallery(
        allowPickImage: Boolean = true,
        shouldCrop: Boolean = false,
        isCropBoxOval: Boolean = false,
        cropBoxRatio: Pair<Int, Int>? = null,
        allowPickVideo: Boolean = false,
        filePath: (path: String) -> Unit
    )

    fun setFileSelectedListener(listener: FilePicker.OnFileSelectedListener)
    fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    )
}

class FilePicker(private val context: AppCompatActivity, private val applicationId: String) :
    ImagePickerContract {

    var pickiT: PickiT? = null
    private var currentSelection: (() -> Unit)? = null

    private var queryFileUrl: String = ""
    private var filePath: String = ""
    private var fileUri: Uri? = null
    private lateinit var permissions: Array<String>
    private val requestCameraPermissionCode = 1000
    private var cameraPermissionErrorString: String = "Permissions not granted"

    override var filePickerHelper: FilePickerHelper = FilePickerHelper(context)
    private lateinit var onFileSelectedListener: OnFileSelectedListener
    private var cameraOrGalleryActivityLauncher: ActivityResultLauncher<Intent>

    private var shouldCrop: Boolean = false
    private var isCropBoxOval: Boolean = false
    private var cropBoxRatio: Pair<Int, Int>? = null


    private var onSelectedFile: ((filePath: String) -> Unit)? = null

    init {

        pickiT = PickiT(context, object : PickiTCallbacks {
            override fun PickiTonUriReturned() {

            }

            override fun PickiTonStartListener() {

            }

            override fun PickiTonProgressUpdate(progress: Int) {

            }

            override fun PickiTonCompleteListener(
                path: String?,
                wasDriveFile: Boolean,
                wasUnknownProvider: Boolean,
                wasSuccessful: Boolean,
                Reason: String?
            ) {
                Log.d("handlepickiT", "pickiT: ${File(path).isFile}")

                if (path != null) {
                    handleImageRequest(path)
                }
            }

            override fun PickiTonMultipleCompleteListener(
                paths: ArrayList<String>?, wasSuccessful: Boolean, Reason: String?
            ) {

            }

        }, context)

        cameraOrGalleryActivityLauncher =
            context.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {

                    if (it.data?.data == null) {
                        // taken from camera
                        pickiT?.getPath(fileUri, Build.VERSION.SDK_INT)
                    } else {
                        // choosed from file manager
                        pickiT?.getPath(it.data?.data, Build.VERSION.SDK_INT)
                    }


                }
            }
    }


    override fun setFileSelectedListener(listener: OnFileSelectedListener) {
        this.onFileSelectedListener = listener
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        when (requestCode) {
            requestCameraPermissionCode -> {
                if (filePickerHelper.isAllPermissionsGranted(grantResults)) {
                    currentSelection?.invoke()
                } else {/* Toast.makeText(
                         context,
                         cameraPermissionErrorString,
                         Toast.LENGTH_SHORT
                     ).show()*/
                    //Instantiate builder variable
                    val builder = AlertDialog.Builder(context)
                    builder.setTitle(cameraPermissionErrorString)
                    builder.setMessage("Please provide the permission by clicking on settings button")
                    builder.setPositiveButton(
                        "Settings"
                    ) { dialog, id ->
                        context.openAppSystemSettings()
                    }

                    builder.setNeutralButton("Cancel") { dialog, id ->
                        dialog.dismiss()
                    }
                    builder.show()


                }
            }
        }
    }

    private fun handleImageRequest(data: String) {
        val exceptionHandler = CoroutineExceptionHandler { _, t ->
            t.printStackTrace()
            if (::onFileSelectedListener.isInitialized) {
                onFileSelectedListener.onFileSelectFailure()
            }
            Toast.makeText(
                context,
                t.localizedMessage ?: context.getString(R.string.some_err),
                Toast.LENGTH_SHORT
            ).show()
        }

        GlobalScope.launch(Dispatchers.Main + exceptionHandler) {

            queryFileUrl = data
            filePath = queryFileUrl
            fileUri = Uri.fromFile(File(queryFileUrl))

            val mimeType = File(queryFileUrl).extension



            Log.d("handleImageRequest", "handleImageRequest: $mimeType")
            Log.d("handleImageRequest", "handleImageRequest: $filePath")



            if (mimeType.equals("JPEG", true) || mimeType.equals(
                    "JPG", true
                ) || mimeType.equals("PNG", true)
            ) {
                if (shouldCrop) {
                    cropImage.launch(options(uri = fileUri) {
                        setGuidelines(CropImageView.Guidelines.ON)
                        setOutputCompressFormat(Bitmap.CompressFormat.PNG)
                        if (isCropBoxOval) setCropShape(cropShape = CropImageView.CropShape.OVAL)
                        cropBoxRatio?.let {
                            setAspectRatio(it.first, it.second)
                        }
                    })

                    return@launch
                }

                queryFileUrl =
                    context.compressImageFile(queryFileUrl, shouldOverride = false, fileUri!!)


            }



            if (queryFileUrl.isNotEmpty()) {
                onSelectedFile?.invoke(queryFileUrl)
                /*  if (::onFileSelectedListener.isInitialized) {
                      onFileSelectedListener.onFileSelectSuccess(queryFileUrl)
                  }*/
            } else {
                /*if (::onFileSelectedListener.isInitialized) {
                    onFileSelectedListener.onFileSelectFailure()
                }*/
            }
        }
    }

    interface OnFileSelectedListener {
        fun onFileSelectSuccess(filePath: String)
        fun onFileSelectFailure()
    }


    // taking care of camera intent
    override fun takePhotoFromCamera(
        shouldCrop: Boolean,
        isCropBoxOval: Boolean,
        cropBoxRatio: Pair<Int, Int>?,
        filePath: (path: String) -> Unit
    ) {
        permissions = Constant.camera_storage_permission
        this.shouldCrop = shouldCrop
        this.isCropBoxOval = isCropBoxOval
        this.cropBoxRatio = cropBoxRatio
        this.onSelectedFile = filePath
        currentSelection =
            { takePhotoFromCamera(shouldCrop, isCropBoxOval, cropBoxRatio, filePath) }
        if (filePickerHelper.isPermissionsAllowed(
                permissions, true, requestCameraPermissionCode
            )
        ) {
            captureImageFromCamera()
        }
    }

    private fun captureImageFromCamera() {
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, setImageUri())
        cameraOrGalleryActivityLauncher.launch(takePhotoIntent)
    }

    private fun setImageUri(): Uri {
        val folder = File("${context.getExternalFilesDir(Environment.DIRECTORY_DCIM)}")
        folder.mkdirs()

        val file = File(folder, "image_tmp.jpg")
        if (file.exists()) file.delete()
        file.createNewFile()
        fileUri = FileProvider.getUriForFile(
            context, applicationId + context.getString(R.string.file_provider_name), file
        )
        filePath = file.absolutePath
        return fileUri!!
    }


    // taking care of gallery intent
    @SuppressLint("IntentReset")
    override fun takeFromGallery(
        allowPickImage: Boolean,
        shouldCrop: Boolean,
        isCropBoxOval: Boolean,
        cropBoxRatio: Pair<Int, Int>?,
        allowPickVideo: Boolean,
        filePath: (path: String) -> Unit
    ) {
        currentSelection =
            {
                takeFromGallery(
                    allowPickImage,
                    shouldCrop,
                    isCropBoxOval,
                    cropBoxRatio,
                    allowPickVideo,
                    filePath
                )
            }
        this.shouldCrop = shouldCrop
        this.isCropBoxOval = isCropBoxOval
        this.cropBoxRatio = cropBoxRatio
        this.onSelectedFile = filePath
        var intent: Intent? = null

        permissions = Constant.storage_permission
        if (filePickerHelper.isPermissionsAllowed(
                permissions, true, requestCameraPermissionCode
            )
        ) {

            val mimeType = arrayListOf<String>()
            if (allowPickImage) mimeType.add("image/*")
            if (allowPickVideo) mimeType.add("video/*")

            val type = mimeType.toTypedArray()

            intent = Intent()
            intent.type = TextUtils.join("|", type)
            if (mimeType.size > 1) intent.putExtra(Intent.EXTRA_MIME_TYPES, type)
            intent.action = Intent.ACTION_GET_CONTENT

            intent.let {
                it.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                cameraOrGalleryActivityLauncher.launch(it)
            }

//            if (allowPickImage && allowPickVideo) { intent = Intent(Intent.ACTION_GET_CONTENT)
//                intent.addCategory(Intent.CATEGORY_OPENABLE)
//                intent.type = "image/* video/*"
//                intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
//            }
//            else if (allowPickImage && !allowPickVideo) {
//                intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//            } else if (!allowPickImage && allowPickVideo) {
//                intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
//            }

        }

    }


    // taking care of gallery intent for video
    override fun takeVideoFromCamera(filePath: (path: String) -> Unit) {
        permissions = Constant.camera_storage_permission
        currentSelection = { takeVideoFromCamera(filePath) }
        this.onSelectedFile = filePath
        if (filePickerHelper.isPermissionsAllowed(
                permissions, true, requestCameraPermissionCode
            )
        ) {
            captureVideoFromCamera()
        }
    }

    private fun captureVideoFromCamera() {
        val takePhotoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        cameraOrGalleryActivityLauncher.launch(takePhotoIntent)
    }


    // taking care of document
    override fun pickFile(
        allowImage: Boolean,
        allowPickVideo: Boolean,
        allowPdf: Boolean,
        allowDoc: Boolean,
        allowXCL: Boolean,
        allowPPT: Boolean,
        allowText: Boolean,
        filePath: (path: String) -> Unit
    ) {
        permissions = Constant.storage_permission
        currentSelection = {
            pickFile(
                allowImage,
                allowPickVideo,
                allowPdf,
                allowDoc,
                allowXCL,
                allowPPT,
                allowText,
                filePath
            )
        }
        this.onSelectedFile = filePath
        if (filePickerHelper.isPermissionsAllowed(
                permissions, true, requestCameraPermissionCode
            )
        ) {
            pickDocument(
                allowImage, allowPickVideo, allowPdf, allowDoc, allowXCL,allowPPT, allowText
            )
        }
    }

    private fun pickDocument(
        allowImage: Boolean,
        allowPickVideo: Boolean,
        allowPdf: Boolean,
        allowDoc: Boolean,
        allowXCL: Boolean,
        allowPPT: Boolean,
        allowText: Boolean
    ) {

        val mimeType = arrayListOf<String>()

        if (!allowImage && !allowPickVideo && !allowPdf && !allowDoc && !allowXCL && !allowText) {
            mimeType.add("*/*")
        }
        if (allowImage) mimeType.add("image/*")
        if (allowPickVideo) mimeType.add("video/*")
        if (allowPdf) mimeType.add(MIME_TYPE_PDF)
        if (allowDoc) mimeType.add(MIME_TYPE_DOC_DOCX)
        if (allowDoc) mimeType.add(MIME_TYPE_DOC_DOC)
        if (allowXCL) mimeType.add(MIME_TYPE_XLS)
        if (allowXCL) mimeType.add(MIME_TYPE_XL)
        if (allowText) mimeType.add(MIME_TYPE_TEXT)
        if (allowPPT) mimeType.add(MIME_TYPE_PPT_PPT)
        if (allowPPT) mimeType.add(MIME_TYPE_PPT_PPTX)

        // by passing arrayList will not work as mime type for that we convert the dynamic arrayList to String array and passed it..
        val type = mimeType.toTypedArray()

        val intent = Intent()
        intent.type = TextUtils.join("|", type)
        if (mimeType.size > 1) intent.putExtra(Intent.EXTRA_MIME_TYPES, type)
        intent.action = Intent.ACTION_GET_CONTENT

        cameraOrGalleryActivityLauncher.launch(Intent.createChooser(intent, "Choose File"))
    }


    // FIXME: 22-09-2022

    private val cropImage = context.registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // use the returned uri

            fileUri = result.uriContent
            queryFileUrl = result.getUriFilePath(context).toString()


            val exceptionHandler = CoroutineExceptionHandler { _, t ->
                t.printStackTrace()
                if (::onFileSelectedListener.isInitialized) {
                    onFileSelectedListener.onFileSelectFailure()
                }
                Toast.makeText(
                    context,
                    t.localizedMessage ?: context.getString(R.string.some_err),
                    Toast.LENGTH_SHORT
                ).show()
            }

            GlobalScope.launch(Dispatchers.Main + exceptionHandler) {

                queryFileUrl =
                    context.compressImageFile(queryFileUrl, shouldOverride = false, fileUri!!)

                if (queryFileUrl.isNotEmpty()) {
                    onSelectedFile?.invoke(queryFileUrl)
                    /*if (::onFileSelectedListener.isInitialized) {
                        onFileSelectedListener.onFileSelectSuccess(queryFileUrl)
                    }*/
                } else {
                    /*if (::onFileSelectedListener.isInitialized) {
                        onFileSelectedListener.onFileSelectFailure()
                    }*/
                }
            }


        } else {
            val exception = result.error
        }


    }

}
