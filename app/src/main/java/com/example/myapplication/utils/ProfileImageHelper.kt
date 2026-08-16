package com.example.myapplication.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import com.example.myapplication.R
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ProfileImageHelper {

    private const val TAG = "ProfileImageHelper"
    private const val PROFILE_IMAGE_FILE_NAME = "user_profile_image.png"

    fun getProfileImageFile(context: Context): File {
        return File(context.filesDir, PROFILE_IMAGE_FILE_NAME)
    }

    fun saveProfileImage(context: Context, uri: Uri): Boolean {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap != null) {
                val destFile = getProfileImageFile(context)
                val outStream = FileOutputStream(destFile)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
                outStream.flush()
                outStream.close()
                Log.d(TAG, "Profile image saved successfully to ${destFile.absolutePath}")
                true
            } else {
                Log.e(TAG, "Failed to decode bitmap from Uri: $uri")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving profile image", e)
            false
        }
    }

    fun loadProfileImage(context: Context, imageView: ImageView) {
        val file = getProfileImageFile(context)
        if (file.exists() && file.length() > 0) {
            try {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                    return
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading saved profile image", e)
            }
        }
        imageView.setImageResource(R.drawable.circle_bg)
    }
}
