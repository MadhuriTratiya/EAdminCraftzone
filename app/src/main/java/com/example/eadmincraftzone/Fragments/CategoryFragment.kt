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
import androidx.activity.result.contract.ActivityResultContracts
import com.example.eadmincraftzone.Adapter.categoryAdapter
import com.example.eadmincraftzone.Model.categoryModel
import com.example.eadmincraftzone.R
import com.example.eadmincraftzone.databinding.FragmentCategoryBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID


class CategoryFragment : Fragment() {

    private lateinit var binding: FragmentCategoryBinding
    private var imageUrl: Uri? = null
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCategoryBinding.inflate(layoutInflater)

        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.progres_layout)
        dialog.setCancelable(false)

        getData()

        binding.apply {
            binding.imageView.setOnClickListener {

                val intent = Intent("android.intent.action.GET_CONTENT")
                intent.type = "image/*"
                launchGallaryActivity.launch(intent)

            }
            categoryUploadbtn.setOnClickListener {
                validateData(binding.categoryName.text.toString())
            }
        }

        return binding.root
    }

    //Get data from database
    private fun getData() {
        val list = ArrayList<categoryModel>()
        Firebase.firestore.collection("category")
            .get().addOnSuccessListener {
                list.clear()
                for (doc in it.documents) {
                    val data = doc.toObject(categoryModel::class.java)
                    list.add(data!!)
                }
                binding.categoryRecyclerview.adapter = categoryAdapter(requireContext(), list)
            }
    }

    private fun validateData(categoryName: String) {
        if (categoryName.isEmpty()) {
            Toast.makeText(requireContext(), "Please Provide Category Name", Toast.LENGTH_SHORT)
                .show()
        } else if (imageUrl == null) {
            Toast.makeText(requireContext(), "Please Select Image", Toast.LENGTH_SHORT).show()
        } else {
            uploadImage(categoryName)
        }
    }

    private fun uploadImage(categoryName: String) {
        dialog.show()
        //create file
        val filename = UUID.randomUUID().toString() + "jpg"
        //firebase storage reference
        val refStorage = FirebaseStorage.getInstance().reference.child("category/$filename")
        //store file in storage
        refStorage.putFile(imageUrl!!)
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { image ->
                    storeData(categoryName, image.toString())
                }
            }.addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(
                    requireContext(), "Something Went Wrong with storage", Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun storeData(categoryName: String, url: String) {
        //To store in firebase
        val db = Firebase.firestore

        //create hashmap to store data
        val data = hashMapOf<String, Any>(
            "cat" to categoryName,
            "img" to url
        )
        db.collection("category").add(data)
            .addOnSuccessListener {
                dialog.dismiss()
                binding.imageView.setImageDrawable(resources.getDrawable(R.drawable.imageview))
                 binding.categoryName.text = null
                getData()
                Toast.makeText(requireContext(), "Category Updated", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(requireContext(), "Something Went Wrong", Toast.LENGTH_SHORT).show()

            }
    }

}
