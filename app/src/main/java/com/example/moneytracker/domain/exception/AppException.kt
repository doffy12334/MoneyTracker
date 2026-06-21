package com.example.moneytracker.domain.exception

import androidx.annotation.StringRes

/**
 * Một lớp Exception tùy chỉnh để thay thế các Exception mặc định.
 * Thay vì chứa câu lỗi dạng String (cứng), nó sẽ chứa ID của chuỗi (String Resource ID)
 * để hỗ trợ hiển thị đa ngôn ngữ trên giao diện.
 */
class AppException(@StringRes val messageResId: Int) : Exception()
