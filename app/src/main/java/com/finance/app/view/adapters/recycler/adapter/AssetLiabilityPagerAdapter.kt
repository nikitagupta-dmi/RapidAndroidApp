package com.finance.app.view.adapters.recycler.adapter

import android.util.SparseArray
import androidx.core.util.set
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.finance.app.persistence.model.PersonalApplicantsModel
import com.finance.app.view.fragment.loanApplicationFragments.AssetLiabilityFragmentForm

class AssetLiabilityPagerAdapter internal constructor(fm: FragmentManager, val applicantsList: ArrayList<PersonalApplicantsModel>) : FragmentStatePagerAdapter(fm) {

    private val hmFragments = SparseArray<AssetLiabilityFragmentForm>()

    override fun getItem(position: Int): Fragment {
        val fragmentItem = AssetLiabilityFragmentForm.newInstance(applicantsList[position].leadApplicantNumber.toString())
        hmFragments[position] = fragmentItem
        return fragmentItem
    }

    override fun getPageTitle(position: Int): CharSequence? {
        //TODO check for main applicant...
        return if (position == 0) "Applicant" else "CoApplicant ${position}"
    }

    override fun getCount(): Int {
        return applicantsList.size
    }

    fun getAllFragments() = hmFragments
}