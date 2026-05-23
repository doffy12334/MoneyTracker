package com.example.moneytracker.presentation.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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
            Toast.makeText(requireContext(), "Cam on ban da danh gia Financier", Toast.LENGTH_SHORT).show()
        }
        binding.rowSupport.setOnClickListener {
            Toast.makeText(requireContext(), "Email ho tro: support@financier.app", Toast.LENGTH_LONG).show()
        }
        binding.rowPrivacy.setOnClickListener {
            showInfoDialog(
                title = "Chinh sach bao mat",
                message = "Financier chi luu du lieu can thiet de van hanh tai khoan va bao ve lich su giao dich cua ban."
            )
        }
        binding.rowTerms.setOnClickListener {
            showInfoDialog(
                title = "Dieu khoan su dung",
                message = "Bang cach su dung ung dung, ban dong y bao mat thong tin dang nhap va chiu trach nhiem voi du lieu da nhap."
            )
        }
    }

    private fun showInfoDialog(title: String, message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Da hieu", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
