package com.andorid.filepicker_native.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.webkit.MimeTypeMap
import java.io.File
import java.net.URLEncoder
import java.util.*

class Exfn {

    companion object{
        fun Context.openAppSystemSettings() {
            startActivity(Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", packageName, null)
            })
        }

        fun File.getMimeType(fallback: String = "image/*"): String {
            return MimeTypeMap.getFileExtensionFromUrl(toString())
                ?.run { MimeTypeMap.getSingleton().getMimeTypeFromExtension(lowercase(Locale.getDefault())) }
                ?: fallback // You might set it to */*
        }


        val File.size get() = if (!exists()) 0.0 else length().toDouble()
        val File.sizeInKb get() = size / 1024
        val File.sizeInMb get() = sizeInKb / 1024
        val File.sizeInGb get() = sizeInMb / 1024
        val File.sizeInTb get() = sizeInGb / 1024
    }

    fun File.getExtension(): String {
        val encoded: String = try {
            URLEncoder.encode(name, "UTF-8").replace("+", "%20")
        } catch (e: Exception) {
            name
        }

        return MimeTypeMap.getFileExtensionFromUrl(encoded).toLowerCase(Locale.getDefault())
    }
}