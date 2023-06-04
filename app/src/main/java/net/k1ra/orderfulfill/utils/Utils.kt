package net.k1ra.orderfulfill.utils

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.gap.hoodies_network.core.HoodiesNetworkClient
import com.gap.hoodies_network.core.Success
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.k1ra.orderfulfill.R

/**
 * Binding to easily load an image from a URL
 * Starts with showing loading shimmer and then lazy loads the image
 */
@BindingAdapter("bind:imageFromUrl")
fun loadImageFromUrl(iv: ImageView, url: String) {
    CoroutineScope(Dispatchers.IO).launch {
        //Set loading shimmer using workaround for gifs not animating in drawable
        val source = ImageDecoder.createSource(iv.context.resources, R.drawable.shimmer_load)
        val drawable = ImageDecoder.decodeDrawable(source) //Why is it complaining, this is an IO coroutine
        Handler(Looper.getMainLooper()).post {
            iv.setImageDrawable(drawable)
            if (drawable is AnimatedImageDrawable)
                drawable.start()
        }

        //Load image from URL and then display when done
        val imageResult = HoodiesNetworkClient.Builder().build().getImage(
            url,
            null,
            0,
            0,
            ImageView.ScaleType.CENTER_CROP,
            Bitmap.Config.ALPHA_8
        )
        if (imageResult is Success)
            Handler(Looper.getMainLooper()).post {
                iv.setImageBitmap(imageResult.value)
            }
    }
}

class Utils {
    companion object {
        fun urlQueryParamStringToHashMap(str: String): HashMap<String, String> {

            //Drop leading ? if present
            var str = str
            if (str.startsWith("?"))
                str = str.drop(1)

            //Split components
            val output = mutableMapOf<String, String>()
            val components = str.split("&")

            //Transfer key=val components into HashMap
            for (item in components) {
                val keyAndVal = item.split("=")
                output[keyAndVal[0]] = keyAndVal[1]
            }

            return output as HashMap<String, String>
        }
    }
}