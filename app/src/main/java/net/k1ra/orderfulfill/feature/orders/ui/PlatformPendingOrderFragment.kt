package net.k1ra.orderfulfill.feature.orders.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.k1ra.orderfulfill.R
import net.k1ra.orderfulfill.activities.OauthActivity
import net.k1ra.orderfulfill.feature.orders.adapters.PendingOrderListener
import net.k1ra.orderfulfill.feature.orders.adapters.PendingOrderRecyclerViewAdapter
import net.k1ra.orderfulfill.databinding.PlatformPendingOrderFragmentBinding
import net.k1ra.orderfulfill.feature.orders.model.PendingOrders
import net.k1ra.orderfulfill.model.*
import net.k1ra.orderfulfill.utils.Constants
import net.k1ra.orderfulfill.feature.orders.viewmodel.PlatformPendingOrderViewModel
import net.k1ra.orderfulfill.fragments.BaseDynamicFragment
import java.util.function.Consumer


class PlatformPendingOrderFragment : BaseDynamicFragment() {
    lateinit var viewModel: PlatformPendingOrderViewModel
    private lateinit var binding: PlatformPendingOrderFragmentBinding

    //Register for OAuth activity result
    private val oAuthActivityForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            CoroutineScope(Dispatchers.IO).launch {
                oAuthActivityResultDataWorker?.callback?.accept(DataWorkerResult(SuccessFail.SUCCESS, result.data!!.extras!!.getString(Constants.intentExtraOAuthResponse)!!))
                viewModel.init(requireContext(), platformActions)
            }
        }
    }
    private var oAuthActivityResultDataWorker: DataWorker<Unit, String>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[PlatformPendingOrderViewModel::class.java]
        binding = PlatformPendingOrderFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /**
         * Set apiKeyMissingOrInvalid callback
         * It will prompt the user for the API key via a Dialog if the platform uses an API key, or perform OAuth
         * Then, it will send the API key string to the DataWorker<Unit, String>, so the ViewModel can save the key
         * Finally, we re-init the ViewModel
         */
        viewModel.apiAuthDataMissingOrInvalidCallback = Consumer {

            if (platformActions.authorizationType == PlatformAuthorizationType.API_KEY) {
                //Prompt user for the API key via an AlertDialog for the platform
                platformActions.apiKeyEntryDialog(this, it)?.show()
            } else {
                //Perform OAuth using OAuthActivity and StartActivityForResult
                oAuthActivityResultDataWorker = it
                val oAuthActivityLaunchIntent = Intent(requireActivity(), OauthActivity::class.java)
                oAuthActivityLaunchIntent.putExtra(Constants.intentExtraPlatform, platformActions.type.ordinal)
                oAuthActivityForResult.launch(oAuthActivityLaunchIntent)
            }
        }

        /**
         * Init ViewModel
         * The ViewModel will check for/get the API key and fetch pending orders
         */
        viewModel.init(requireContext(), platformActions)


        //Handle page's state and show pending orders when loading is complete
        observeViewModelDynamicState(viewModel) {
            showPendingOrderView()
        }
    }

    /**
     * Shows the main view this Fragment is supposed to display, a list of pending orders
     */
    private fun showPendingOrderView() {

        binding.platformPendingOrderRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        viewModel.orders.observeForever {
            binding.platformPendingOrderRecyclerView.adapter = PendingOrderRecyclerViewAdapter(it, object: PendingOrderListener {
                override fun onOrderClicked(order: PendingOrders.PendingOrder) {
                    PendingOrderViewOverlay.getInstance().showOverlay(childFragmentManager, order, viewModel, platformActions)
                }
            })

            binding.platformPendingOrdersTitle.text = requireContext().getString(
                R.string.platform_unfulfilled_orders,
                platformActions.type.toString(),
                it.size.toString()
            )
        }
    }
}