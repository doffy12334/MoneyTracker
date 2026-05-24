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
            Toast.makeText(requireContext(), "Cảm ơn bạn đã đánh giá Financier", Toast.LENGTH_SHORT).show()
        }
        binding.rowSupport.setOnClickListener {
            Toast.makeText(requireContext(), "Email hỗ trợ: support@financier.app", Toast.LENGTH_LONG).show()
        }
        binding.rowPrivacy.setOnClickListener {
            showInfoDialog(
                title = "Chính sách bảo mật",
                message = "Financier chỉ lưu dữ liệu cần thiết để vận hành tài khoản và bảo vệ lịch sử giao dịch của bạn."
            )
        }
        binding.rowTerms.setOnClickListener {
            showInfoDialog(
                title = "Điều khoản sử dụng",
                message = "Bằng cách sử dụng ứng dụng, bạn đồng ý bảo mật thông tin đăng nhập và chịu trách nhiệm với dữ liệu đã nhập."
            )
        }
    }

    private fun showInfoDialog(title: String, message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Đã hiểu", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
