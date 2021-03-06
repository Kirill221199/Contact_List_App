package ru.kirill.contact_list_app

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ru.kirill.contact_list_app.databinding.FragmentMainBinding


class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding: FragmentMainBinding
        get() {
            return _binding!!
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkContacts()

    }

    @SuppressLint("Range")
    fun getContacts() {
        val contentResolver: ContentResolver = requireContext().contentResolver
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            null//ContactsContract.Contacts.DISPLAY_NAME + "ASC"
        ) // или DESC
        cursor?.let {
            for (i in 0 until it.count) {
                if (cursor.moveToPosition(i)) {
                    val columnNameIndexName = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    val name: String = cursor.getString(columnNameIndexName)
                    val contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                    val number = getNumberFromID(contentResolver,contactId)

                    binding.fragmentContainer.addView(TextView(requireContext()).apply {
                        textSize = 30f
                        text = name+": "+number
                        setOnClickListener( View.OnClickListener { view ->
                            val intent = Intent(Intent.ACTION_DIAL)
                            intent.data = Uri.parse("tel:$number")
                            startActivity(intent)
                        })
                    })
                }
            }
            cursor?.close()
        }

    }

    @SuppressLint("Range")
    private fun getNumberFromID(сontentResolver: ContentResolver, contactId: String) :String {
        val phones = сontentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null
        )
        var number: String? = null
        phones?.let { cursor ->
            while (cursor.moveToNext()) {
                number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            }
        }
        return number!!
    }

    private fun mRequestPermission() {
        requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), REQUEST_CODE_CONTACTS)
    }

    fun checkContacts() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getContacts()
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
            alert()
        } else {
            mRequestPermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_CONTACTS) {
            for (i in permissions.indices) {
                if (permissions[i] == Manifest.permission.READ_CONTACTS && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    getContacts()
                } else {
                    alert()
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun alert() {
        AlertDialog.Builder(requireContext())
            .setTitle("Access to contacts")
            .setMessage("This is necessary for the application to work correctly")
            .setPositiveButton("Grant access") { _, _ ->
                mRequestPermission()
            }
            .setNegativeButton("Not necessary") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    companion object {
        fun newInstance() =
            MainFragment()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}