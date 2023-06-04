package net.k1ra.orderfulfill.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import net.k1ra.orderfulfill.R
import net.k1ra.orderfulfill.adapters.EcomPlatformListener
import net.k1ra.orderfulfill.adapters.EcomPlatformRecyclerViewAdapter
import net.k1ra.orderfulfill.databinding.FragmentMainMenuBinding
import net.k1ra.orderfulfill.platforms.ecommerce.EcomPlatformActions
import net.k1ra.orderfulfill.platforms.PlatformProvider
import net.k1ra.orderfulfill.utils.Constants

class MainMenuFragment : Fragment() {
    private lateinit var binding: FragmentMainMenuBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ecomPlatformsRv.layoutManager = LinearLayoutManager(requireContext())
        binding.ecomPlatformsRv.adapter = EcomPlatformRecyclerViewAdapter(PlatformProvider.getEcomTypes(), object: EcomPlatformListener{
            override fun onClicked(platform: EcomPlatformActions) {
                val bundle = bundleOf(Constants.intentExtraPlatform to platform.type.ordinal)
                findNavController().navigate(R.id.action_mainMenuFragment_to_pendingOrderFragment, bundle)
            }

        })
    }
}