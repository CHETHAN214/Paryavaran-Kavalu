package com.paryavaran.kavalu.ui.report

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.paryavaran.kavalu.R
import com.paryavaran.kavalu.databinding.DialogSuccessBinding

class SuccessOverlayDialog : DialogFragment() {

    private var _binding: DialogSuccessBinding? = null
    private val binding get() = _binding!!
    private var onClose: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        dialog?.window?.apply {
            requestFeature(Window.FEATURE_NO_TITLE)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        _binding = DialogSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val karmaEarned = arguments?.getInt("karma", 10) ?: 10
        binding.tvKarmaEarned.text = "+$karmaEarned 🌿 Eco-Karma"

        binding.btnViewOnMap.setOnClickListener {
            dismiss()
            onClose?.invoke()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(karmaEarned: Int, onClose: () -> Unit): SuccessOverlayDialog {
            return SuccessOverlayDialog().apply {
                this.onClose = onClose
                arguments = Bundle().apply { putInt("karma", karmaEarned) }
            }
        }
    }
}
