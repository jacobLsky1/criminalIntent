package com.bignerdranch.andriod.criminalintent

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.*
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.Observer
import java.io.File
import java.util.*

private const val ARG_CRIME_ID = "crime_id"
private const val TAG = "CrimeFragment"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_PICTURE = "DialogePicture"
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_TIME = 1
private  const val REQUEST_DATE = 0
private const val REQUEST_CONTACT = 2
private const val REQUEST_PHOTO = 3
private  const val DATE_FORMAT = "EEE,MMM,dd"




class CrimeFragment : Fragment(),DatePickerFragment.Callbacks,TimePickerFragment.Callbacks {

    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton:Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton:Button
    private  lateinit var suspectButton:Button
    private lateinit var callButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView
    private  lateinit var photoFile: File
    private lateinit var photoUri: Uri


    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {

        ViewModelProviders.of(this).get(CrimeDetailViewModel::class.java)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime= Crime()
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        Log.d(TAG, "args bundle crime ID: $crimeId")
              // Eventually, load crime from database
        crimeDetailViewModel.loadCrime(crimeId)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)

        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        timeButton = view.findViewById(R.id.crime_time) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        reportButton = view.findViewById(R.id.crime_report) as Button
        suspectButton = view.findViewById(R.id.crime_suspect)as Button
        callButton = view.findViewById(R.id.crime_call) as Button
        photoButton = view.findViewById(R.id.crime_camera) as ImageButton
        photoView = view.findViewById(R.id.crime_photo)as ImageView


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState:
    Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            Observer { crime ->
                crime?.let {
                    this.crime = crime
                    photoFile = crimeDetailViewModel.getPhotoFile(crime)
                    photoUri = FileProvider.getUriForFile(requireActivity(), "com.bignerdranch.andriod.criminalintent.fileprovider", photoFile)
                    updateUI()

                }
            })
    }

    fun getItemViewType(view:View):Int{
        /*var crime = Crime()
        return if(crime.requiresPolice)
            1
        else
            0*/
        return 0
    }

    //****************
    override fun onStart() {
        super.onStart()
        val titleWatcher = object : TextWatcher {//textWatcher is an Interface
            override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int)
            {
// This space intentionally left blank
            }
            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                crime.title = sequence.toString()
            }
            override fun afterTextChanged(sequence: Editable?) {
// This one too
            }
        }
        titleField.addTextChangedListener(titleWatcher)
        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }
        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_DATE)

                show(this@CrimeFragment.requireFragmentManager(), DIALOG_TIME)
            }
        }
        timeButton.setOnClickListener {
            TimePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_TIME)
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_DATE)
            }
        }
        reportButton.setOnClickListener{
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
            }.also { intent ->
                val chooserIntent = Intent.createChooser(intent,getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }
        suspectButton.apply {
            val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            setOnClickListener {
                startActivityForResult(pickContactIntent,REQUEST_CONTACT)
            }
           /* val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                isEnabled = false
            }*/

            callButton.setOnClickListener {
                val callContactIntent =
                    Intent(Intent.ACTION_DIAL).apply {

                        val phone = crime.suspectNumber
                        data = Uri.parse("tel:$phone")
                    }
                // this intent will call the phone number given in Uri.parse("tel:$phone")
                startActivity(callContactIntent)
            }

            photoButton.apply {
                val packageManager: PackageManager = requireActivity().packageManager
                val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(captureImage, PackageManager.MATCH_DEFAULT_ONLY)

                /*if (resolvedActivity == null) {
                    isEnabled = false
                }*/

                setOnClickListener {
                    captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    val cameraActivities: List<ResolveInfo> = packageManager.queryIntentActivities(captureImage, PackageManager.MATCH_DEFAULT_ONLY)
                    for (cameraActivity in cameraActivities) {
                        requireActivity().grantUriPermission(cameraActivity.activityInfo.packageName, photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    }


                    startActivityForResult(captureImage, REQUEST_PHOTO)
                }
            }
            photoView.setOnClickListener {
                if (photoFile.exists())
                {
                    PictureDialogFragment.newInstance(photoFile).apply { show(this@CrimeFragment.parentFragmentManager, DIALOG_PICTURE ) }
                }
            }
        }

        //calling the packege maneger to inspect if the divice can acsess the contact info so not to cause a crash
    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    override fun onDateSelected(date: Date) {
        crime.date = date
        updateUI()
    }


    private fun updateUI(){
        titleField.setText(crime.title)
        val crimeDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            SimpleDateFormat(resources.getString(R.string.date_format)).format(this.crime.date)
        } else {
            TODO("VERSION.SDK_INT < N")
        }
        dateButton.text= crimeDate
        val crimeTime = SimpleDateFormat("hh:mm a").format(this.crime.date)
        timeButton.text = crimeTime
       solvedCheckBox.apply {
           isChecked = crime.isSolved
           jumpDrawablesToCurrentState()
       }
        if(crime.suspect.isNotEmpty())
        {
            suspectButton.text = crime.suspect
        }
        updatePhotoView()
    }

    private fun updatePhotoView() {
        if (photoFile.exists()) {
            val bitmap = getScaledBitmap(photoFile.path,
                requireActivity())
            photoView.setImageBitmap(bitmap)
        } else {
            photoView.setImageDrawable(null)
        }
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode != Activity.RESULT_OK -> return
            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri? = data.data
                // queryFieldsName: a List to return the DISPLAY_NAME Column Only
                val queryFieldsName = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                // queryFieldsId: a List to return the _ID Column Only, i will use it to get the suspect Id
                val queryFieldsId = arrayOf(ContactsContract.Contacts._ID)
                val cursorName = requireActivity().contentResolver.query(contactUri!!, queryFieldsName, null, null, null)
                cursorName?.use {
                    if (it.count == 0) {
                        Log.d("CrimeFragment","it.count = 0")
                        return
                    }

                    it.moveToFirst()
                    val suspect = it.getString(0)
                    crime.suspect = suspect
                    suspectButton.text = suspect
                }

                val cursorId = requireActivity().contentResolver.query(contactUri!!, queryFieldsId, null, null, null)
                cursorId?.use {
                    if (it.count == 0) {
                        return
                    }

                    it.moveToFirst()
                    val contactId = it.getString(0)
                    val phoneURI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                    val phoneNumberQueryFields = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val phoneWhereClause = "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?"
                    val phoneQueryParameters = arrayOf(contactId)

                    val phoneCursor = requireActivity().contentResolver
                        .query(phoneURI, phoneNumberQueryFields, phoneWhereClause, phoneQueryParameters, null )

                    phoneCursor?.use { cursorPhone ->
                        cursorPhone.moveToFirst()
                        val phoneNumValue = cursorPhone.getString(0)

                        crime.suspectNumber = phoneNumValue
                    }
                    crimeDetailViewModel.saveCrime(crime)
                }
            }
            requestCode == REQUEST_PHOTO -> {
                requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                updatePhotoView()
            }

        }
    }





    private  fun getCrimeReport():String{
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }
        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        var suspect = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }
        return getString(R.string.crime_report,
            crime.title, dateString, solvedString, suspect)
    }
    companion object {
        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }

    override fun onTimeSelected(date: Date) {
        crime.date = date
        updateUI()
    }

}