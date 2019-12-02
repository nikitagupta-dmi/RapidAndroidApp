package com.finance.app.view.adapters.recycler.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.finance.app.R
import com.finance.app.databinding.ItemReferenceBinding
import com.finance.app.persistence.model.ReferenceModel

class ReferenceAdapter(private val c: Context, private val references: ArrayList<ReferenceModel>) : RecyclerView.Adapter<ReferenceAdapter.ReferenceViewHolder>() {
    private lateinit var binding: ItemReferenceBinding
    private var mOnItemClickListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReferenceViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.item_reference, parent, false)
        return ReferenceViewHolder(binding, c)
    }

    override fun getItemCount() = references.size

    override fun onBindViewHolder(holder: ReferenceViewHolder, position: Int) {
        holder.bindItems(references[position], position)
    }

    fun setOnItemClickListener(listener: ItemClickListener) {
        mOnItemClickListener = listener
    }

    interface ItemClickListener {
        fun onDeleteClicked(position: Int)
        fun onEditClicked(position: Int, reference: ReferenceModel)
    }

    inner class ReferenceViewHolder(val binding: ItemReferenceBinding, val c: Context) : RecyclerView.ViewHolder(binding.root) {
        fun bindItems(reference: ReferenceModel, position: Int) {
            binding.tvName.text = reference.name
            binding.tvAddress.text = reference.address
            binding.tvContact.text = reference.contactNumber
            binding.tvKnownSince.text = reference.knowSince
            addClickListener(position, reference)
        }

        private fun addClickListener(position: Int, reference: ReferenceModel) {
            binding.btnDelete.setOnClickListener {
                mOnItemClickListener!!.onDeleteClicked(position)
            }

            binding.btnEdit.setOnClickListener {
                mOnItemClickListener!!.onEditClicked(position, reference)
            }
        }
    }
}
