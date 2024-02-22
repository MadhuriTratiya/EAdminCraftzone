package com.example.eadmincraftzone.Fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.eadmincraftzone.Adapter.AddProductImageAddapter
import com.example.eadmincraftzone.Model.AddProductModel
import com.example.eadmincraftzone.Model.categoryModel
import com.example.eadmincraftzone.R
import com.example.eadmincraftzone.databinding.FragmentAddProductBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class AddProductFragment : Fragment() {

    private lateinit var binding: FragmentAddProductBinding
    private lateinit var list: ArrayList<Uri>
    private lateinit var listimages: ArrayList<String>
    private lateinit var adapter: AddProductImageAddapter
    private var coverImage: Uri? = null
    private lateinit var dialog: Dialog
    private var coverImageUrl: String? = ""
    private lateinit var categoryList: ArrayList<String>


    private var launchGallaryActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            coverImage = it.data!!.data
            binding.productimageview.setImageURI(coverImage)
            binding.productimageview.visibility = VISIBLE
        }
    }

    private var launchProductActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            val imgeUrl = it.data!!.data
            list.add(imgeUrl!!)
            adapter.notifyDataSetChanged()
            coverImage = it.data!!.data
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAddProductBinding.inflate(layoutInflater)

        list = ArrayList()
        listimages = ArrayList()

        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.progres_layout)
        dialog.setCancelable(false)

        binding.selectcoverimage.setOnClickListener {
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type = "image/*"
            launchGallaryActivity.launch(intent)

        }

        binding.productimagebtn.setOnClickListener {
            val intent = Intent("android.intent.action.GET_CONTENT")
            intent.type = "image/*"
            launchProductActivity.launch(intent)

        }
        setProductCategory()

        adapter = AddProductImageAddapter(list)
        binding.productimgrecyclerview.adapter = adapter


        binding.submitproductbtn.setOnClickListener {
            validateData()
        }

        return binding.root
    }

    private fun validateData() {
        if (binding.productnmedit.text.toString().isEmpty()) {
            binding.productnmedit.requestFocus()
            binding.productnmedit.error = "Empty"
        } else if (binding.productspedit.text.toString().isEmpty()) {
            binding.productspedit.requestFocus()
            binding.productspedit.error = "Empty"
        } else if (coverImage == null) {
            Toast.makeText(requireContext(), "Please Select Cover Image", Toast.LENGTH_SHORT).show()
        } else if (list.size < 1) {
            Toast.makeText(requireContext(), "Please Select Product Images", Toast.LENGTH_SHORT).show()
        } else {
            uploadImage()
        }
    }

    private fun uploadImage() {
        dialog.show()
        //create file
        val filename = UUID.randomUUID().toString() + "jpg"
        //firebase storage reference
        val refStorage = FirebaseStorage.getInstance().reference.child("products/$filename")
        //store file in storage
        refStorage.putFile(coverImage!!)
             .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { image ->
                    coverImageUrl = image.toString()

                    uploadProductImage()
                }
            }.addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(
                    requireContext(), "Something Went Wrong with storage", Toast.LENGTH_SHORT
                ).show()
            }
    }

    private var i = 0
    private fun uploadProductImage() {
        dialog.show()
        //create file
        val filename = UUID.randomUUID().toString() + "jpg"
        //firebase storage reference
        val refStorage = FirebaseStorage.getInstance().reference.child("products/$filename")
        //store file in storage
        refStorage.putFile(list[i]!!)
             .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { image ->
                    listimages.add(image!!.toString())
                    if (list.size == listimages.size) {

                        storeData()
                    } else {
                        i += 1
                        uploadProductImage()
                    }

                }

            }.addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(
                    requireContext(), "Something Went Wrong with storage", Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun storeData() {
        val db = Firebase.firestore.collection("products")
        val key = db.document().id

        val data = AddProductModel(
            binding.productnmedit.text.toString(),
            binding.productdescriptionedit.text.toString(),
            coverImageUrl.toString(),
            categoryList[binding.productcategorydropdown.selectedItemPosition],
            key,
            binding.productmrpedit.text.toString(),
            binding.productspedit.text.toString(),
            listimages
        )
        db.document(key).set(data).addOnSuccessListener {
            dialog.dismiss()
            Toast.makeText(requireContext(), "Product Added", Toast.LENGTH_SHORT).show()
            binding.productnmedit.text = null
        }.addOnFailureListener {
                dialog.dismiss()
                Toast.makeText(requireContext(), "Something Went Wrong", Toast.LENGTH_SHORT).show()

            }
    }

    private fun setProductCategory() {
        categoryList = ArrayList()
        Firebase.firestore.collection("category").get().addOnSuccessListener {
            categoryList.clear()
            for (doc in it.documents) {
                val data = doc.toObject(categoryModel::class.java)
                categoryList.add(data!!.cat!!)
            }
            categoryList.add(0, "Select Category")
            val arrayAdapter =
                ArrayAdapter(requireContext(), R.layout.dropdownitemlayout, categoryList)
            binding.productcategorydropdown.adapter = arrayAdapter

        }
    }
}