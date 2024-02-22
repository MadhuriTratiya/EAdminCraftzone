package com.example.eadmincraftzone.Fragments

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import com.example.eadmincraftzone.R
import com.example.eadmincraftzone.databinding.FragmentSliderBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID


class SliderFragment : Fragment() {

    private lateinit var binding: FragmentSliderBinding

    private var imageUrl: Uri? = null
   private var imgUrl: Uri? = null
    private lateinit var dialog: Dialog


    private var launchGallaryActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            imageUrl = it.data!!.data
            binding.imageView.setImageURI(imageUrl)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSliderBinding.inflate(layoutInflater)

        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.progres_layout)
        dialog.setCancelable(false)

        binding.apply {
            binding.imageView.setOnClickListener {
                val intent = Intent("android.intent.action.GET_CONTENT")
                intent.type = "image/*"
                launchGallaryActivity.launch(intent)
            }
            uploadImage.setOnClickListener {
                if (imageUrl != null) {
                    uploadImage(imageUrl!!)
                } else {
                    Toast.makeText(requireContext(), "please select image", Toast.LENGTH_SHORT).show()
                }
            }
        }
        //Image Upload code
        return binding.root
    }
    private fun uploadImage(Uri: Uri) {
        dialog.show()
        //create file
        val filename = UUID.randomUUID().toString() + "jpg"
        //firebase storage reference
        val refStorage = FirebaseStorage.getInstance().reference.child("slider/$filename")
        //store file in storage
        refStorage.putFile(Uri).addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { image ->
                    storeData(it.toString())
                }
            }.addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(
                    requireContext(), "Something Went Wrong with storage", Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun storeData(image: String) {
        //To store in firebase
        val db = Firebase.firestore
        //create hashmap to store data
        val data = hashMapOf<String, Any>(
            "img" to image
        )
        db.collection("slider").document("item").set(data).addOnSuccessListener {
                dialog.dismiss()
                Toast.makeText(requireContext(), "Slider Updated", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(requireContext(), "Something Went Wrong", Toast.LENGTH_SHORT).show()

            }
    }


}
