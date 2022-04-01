package com.example.android.lesson5.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.ContactsContract
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.lesson5.Contact
import com.example.android.lesson5.R
import com.example.android.lesson5.RecyclerViewAdapter


class ContactsFragment : Fragment(){
    private lateinit var recyclerViewAdapter: RecyclerViewAdapter
    private var contactsList = mutableListOf<Contact>()

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission())
        { isGranted: Boolean ->
            if (isGranted) {
                loadContacts()
            } else {
                showInfoDialog()
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view: View = inflater.inflate(R.layout.fragment_contacts, container, false)

        val linearLayoutManager = LinearLayoutManager(requireContext())

        val recyclerView = view.findViewById(R.id.recycler_view) as RecyclerView
        recyclerViewAdapter = RecyclerViewAdapter(contactsList)
        recyclerView.adapter = recyclerViewAdapter
        recyclerView.layoutManager = linearLayoutManager

        val button: Button = view.findViewById<View>(R.id.load_contacts_btn) as Button
        button.setOnClickListener {
            requestPermissions()
        }

        return view
    }

    private fun requestPermissions() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED -> {
                loadContacts()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) -> {
                showAlertDialog()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }

    private fun showAlertDialog() {
        val alertDialog = AlertDialog.Builder(requireContext())

        alertDialog.setTitle("Доступ к контактам")

        alertDialog.setMessage("Ну очень нужно (О)_(О)")

        alertDialog.setPositiveButton("Сжалиться") { dialog, which ->
            dialog.cancel()
            requestPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
        alertDialog.setNegativeButton("Не очень") { dialog, which ->
            dialog.cancel()
        }

        alertDialog.create().show()
    }

    private fun showInfoDialog() {
        val infoDialog = AlertDialog.Builder(requireContext())

        infoDialog.setTitle("No magic :(")

        infoDialog.setMessage("Please allow permission to access your contacts")
        infoDialog.setPositiveButton("OK") { dialog, which ->
            dialog.cancel()
        }

        infoDialog.show()
    }


    @SuppressLint("Range")
    private fun loadContacts() {
        var contactId = ""
        var displayName = ""

        val cursor: Cursor? = activity?.contentResolver?.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val hasPhoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)).toInt()
                    if (hasPhoneNumber > 0) {
                        contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                        displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))

                        val phoneCursor: Cursor? = activity?.contentResolver?.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(contactId),
                            null
                        )

                        if(phoneCursor != null) {
                            if (phoneCursor.moveToFirst()) {
                                val phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(
                                            ContactsContract.CommonDataKinds.Phone.NUMBER
                                        )
                                    )

                                contactsList.add(Contact(displayName, phoneNumber))
                            }
                            phoneCursor.close()
                        }
                    }

                } while (cursor.moveToNext())
            }

            cursor.close()
        }

        recyclerViewAdapter.notifyDataSetChanged()
    }
}