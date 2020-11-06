package com.example.criminalintent

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import java.io.File
import java.util.*

private const val ARG_CRIME_ID = "crime_id"
private const val DATE_DIALOG = "date_dialog"
private const val ZOOMED_IN_DIALOG = "zoomed_in_dialog"
private const val REQUEST_CODE_DATE = 0
private const val REQUEST_CONTACT = 1
private const val REQUEST_PHOTO = 2
private const val DATE_FORMAT = "EEE dd/MM/yyyy"

class CrimeFragment : Fragment(), DatePickerFragment.Callbacks {
    private lateinit var crime: Crime
    private lateinit var titleEd: EditText
    private lateinit var dateBtn: Button
    private lateinit var isSolvedChk: CheckBox
    private lateinit var reportBtn: Button
    private lateinit var suspectBtn: Button
    private lateinit var photoImg: ImageView
    private lateinit var cameraBtn: ImageButton
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri
    private var widthPhotoImg: Int = 0
    private var heightPhotoImg: Int = 0
    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProvider(this).get(CrimeDetailViewModel::class.java)
    }

    companion object {
        fun newInstance(id: UUID): CrimeFragment {
            val args: Bundle = Bundle().apply {
                putSerializable(ARG_CRIME_ID, id)
            }

            return CrimeFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        crimeDetailViewModel.loadCrime(crimeId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_crime, container, false)
        titleEd = view.findViewById(R.id.crime_title_ed)
        dateBtn = view.findViewById(R.id.crime_date_btn)
        isSolvedChk = view.findViewById(R.id.crime_solved_chk)
        reportBtn = view.findViewById(R.id.crime_report_btn)
        suspectBtn = view.findViewById(R.id.crime_suspect_btn)
        photoImg = view.findViewById(R.id.crime_photo_img)
        cameraBtn = view.findViewById(R.id.crime_camera_btn)

        photoImg.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                photoImg.viewTreeObserver.removeOnGlobalLayoutListener(this)
                widthPhotoImg = photoImg.width
                heightPhotoImg = photoImg.height
            }
        })

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            { crime ->
                crime?.let {
                    this.crime = crime
                    photoFile = crimeDetailViewModel.getPhotoFile(crime)
                    photoUri = FileProvider
                        .getUriForFile(
                            requireActivity(),
                            "com.example.criminalintent.fileprovider",
                            photoFile
                        )
                    updateUi()
                }
            }
        )
    }

    override fun onStart() {
        super.onStart()

        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                crime.title = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        }
        titleEd.addTextChangedListener(titleWatcher)

        isSolvedChk.setOnCheckedChangeListener { _, isChecked ->
            crime.isSolved = isChecked
        }

        dateBtn.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_CODE_DATE)
                show(this@CrimeFragment.parentFragmentManager, DATE_DIALOG)
            }
        }

        reportBtn.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
            }.also { intent ->
                val chooserIntent = Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }

        suspectBtn.apply {
            val contactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            setOnClickListener {
                startActivityForResult(contactIntent, REQUEST_CONTACT)
            }

            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity =
                packageManager.resolveActivity(contactIntent, PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                isEnabled = false
            }
        }

        cameraBtn.apply {
            val photoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val packageManager = requireActivity().packageManager

            val resolvedActivity =
                packageManager.resolveActivity(photoIntent, PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null) {
                isEnabled = false
            }

            setOnClickListener {
                photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                val cameraActivities: List<ResolveInfo> =
                    packageManager.queryIntentActivities(
                        photoIntent,
                        PackageManager.MATCH_DEFAULT_ONLY
                    )

                for (cameraActivity in cameraActivities) {
                    requireActivity().grantUriPermission(
                        cameraActivity.activityInfo.packageName,
                        photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                }

                startActivityForResult(photoIntent, REQUEST_PHOTO)
            }

            photoImg.setOnClickListener {
                val imageZoomedInFragment = ImageZoomedInFragment.newInstance(photoFile.path)
                imageZoomedInFragment.show(parentFragmentManager, ZOOMED_IN_DIALOG)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    private fun updateUi() {
        titleEd.setText(crime.title)
        dateBtn.text =
            java.text.DateFormat.getDateInstance(java.text.DateFormat.LONG).format(crime.date)
        isSolvedChk.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }

        crime.suspect
            .takeIf { it.isNotBlank() }
            ?.also { suspectBtn.text = it }

        updatePhotoView()
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }

    private fun updatePhotoView() {
        if (!photoFile.exists() || widthPhotoImg == 0) {
            photoImg.setImageDrawable(null)
            photoImg.contentDescription = getString(R.string.crime_photo_no_image_description)
        } else {
            photoImg.setImageBitmap(getScaledBitmap(photoFile.path, widthPhotoImg, heightPhotoImg))
            photoImg.contentDescription = getString(R.string.crime_photo_image_description)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when {
            resultCode != Activity.RESULT_OK -> return
            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri? = data.data
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                val cursor: Cursor? = contactUri?.let {
                    requireActivity()
                        .contentResolver
                        .query(it, queryFields, null, null, null)
                }

                cursor?.use {
                    if (it.count == 0) {
                        return
                    }

                    it.moveToFirst()
                    val suspect = it.getString(0)
                    crime.suspect = suspect
                    crimeDetailViewModel.saveCrime(crime)
                }
            }

            requestCode == REQUEST_PHOTO -> {
                requireActivity().revokeUriPermission(
                    photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                updatePhotoView()
            }
        }
    }

    private fun getCrimeReport(): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }
        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()

        val suspectString = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }

        return getString(
            R.string.crime_report,
            crime.title, dateString, solvedString, suspectString
        )
    }

    override fun onDateSelected(date: Date) {
        crime.date = date
        updateUi()
    }
}