package com.tans.image2characters

import android.Manifest
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.tans.image2characters.converters.ColorCharactersConverter
import com.tans.rxutils.SaveMediaType
import com.tans.rxutils.chooseImageFromGallery
import com.tans.rxutils.loadingDialog
import com.tans.rxutils.saveDataToMediaStore
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.withContext
import java.io.PipedInputStream
import java.io.PipedOutputStream

val maxBitmapSize = 500f

class MainActivity : AppCompatActivity(), CoroutineScope by CoroutineScope(Dispatchers.Main) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tool_bar.menu.findItem(R.id.add_image)
            .setOnMenuItemClickListener  {
                launch {
                    val bitmap = chooseImageFromGallery(this@MainActivity)
                        .observeOn(Schedulers.io())
                        .map {
                            val orientation = this@MainActivity.getImageOrientation(it)
                            val sampleCharactersConverter = ColorCharactersConverter()
                            val image = getBitmapWithMaxSize(
                                maxWidth = maxBitmapSize,
                                maxHeight = maxBitmapSize,
                                uri = it
                            ).rotation(orientation)
                            sampleCharactersConverter.convert(image)
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .loadingDialog(this@MainActivity)
                        .await()
                    if (bitmap != null) {
                        image_view.setImageBitmap(bitmap)
                    }
                }
                true
            }

        tool_bar.menu.findItem(R.id.save_image)
            .setOnMenuItemClickListener {
                launch {
                    val grant = RxPermissions(this@MainActivity)
                        .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .firstOrError()
                        .await()
                    if (grant) {
                        val bitmap = (image_view.drawable as? BitmapDrawable)?.bitmap
                        if (bitmap != null) {
                            val inputStream = PipedInputStream()
                            val outputStream =
                                withContext(Dispatchers.IO) { PipedOutputStream(inputStream) }

                            launch(Dispatchers.IO) {
                                bitmap.compress(
                                    Bitmap.CompressFormat.JPEG,
                                    50,
                                    outputStream
                                )
                                outputStream.flush()
                                outputStream.close()
                            }
                            launch(Dispatchers.IO) {
                                saveDataToMediaStore(
                                    context = this@MainActivity,
                                    inputStream = inputStream,
                                    mimeType = "image/jpeg",
                                    name = "${System.currentTimeMillis()}.jpeg",
                                    saveMediaType = SaveMediaType.Images,
                                    relativePath = ""
                                )
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .loadingDialog(this@MainActivity)
                                    .subscribeOn(AndroidSchedulers.mainThread())
                                    .await()
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Saved!!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Image is Empty!!!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                true
            }

    }
}