package com.example.moneytracker.presentation.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import com.example.moneytracker.R
import com.example.moneytracker.databinding.FragmentSettingBinding

class SettingFragment : Fragment() {
    private var _binding : FragmentSettingBinding? = null
    private val binding get() = _binding!!
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Xu ly btn log out
        binding.btnLogout.setOnClickListener {
            // 1. Đăng xuất khỏi Firebase
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()

            // 2. Điều hướng về màn hình Login và xóa sạch lịch sử các màn hình trước đó
            findNavController().navigate(R.id.loginFragment, null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()
            )

            Toast.makeText(context, "Đã đăng xuất", Toast.LENGTH_SHORT).show()
        }
    }
}