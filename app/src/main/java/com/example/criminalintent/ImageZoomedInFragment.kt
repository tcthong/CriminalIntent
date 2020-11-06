package com.example.criminalintent

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import java.io.File

private const val ARG_PATH = "path"

class ImageZoomedInFragment : DialogFragment() {
    private lateinit var photoImg: ImageView

    companion object {
        fun newInstance(path: String): ImageZoomedInFragment {
            val args = Bundle().apply {
                putString(ARG_PATH, path)
            }

            return ImageZoomedInFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.AppTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_image_zoomed_in, container, false)
        photoImg = view.findViewById(R.id.crime_photo_img)

        val path: String? = arguments?.getString(ARG_PATH)
        path?.let {
            val file = File(it)
            if (file.exists()) {
                photoImg.setImageBitmap(getScaledBitmap(it, requireActivity()))
            } else {
                photoImg.setImageDrawable(null)
            }
        }

        return view
    }
}