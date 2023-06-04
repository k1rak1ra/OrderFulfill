package net.k1ra.orderfulfill.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import net.k1ra.orderfulfill.model.LoadingState
import net.k1ra.orderfulfill.model.Platform
import net.k1ra.orderfulfill.overlays.ErrorOverlay
import net.k1ra.orderfulfill.overlays.LoadingOverlay
import net.k1ra.orderfulfill.platforms.ecommerce.EcomPlatformActions
import net.k1ra.orderfulfill.platforms.PlatformProvider
import net.k1ra.orderfulfill.utils.Constants
import net.k1ra.orderfulfill.viewmodel.BaseDynamicViewModel


open class BaseDynamicFragment : Fragment() {
    lateinit var platformActions: EcomPlatformActions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        platformActions = PlatformProvider.forEcomType(Platform.values()[requireArguments().getInt(Constants.intentExtraPlatform)])
    }

    /**
     * Handle this page's loading, error, and complete states
     * We show an error overlay if there is an error, loading overlay if data is loading, etc
     */
    fun observeViewModelDynamicState(viewModel: BaseDynamicViewModel, loadingCompleteAction: Runnable) {
        viewModel.mainLoadingState.observeForever{
            when(it) {
                LoadingState.LOADING -> {
                    ErrorOverlay.getInstance().hide()
                    LoadingOverlay.getInstance().showOverlay(childFragmentManager, viewModel.loadingText.value)
                }
                LoadingState.ERROR -> {
                    LoadingOverlay.getInstance().hide()
                    ErrorOverlay.getInstance().showOverlay(childFragmentManager, viewModel.errorText.value, viewModel.errorActionCallback.value)
                }
                LoadingState.COMPLETE -> {
                    ErrorOverlay.getInstance().hide()
                    LoadingOverlay.getInstance().hide()

                    loadingCompleteAction.run()
                }
            }
        }
    }
}