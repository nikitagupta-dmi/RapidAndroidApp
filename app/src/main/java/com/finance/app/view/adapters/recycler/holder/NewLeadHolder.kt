package com.finance.app.view.adapters.recycler.holder

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.finance.app.R
import com.finance.app.databinding.ItemNewLeadBinding
import com.finance.app.persistence.model.AllLeadMaster
import com.finance.app.utility.ConvertDate
import com.finance.app.view.activity.LeadDetailActivity
import motobeans.architecture.util.setDrawableColor

class NewLeadHolder(val binding: ItemNewLeadBinding,val mContext:Context) : RecyclerView.ViewHolder(binding.root) {
    fun bindItems(lead: AllLeadMaster) {
        binding.tvLeadName.text = lead.applicantFirstName
        binding.tvLeadStatus.text = lead.status
        binding.tvLoanType.text = lead.loanProductName
        binding.tvLeadAddress.text = lead.applicantAddress
        binding.tvDateAndTime.text = ConvertDate().convertDate(lead.createdOn!!)

        binding.tvDateAndTime.setDrawableColor(R.color.gradient_black)
        binding.tvLeadStatus.setDrawableColor(R.color.md_green_300)

        binding.leadCard.setOnClickListener {
            LeadDetailActivity.start(mContext, lead.leadID)
        }
    }
}