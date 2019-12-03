package com.finance.app.view.adapters.recycler.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.finance.app.R
import com.finance.app.databinding.ItemAssetBinding
import com.finance.app.databinding.ItemBankBinding
import com.finance.app.persistence.model.AssetLiability
import com.finance.app.persistence.model.BankDetailBean

class BankDetailAdapter(private val c: Context, private val bankDetails: ArrayList<BankDetailBean>) : RecyclerView.Adapter<BankDetailAdapter.BankDetailViewHolder>() {
    private lateinit var binding: ItemBankBinding
    private var mOnBankDetailClickListener: BankDetailClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BankDetailViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.item_bank, parent, false)
        return BankDetailViewHolder(binding, c)
    }

    override fun getItemCount() = bankDetails.size

    fun setOnBankDetailClickListener(listener: BankDetailClickListener) {
        mOnBankDetailClickListener = listener
    }

    interface BankDetailClickListener {
        fun onBankDetailDeleteClicked(position: Int)
        fun onBankDetailEditClicked(position: Int, bank: BankDetailBean)
    }

    override fun onBindViewHolder(holder: BankDetailViewHolder, position: Int) {
        holder.bindItems(position, bankDetails[position])
    }

    inner class BankDetailViewHolder(val binding: ItemBankBinding, val c: Context) : RecyclerView.ViewHolder(binding.root) {
        fun bindItems(position: Int, bank: BankDetailBean) {
            binding.tvAccountHolder.text = bank.accountHolderName.toString()
            binding.tvAccountNum.text = bank.accountNumber.toString()
            binding.tvSalaryCreditedNum.text = bank.numberOfCredit.toString()
            addClickListener(position, bank)
        }

        private fun addClickListener(position: Int, bank: BankDetailBean) {
            binding.btnDelete.setOnClickListener {
                mOnBankDetailClickListener!!.onBankDetailDeleteClicked(position)
            }

            binding.btnEdit.setOnClickListener {
                mOnBankDetailClickListener!!.onBankDetailEditClicked(position, bank)
            }
        }
    }
}