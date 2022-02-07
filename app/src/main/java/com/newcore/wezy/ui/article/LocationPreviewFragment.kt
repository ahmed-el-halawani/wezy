package com.newcore.wezy.ui.article

import androidx.navigation.fragment.navArgs
import com.newcore.wezy.databinding.FragmentArticleBinding
import com.newcore.wezy.ui.BaseFragment

class LocationPreviewFragment
    : BaseFragment<FragmentArticleBinding>( FragmentArticleBinding::inflate ) {

    private val locationArgs:LocationPreviewFragmentArgs by navArgs()

}