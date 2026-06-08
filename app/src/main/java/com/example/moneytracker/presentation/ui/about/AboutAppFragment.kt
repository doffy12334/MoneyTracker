package com.example.moneytracker.presentation.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.moneytracker.R
import com.example.moneytracker.databinding.FragmentAboutAppBinding

class AboutAppFragment : Fragment() {
    private var _binding: FragmentAboutAppBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutAppBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.rowRate.setOnClickListener {
            Toast.makeText(requireContext(), R.string.about_rate_thanks, Toast.LENGTH_SHORT).show()
        }
        binding.rowSupport.setOnClickListener {
            Toast.makeText(requireContext(), R.string.about_support_message, Toast.LENGTH_LONG).show()
        }
        binding.rowPrivacy.setOnClickListener {
            showInfoDialog(
                title = getString(R.string.about_privacy_title),
                message = getString(R.string.about_privacy_message)
            )
        }
        binding.rowTerms.setOnClickListener {
            showInfoDialog(
                title = getString(R.string.about_terms_title),
                message = getString(R.string.about_terms_message)
            )
        }
    }

    private fun showInfoDialog(title: String, message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.action_ok, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
