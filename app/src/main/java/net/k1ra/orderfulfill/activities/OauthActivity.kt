package net.k1ra.orderfulfill.activities

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.*
import net.k1ra.orderfulfill.R
import net.k1ra.orderfulfill.databinding.ActivityOauthBinding
import net.k1ra.orderfulfill.model.DataWorker
import net.k1ra.orderfulfill.model.SuccessFail
import net.k1ra.orderfulfill.model.Platform
import net.k1ra.orderfulfill.platforms.ecommerce.EcomPlatformActions
import net.k1ra.orderfulfill.platforms.PlatformProvider
import net.k1ra.orderfulfill.utils.Constants
import net.k1ra.orderfulfill.utils.Utils

class OauthActivity : AppCompatActivity() {
    lateinit var binding: ActivityOauthBinding
    var queryParamHashMap: HashMap<String, String>? = null
    lateinit var platform: EcomPlatformActions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOauthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.oauthAccessTokenProgressBar.visibility = View.GONE

        //Get platform from provider based on intent extra
        platform = PlatformProvider.forEcomType(Platform.values()[intent.getIntExtra(Constants.intentExtraPlatform, -1)])

        //Url must be passed by whoever requested OAuth
        binding.oauthWebview.loadUrl(platform.oAuthUrl)

        //Make websites happy
        binding.oauthWebview.settings.javaScriptEnabled = true
        binding.oauthWebview.settings.domStorageEnabled = true


        binding.oauthWebview.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                //Show progressbar if page is still loading and make it gone once it is fully loaded
                binding.oauthWebviewProgressbar.visibility = View.VISIBLE
                binding.oauthWebviewProgressbar.progress = progress
                if (progress == 100)
                    binding.oauthWebviewProgressbar.visibility = View.GONE

                //If we are being redirected to our OAuth redirect URL, abort loading, capture it, and parse the result based on the platform
                if (binding.oauthWebview.url?.startsWith(platform.oAuthReturnUrl) == true) {
                    val paramString = binding.oauthWebview.url!!.replace(platform.oAuthReturnUrl,"")
                    binding.oauthWebview.stopLoading()

                    //In case this is called twice, make sure the query param HashMap
                    if (queryParamHashMap == null) {
                        queryParamHashMap = Utils.urlQueryParamStringToHashMap(paramString)

                        binding.oauthAccessTokenProgressBar.visibility = View.VISIBLE
                        platform.oAuthAccessTokenAcquisitionRunnable.accept(DataWorker(queryParamHashMap!!){
                            Handler(Looper.getMainLooper()).post {
                                if (it.result == SuccessFail.SUCCESS) {
                                    val returnIntent = Intent().apply {
                                        putExtra(Constants.intentExtraOAuthResponse, it.data)
                                    }
                                    setResult(RESULT_OK, returnIntent)
                                    finish()
                                } else {
                                    val builder = AlertDialog.Builder(this@OauthActivity)
                                    builder.setTitle(R.string.error)
                                    builder.setMessage(getString(it.data.toInt()))
                                    builder.setPositiveButton(R.string.ok) { dialog, _ ->
                                        dialog.cancel()

                                        finish()
                                    }
                                    builder.show()
                                }
                            }
                        })
                    }
                }
            }
        }

        //Keep everything in the WebView
        binding.oauthWebview.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                view?.loadUrl(request?.url.toString())
                return false
            }
        }
    }
}