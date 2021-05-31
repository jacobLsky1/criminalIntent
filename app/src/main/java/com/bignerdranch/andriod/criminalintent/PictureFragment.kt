package com.bignerdranch.andriod.criminalintent

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import java.io.File

private const val ARG_IMAGE = "image"

class PictureDialogFragment: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return activity?.let {


            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)

            //get the layout inflater
            val inflater = requireActivity().layoutInflater

            //get a dialog picture view reference
            // Pass null as the parent view because its going in the dialog layout
            val view = inflater.inflate(R.layout.crime_picture, null)

            // Inflate and set the layout for the dialog
            builder.setView(view)

            //get reference to crimePicture image view
            val crimePicture = view.findViewById(R.id.crimePicture) as ImageView

            //get the image file path argument
            val photoFile = arguments?.getSerializable(ARG_IMAGE) as File

            //get the scaled image
            val bitmap = getScaledBitmap(photoFile.path, requireActivity())

            //set the picture in the crimePicture view
            crimePicture.setImageBitmap(bitmap)


            //set the dialog characteristics
            builder.setTitle("Crime Photo")
                .setNegativeButton("RETURN", DialogInterface.OnClickListener { _, _ -> dialog?.cancel() })


            // Create the AlertDialog object and return it
            builder.create()

        } ?: throw IllegalStateException("Acitivity cannot be null")

    }


    companion object {
        fun newInstance(photoFile: File): PictureDialogFragment {
            val args = Bundle().apply { putSerializable(ARG_IMAGE, photoFile) }

            return PictureDialogFragment().apply { arguments = args }

        }
    }
}