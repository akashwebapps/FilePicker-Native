package com.andorid.filepicker.utils

import android.Manifest
import android.os.Build

object Constant {

    val TAG = "AkashUtilsClass"


    val MIME_TYPE_IMAGE = "image/jpeg"
    val MIME_TYPE_PDF = "application/pdf"
    val MIME_TYPE_VIDEO_MP4 = "video/mp4"
    val MIME_TYPE_DOC_DOCX = "application/msword"
    val MIME_TYPE_PPT_PPTX = "application/vnd.ms-powerpoint"
    val MIME_TYPE_XLS = "application/vnd.ms-excel"
    val MIME_TYPE_TEXT = "text/*"
    val MIME_TYPE_ZIP_RAR = "application/x-wav"
    val MIME_TYPE_WAV_AUDIO = "audio/x-wav"


    val camera_storage_permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.CAMERA,
        )
    } else arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    val storage_permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
        )
    } else arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )


}