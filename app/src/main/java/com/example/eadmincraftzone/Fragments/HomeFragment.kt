package com.example.eadmincraftzone.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.eadmincraftzone.Activity.AllOrders
import com.example.eadmincraftzone.R
import com.example.eadmincraftzone.databinding.FragmentHomeBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

class HomeFragment : Fragment() {

private lateinit var binding :FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(layoutInflater)
        binding.addcategory.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_categoryFragment)

        }
        binding.addproduct.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_productFragment)
        }
        binding.addslider.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_sliderFragment)
        }
        binding.addorderdetail.setOnClickListener {
            startActivity(Intent(requireContext(), AllOrders::class.java))
        }
        return binding.root
    }
}