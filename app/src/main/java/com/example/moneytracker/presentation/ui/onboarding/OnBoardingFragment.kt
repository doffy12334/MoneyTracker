package com.example.moneytracker.presentation.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.moneytracker.R
import com.example.moneytracker.databinding.FragmentOnBoardingBinding

class OnBoardingFragment : Fragment() {
    private var _binding: FragmentOnBoardingBinding? = null
    private val binding get() = _binding!!
    private var currentPage = 0

    private val pages = listOf(
        OnboardingPage(
            imageRes = R.drawable.img_intro_finance,
            titleRes = R.string.onboarding_smart_finance_title,
            subtitleRes = R.string.onboarding_smart_finance_subtitle,
            buttonTextRes = R.string.onboarding_start,
            showSkip = true,
            showBadges = false,
            imagePaddingDp = 0
        ),
        OnboardingPage(
            imageRes = R.drawable.img_finance,
            titleRes = R.string.onboarding_manage_finance_title,
            subtitleRes = R.string.onboarding_manage_finance_subtitle,
            buttonTextRes = R.string.onboarding_continue,
            showSkip = true,
            showBadges = true,
            imagePaddingDp = 22
        ),
        OnboardingPage(
            imageRes = R.drawable.img_savings_piggy,
            titleRes = R.string.onboarding_easy_saving_title,
            subtitleRes = R.string.onboarding_easy_saving_subtitle,
            buttonTextRes = R.string.onboarding_continue,
            showSkip = false,
            showBadges = false,
            imagePaddingDp = 0
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnBoardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentPage = savedInstanceState?.getInt(KEY_CURRENT_PAGE) ?: 0
        renderPage(currentPage, animate = false)

        binding.btnGetStarted.setOnClickListener {
            if (currentPage < pages.lastIndex) {
                val nextPage = currentPage + 1
                renderPage(nextPage, animate = true)
                currentPage = nextPage
            } else {
                navigateToLogin()
            }
        }
        binding.btnSkip.setOnClickListener {
            navigateToLogin()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_CURRENT_PAGE, currentPage)
    }

    private fun renderPage(index: Int, animate: Boolean) {
        if (!animate) {
            applyPage(index)
            return
        }

        val views = animatedViews()
        views.forEachIndexed { viewIndex, pageView ->
            pageView.animate()
                .alpha(0f)
                .translationY(10.dp().toFloat())
                .setDuration(110L)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction(
                    if (viewIndex == 0) {
                        Runnable {
                            applyPage(index)
                            views.forEach {
                                it.alpha = 0f
                                it.translationY = 18.dp().toFloat()
                                it.animate()
                                    .alpha(1f)
                                    .translationY(0f)
                                    .setDuration(260L)
                                    .setInterpolator(DecelerateInterpolator())
                                    .start()
                            }
                        }
                    } else {
                        null
                    }
                )
                .start()
        }
    }

    private fun applyPage(index: Int) {
        val page = pages[index]
        val padding = page.imagePaddingDp.dp()

        binding.imgHero.setImageResource(page.imageRes)
        binding.imgHero.setPadding(padding, padding, padding, padding)
        binding.tvTitle.setText(page.titleRes)
        binding.tvSubtitle.setText(page.subtitleRes)
        binding.btnGetStarted.setText(page.buttonTextRes)
        binding.btnSkip.isVisible = page.showSkip
        binding.profitBadge.isVisible = page.showBadges
        binding.walletBadge.isVisible = page.showBadges

        renderIndicators(index)
    }

    private fun renderIndicators(activeIndex: Int) {
        val indicators = listOf(binding.indicatorFirst, binding.indicatorSecond, binding.indicatorThird)
        indicators.forEachIndexed { index, indicator ->
            val isActive = index == activeIndex
            indicator.setBackgroundResource(
                if (isActive) R.drawable.bg_indicator_active else R.drawable.bg_indicator_inactive
            )
            indicator.layoutParams.width = if (isActive) 24.dp() else 6.dp()
            indicator.layoutParams.height = 6.dp()
            indicator.requestLayout()
        }
    }

    private fun animatedViews(): List<View> {
        return listOf(
            binding.heroPanel,
            binding.contentGroup,
            binding.indicatorGroup,
            binding.btnGetStarted
        )
    }

    private fun navigateToLogin() {
        findNavController().navigate(R.id.action_onboarding_to_login)
    }

    private fun Int.dp(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private data class OnboardingPage(
        val imageRes: Int,
        val titleRes: Int,
        val subtitleRes: Int,
        val buttonTextRes: Int,
        val showSkip: Boolean,
        val showBadges: Boolean,
        val imagePaddingDp: Int
    )

    companion object {
        private const val KEY_CURRENT_PAGE = "current_page"
    }
}
