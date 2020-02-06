package com.finance.app.view.customViews

import android.app.Dialog
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.finance.app.R
import com.finance.app.databinding.*
import com.finance.app.persistence.model.*
import com.finance.app.utility.CurrencyConversion
import com.finance.app.utility.LeadMetaData
import com.finance.app.utility.SelectDate
import com.finance.app.view.adapters.recycler.adapter.AssetDetailAdapter
import com.finance.app.view.adapters.recycler.adapter.CardDetailAdapter
import com.finance.app.view.adapters.recycler.adapter.ObligationAdapter
import com.finance.app.view.adapters.recycler.spinner.MasterSpinnerAdapter
import kotlinx.android.synthetic.main.add_assests_dialog.*
import kotlinx.android.synthetic.main.add_assests_dialog.cancel_bttn
import kotlinx.android.synthetic.main.add_assests_dialog.view.*
import kotlinx.android.synthetic.main.add_obligation_dialog.*
import kotlinx.android.synthetic.main.asset_creditcard_dialog.*
import kotlinx.android.synthetic.main.delete_dialog.view.*
import kotlinx.android.synthetic.main.layout_credit_card_details_new.etCreditCardLimit
import kotlinx.android.synthetic.main.layout_credit_card_details_new.etCurrentUtilization
import kotlinx.android.synthetic.main.layout_credit_card_details_new.etLastPaymentDate
import kotlinx.android.synthetic.main.layout_credit_card_details_new.spinnerBankName
import kotlinx.android.synthetic.main.layout_credit_card_details_new.spinnerObligate
import motobeans.architecture.application.ArchitectureApp
import motobeans.architecture.development.interfaces.DataBaseUtil
import motobeans.architecture.development.interfaces.FormValidation
import motobeans.architecture.util.AppUtilExtensions
import motobeans.architecture.util.AppUtils.showToast
import javax.inject.Inject

class CustomAssetLiabilityViewInfo @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs), AssetDetailAdapter.AssetClickListener, CardDetailAdapter.CardClickListener,
        ObligationAdapter.ObligationClickListener {

    private val TAG = this.javaClass.canonicalName
    @Inject
    lateinit var dataBase: DataBaseUtil
    @Inject
    lateinit var formValidation: FormValidation


    private lateinit var binding: LayoutCustomviewAssetliabilityBinding
    private lateinit var activity: FragmentActivity


    private var allMasterDropDown: AllMasterDropDown? = null
    private var assetAdapter: AssetDetailAdapter? = null
    private var cardDetailAdapter: CardDetailAdapter? = null
    private var obligationAdapter: ObligationAdapter? = null
    private var currentApplicant: AssetLiabilityModel = AssetLiabilityModel()
    private var assetsList: ArrayList<AssetLiability> = ArrayList()
    private var cardDetailList: ArrayList<CardDetail> = ArrayList()
    private var obligationsList: ArrayList<ObligationDetail> = ArrayList()
    private var currentPosition = 0
    private var deleteDialog: Dialog? = null
    private var addAssestsDialog: Dialog? = null
    private var addCreditCardDialog: Dialog? = null
    private var addObligationDialog: Dialog? = null
    private val ASSET: Int = 1
    private val CARD: Int = 2
    private val OBLIGATION: Int = 3


    init {
        ArchitectureApp.instance.component.inject(this)
        binding = AppUtilExtensions.initCustomViewBinding(context = context, layoutId = R.layout.layout_customview_assetliability, container = this)

        initViews()
        setOnClickListener()
    }


    private fun initViews() {


    }

    private fun setOnClickListener() {
        binding.addasset.setOnClickListener { showDialogtoAddAssests() }
        binding.layoutCreditCard.addcreditdilaog.setOnClickListener { showDialogtoAddCreditCard() }
        binding.layoutObligations.addcreditdilaog.setOnClickListener { showDialogtoAddObligation() }


        binding.assetcounter.setOnClickListener() {
            binding.llAssetDetail.visibility = View.VISIBLE
            binding.pageIndicatorAsset.visibility = View.VISIBLE
            binding.rcAsset.visibility = View.VISIBLE

            binding.layoutObligations.pageIndicatorObligation.visibility = View.GONE
            binding.layoutObligations.rcObligation.visibility = View.GONE

            binding.layoutCreditCard.pageIndicatorCreditCard.visibility = View.GONE
            binding.layoutCreditCard.rcCreditCard.visibility = View.GONE
        }

        binding.tvAssetdetail.setOnClickListener() {
            binding.llAssetDetail.visibility = View.VISIBLE
            binding.pageIndicatorAsset.visibility = View.VISIBLE
            binding.rcAsset.visibility = View.VISIBLE

            binding.layoutObligations.pageIndicatorObligation.visibility = View.GONE
            binding.layoutObligations.rcObligation.visibility = View.GONE

            binding.layoutCreditCard.pageIndicatorCreditCard.visibility = View.GONE
            binding.layoutCreditCard.rcCreditCard.visibility = View.GONE
        }

        cardDetailFormListeners(binding.layoutCreditCard)
        obligationFormListeners(binding.layoutObligations)


    }


    fun initApplicantDetails(fragmentActivity: FragmentActivity, applicantId: Int) {
        activity = fragmentActivity

        //fetch details
        fetchDropDownsFromDB()
        fetchApplicantAssetsAndLibilityDetailsById(applicantId)
    }

    private fun setAssetAdapter(assets: ArrayList<AssetLiability>) {
        binding.rcAsset.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        assetAdapter = AssetDetailAdapter(context, assets)
        binding.rcAsset.adapter = assetAdapter
        assetAdapter?.setOnAssetClickListener(this)
        binding.pageIndicatorAsset.attachTo(binding.rcAsset)
        binding.assetcounter.setText(assets.size.toString())
    }

    private fun setCardDetailAdapter(cards: ArrayList<CardDetail>) {
        binding.layoutCreditCard.rcCreditCard.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        cardDetailAdapter = CardDetailAdapter(context, cards)
        binding.layoutCreditCard.rcCreditCard.adapter = cardDetailAdapter
        cardDetailAdapter?.setOnCardClickListener(this)
        binding.layoutCreditCard.pageIndicatorCreditCard.attachTo(binding.layoutCreditCard.rcCreditCard)
        binding.layoutCreditCard.creditcardcounter.setText(cards.size.toString())

    }

    private fun setObligationAdapter(obligations: ArrayList<ObligationDetail>) {
        binding.layoutObligations.rcObligation.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        obligationAdapter = ObligationAdapter(context, obligations)
        binding.layoutObligations.rcObligation.adapter = obligationAdapter
        obligationAdapter?.setOnObligationClickListener(this)
        binding.layoutObligations.pageIndicatorObligation.attachTo(binding.layoutObligations.rcObligation)
        binding.layoutObligations.obligationcounter.setText(obligations.size.toString())


    }


    private fun fetchApplicantAssetsAndLibilityDetailsById(applicantId: Int) {
        LeadMetaData.getLeadObservable().observe(activity, Observer { allLeadDetails ->
            allLeadDetails?.let {
                val selectedApplicantList = it.assetLiabilityData?.applicantDetails?.filter { assetsLiability -> applicantId == assetsLiability.applicantId }
                if (!selectedApplicantList.isNullOrEmpty()) {

                    if (selectedApplicantList[0].applicantAssetLiabilityList.isNullOrEmpty()) {
                        selectedApplicantList[0].applicantAssetLiabilityList = ArrayList()
                        setAssetAdapter(selectedApplicantList[0].applicantAssetLiabilityList!!)
                    } else setAssetAdapter(selectedApplicantList[0].applicantAssetLiabilityList!!)

                    if (selectedApplicantList[0].applicantCreditCardDetailList.isNullOrEmpty()) {
                        selectedApplicantList[0].applicantCreditCardDetailList = ArrayList()
                        setCardDetailAdapter(selectedApplicantList[0].applicantCreditCardDetailList!!)
                    } else setCardDetailAdapter(selectedApplicantList[0].applicantCreditCardDetailList!!)


                    if (selectedApplicantList[0].applicantExistingObligationList.isNullOrEmpty()) {
                        selectedApplicantList[0].applicantExistingObligationList = ArrayList()
                        setObligationAdapter(selectedApplicantList[0].applicantExistingObligationList!!)
                    } else setObligationAdapter(selectedApplicantList[0].applicantExistingObligationList!!)
                }
            }
        })
    }


    private fun initializeViews(applicant: AssetLiabilityModel) {
        proceedFurther(applicant)
//        setClickListeners()
        showData(applicant)
    }


    private fun showData(applicant: AssetLiabilityModel) {

        setUpCurrentApplicantDetails(applicant)

    }

    private fun showSetDataOnView(applicantList: ArrayList<AssetLiabilityModel>?) {

        for (applicant in applicantList!!) {
            if (applicant.isMainApplicant) {
                currentApplicant = applicant
            }
        }

        setUpCurrentApplicantDetails(currentApplicant)

    }


    private fun setUpCurrentApplicantDetails(currentApplicant: AssetLiabilityModel) {
        assetsList = currentApplicant.applicantAssetLiabilityList
        cardDetailList = currentApplicant.applicantCreditCardDetailList
        obligationsList = currentApplicant.applicantExistingObligationList
        setAssetAdapter(assetsList)
        setCardDetailAdapter(cardDetailList)
        setObligationAdapter(obligationsList)


    }

    private fun proceedFurther(applicant: AssetLiabilityModel?) {

        fetchDropDownsFromDB()
    }


    private fun fetchDropDownsFromDB() {
        dataBase.provideDataBaseSource().allMasterDropDownDao().getMasterDropdownValue().observe(activity,
                Observer { allMasterDropdown ->
                    allMasterDropdown?.let {
                        allMasterDropDown = it
                    }
                })
    }


    private fun assetFormListeners(binding: LayoutCustomviewAssetliabilityBinding) {

        binding.addasset.setOnClickListener() {
            showDialogtoAddAssests()
        }
    }

    // dialog for add asset
    private fun showDialogtoAddAssests() {

        val binding = DataBindingUtil.inflate<AddAssestsDialogBinding>(LayoutInflater.from(context), R.layout.add_assests_dialog, null, false)
        val mBuilder = AlertDialog.Builder(context)
                .setView(binding.root)
                .setCancelable(true)

        addAssestsDialog = mBuilder.show()
        binding.cancelBttn.cancel_bttn?.setOnClickListener() {
            addAssestsDialog?.dismiss()
        }

        addAssestsDialog?.spinnerAssetType?.adapter = MasterSpinnerAdapter(context, allMasterDropDown?.AssetDetail!!)
        addAssestsDialog?.spinnerAssetSubType?.adapter = MasterSpinnerAdapter(context, allMasterDropDown?.AssetSubType!!)
        addAssestsDialog?.spinnerOwnership?.adapter = MasterSpinnerAdapter(context, allMasterDropDown?.AssetOwnership!!)
        addAssestsDialog?.spinnerDocumentProof?.adapter = MasterSpinnerAdapter(context, allMasterDropDown!!.DocumentProof!!)

        addAssestsDialog?.btnAddAsset?.setOnClickListener() {
            if (formValidation.validateAssetsDialog(binding)) {
                val currentAsset = AssetLiability()
                val assetType = addAssestsDialog?.spinnerAssetType?.selectedItem as DropdownMaster?
                val assetSubType = addAssestsDialog?.spinnerAssetSubType?.selectedItem as DropdownMaster?
                val ownership = addAssestsDialog?.spinnerOwnership?.selectedItem as DropdownMaster?
                val documentProof = addAssestsDialog?.spinnerDocumentProof?.selectedItem as DropdownMaster?
                currentAsset.assetValue = CurrencyConversion().convertToNormalValue(addAssestsDialog?.etValue?.text.toString()).toInt()
                currentAsset.assetDetailsTypeDetailID = assetType?.typeDetailID
                currentAsset.subTypeOfAssetTypeDetailID = assetSubType?.typeDetailID
                currentAsset.ownershipTypeDetailID = ownership?.typeDetailID
                currentAsset.documentedProofTypeDetailID = documentProof?.typeDetailID


                assetsList?.add(currentAsset)
                assetAdapter?.notifyDataSetChanged()
                addAssestsDialog?.dismiss()
                showToast(context, "Add Successfully")
                setAssetAdapter(assetsList!!)

                getCurrentApplicant()


            } else {
                //showToast(getString(R.string.validation_error))
                Toast.makeText(context, context.getString(R.string.validation_error), Toast.LENGTH_SHORT).show()
            }
        }


    }


    //add obligation Detail
    private fun showDialogtoAddObligation() {
        val binding = DataBindingUtil.inflate<AddObligationDialogBinding>(LayoutInflater.from(context), R.layout.add_obligation_dialog, null, false)
        val mBuilder = AlertDialog.Builder(context)
                .setView(binding.root)
                .setCancelable(true)

        addObligationDialog = mBuilder.show()
        addObligationDialog?.cancel_bttn?.setOnClickListener() {
            addObligationDialog?.dismiss()

        }

        addObligationDialog?.spinnerObligate?.adapter = MasterSpinnerAdapter(context, allMasterDropDown?.Obligate!!)
        addObligationDialog?.spinnerObligate?.adapter = MasterSpinnerAdapter(context, allMasterDropDown?.Obligate!!)
        addObligationDialog?.spinnerLoanOwnership?.adapter = MasterSpinnerAdapter(context, allMasterDropDown?.LoanOwnership!!)
        addObligationDialog?.spinnerLoanType?.adapter = MasterSpinnerAdapter(context, allMasterDropDown?.LoanType!!)
        addObligationDialog?.spinnerRepaymentBank?.adapter = MasterSpinnerAdapter(context, allMasterDropDown?.RepaymentBank!!)
        addObligationDialog?.spinnerEmiPaidInSameMonth?.adapter = MasterSpinnerAdapter(context, allMasterDropDown?.BounceEmiPaidInSameMonth!!)

        addObligationDialog?.etDisbursementDate?.setOnClickListener() {
            SelectDate(addObligationDialog?.etDisbursementDate!!, context)
        }

        addObligationDialog?.cancel_bttn?.setOnClickListener() {

            addObligationDialog?.dismiss()
        }
        addObligationDialog?.btnAddObligation?.setOnClickListener() {


            if (formValidation.validateObligationDialog(binding)) {

                val currentObligation = ObligationDetail()
                val loanOwnership = binding.spinnerLoanOwnership.selectedItem as DropdownMaster?
                val obligate = addObligationDialog?.spinnerObligate?.selectedItem as DropdownMaster?
                val loanType = addObligationDialog?.spinnerLoanType?.selectedItem as DropdownMaster?
                val repaymentBank = addObligationDialog?.spinnerRepaymentBank?.selectedItem as DropdownMaster?
                val emiPaidInSameMonth = addObligationDialog?.spinnerEmiPaidInSameMonth?.selectedItem as DropdownMaster?
                currentObligation.numberOfBouncesInLastNineMonth = addObligationDialog?.etBouncesInLastNineMonths?.text.toString().toInt()
                currentObligation.numberOfBouncesInLastSixMonth = addObligationDialog?.etBouncesInLastSixMonths?.text.toString().toInt()
                currentObligation.financerName = addObligationDialog?.etFinancierName?.text.toString()
                currentObligation.loanAmount = CurrencyConversion().convertToNormalValue(addObligationDialog?.etLoanAmount?.text.toString()).toInt()
                currentObligation.emiAmount = CurrencyConversion().convertToNormalValue(addObligationDialog?.etEmiAmount?.text.toString()).toInt()
                currentObligation.loanAccountNumber = addObligationDialog?.etAccountNum?.text.toString()
                currentObligation.tenure = addObligationDialog?.etTenure?.text.toString().toInt()
                currentObligation.balanceTenure = addObligationDialog?.etBalanceTenure?.text.toString().toInt()
                currentObligation.loanOwnershipTypeDetailID = loanOwnership?.typeDetailID
                currentObligation.obligateTypeDetailID = obligate?.typeDetailID
                currentObligation.loanTypeTypeDetailID = loanType?.typeDetailID
                currentObligation.repaymentBankTypeDetailID = repaymentBank?.typeDetailID
                currentObligation.bounseEmiPaidInSameMonth = emiPaidInSameMonth?.typeDetailID

                obligationsList?.add(currentObligation)
                obligationAdapter?.notifyDataSetChanged()
                addObligationDialog?.dismiss()
                setObligationAdapter(obligationsList!!)


            } else {

                //showToast(context.getString(R.string.validation_error))
                Toast.makeText(context, context.getString(R.string.validation_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    // diaolog for credit card
    private fun showDialogtoAddCreditCard() {
        val binding = DataBindingUtil.inflate<AssetCreditcardDialogBinding>(LayoutInflater.from(context), R.layout.asset_creditcard_dialog, null, false)
        val mBuilder = AlertDialog.Builder(context)
                .setView(binding.root)
                .setCancelable(true)

        addCreditCardDialog = mBuilder.show()
        addCreditCardDialog?.cancel_bttn?.setOnClickListener() {
            addCreditCardDialog?.dismiss()
        }

        addCreditCardDialog?.spinnerBankName?.adapter = MasterSpinnerAdapter(context, allMasterDropDown?.BankName!!)
        addCreditCardDialog?.spinnerObligate?.adapter = MasterSpinnerAdapter(context, allMasterDropDown?.CreditCardObligation!!)
        addCreditCardDialog?.etLastPaymentDate?.setOnClickListener {
            SelectDate(addCreditCardDialog?.etLastPaymentDate!!, context)
        }

        addCreditCardDialog?.btnAddcrdetail?.setOnClickListener() {

            if (formValidation.validateCardsDialog(binding)) {
                val currentCard = CardDetail()
                val bankName = addCreditCardDialog?.spinnerBankName?.selectedItem as DropdownMaster?
                val obligate = addCreditCardDialog?.spinnerObligate?.selectedItem as DropdownMaster?
                currentCard.lastPaymentDate = addCreditCardDialog?.etLastPaymentDate?.text.toString()
                currentCard.cardLimit = addCreditCardDialog?.etCreditCardLimit!!.text.toString().toInt()
                currentCard.currentUtilization = addCreditCardDialog?.etCurrentUtilization!!.text.toString().toInt()
                currentCard.bankNameTypeDetailID = bankName?.typeDetailID
                currentCard.obligateTypeDetail = obligate?.typeDetailID

                cardDetailList?.add(currentCard)
                cardDetailAdapter?.notifyDataSetChanged()
                addCreditCardDialog?.dismiss()
                setCardDetailAdapter(cardDetailList!!)


            } else {
                Toast.makeText(context, context.getString(R.string.validation_error), Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun cardDetailFormListeners(layoutCreditCard: LayoutCreditCardDetailsBinding?) {
        binding.layoutCreditCard.tvCreditdetail.setOnClickListener() {
            binding.layoutCreditCard.rcCreditCard.visibility = View.VISIBLE
            binding.layoutCreditCard.pageIndicatorCreditCard.visibility = View.VISIBLE
            binding.pageIndicatorAsset.visibility = View.GONE
            binding.rcAsset.visibility = View.GONE
            binding.layoutObligations.pageIndicatorObligation.visibility = View.GONE
            binding.layoutObligations.rcObligation.visibility = View.GONE

        }

        binding.layoutCreditCard.creditcardcounter.setOnClickListener() {

            binding.layoutCreditCard.rcCreditCard.visibility = View.VISIBLE
            binding.layoutCreditCard.pageIndicatorCreditCard.visibility = View.VISIBLE
            binding.pageIndicatorAsset.visibility = View.GONE
            binding.rcAsset.visibility = View.GONE
            binding.layoutObligations.pageIndicatorObligation.visibility = View.GONE
            binding.layoutObligations.rcObligation.visibility = View.GONE

        }

    }

    private fun obligationFormListeners(layoutObligations: LayoutObligationBinding?) {
        binding.layoutObligations.tvObligationdetail.setOnClickListener() {

            binding.layoutObligations.pageIndicatorObligation.visibility = View.VISIBLE
            binding.layoutObligations.rcObligation.visibility = View.VISIBLE
            binding.layoutCreditCard.pageIndicatorCreditCard.visibility = View.GONE
            binding.layoutCreditCard.rcCreditCard.visibility = View.GONE
            binding.pageIndicatorAsset.visibility = View.GONE
            binding.rcAsset.visibility = View.GONE
        }
        binding.layoutObligations.obligationcounter.setOnClickListener() {

            binding.layoutObligations.pageIndicatorObligation.visibility = View.VISIBLE
            binding.layoutObligations.rcObligation.visibility = View.VISIBLE
            binding.layoutCreditCard.pageIndicatorCreditCard.visibility = View.GONE
            binding.layoutCreditCard.rcCreditCard.visibility = View.GONE
            binding.pageIndicatorAsset.visibility = View.GONE
            binding.rcAsset.visibility = View.GONE
        }

    }


    private fun deleteAsset(position: Int) {
        assetsList?.removeAt(position)
        binding.rcAsset?.adapter!!.notifyItemRemoved(position)
        deleteDialog?.dismiss()
    }

    private fun deleteCard(position: Int) {
        cardDetailList?.removeAt(position)
        binding.layoutCreditCard.rcCreditCard.adapter!!.notifyItemRemoved(position)
        deleteDialog?.dismiss()
    }

    private fun deleteObligation(position: Int) {
        obligationsList?.removeAt(position)
        binding.layoutObligations.rcObligation.adapter!!.notifyItemRemoved(position)
        deleteDialog?.dismiss()
    }


    override fun onAssetEditClicked(position: Int, asset: AssetLiability) {
        Log.e("Tag", "sandeep")
        showDialogtoEditAssests(position, asset)
    }

    override fun onCardDetailEditClicked(position: Int, card: CardDetail) {
        showDialogEditCreditCard(position, card)
    }

    override fun onObligationEditNewClick(position: Int, obligation: ObligationDetail) {
        showDialogToEditObligation(position, obligation)
    }


    override fun onAssetDeleteClicked(position: Int) = showAlertDialog(position, ASSET)

    override fun onObligationDeleteClicked(position: Int) = showAlertDialog(position, OBLIGATION)

    override fun onCardDetailDeleteClicked(position: Int) = showAlertDialog(position, CARD)


    private fun showAlertDialog(position: Int, formType: Int) {
        val deleteDialogView = LayoutInflater.from(context).inflate(R.layout.delete_dialog, null)
        val mBuilder = android.app.AlertDialog.Builder(context)
                .setView(deleteDialogView)
                .setTitle("Delete Detail")
        deleteDialog = mBuilder.show()
        deleteDialogView.tvDeleteConfirm.setOnClickListener {
            when (formType) {
                ASSET -> deleteAsset(position)
                CARD -> deleteCard(position)
                OBLIGATION -> deleteObligation(position)
            }
        }
        deleteDialogView.tvDonotDelete.setOnClickListener { deleteDialog?.dismiss() }
    }


    fun isValidAssetApplicant(): AssetLiabilityModel? {
        return if (formValidation.validateAssetLiabilityInfo(binding)) getCurrentApplicant()
        else null
    }


    fun getCurrentApplicant(): AssetLiabilityModel {
        val currentApplicant = AssetLiabilityModel()
        currentApplicant.isMainApplicant = currentPosition == 0
        currentApplicant.leadApplicantNumber = LeadMetaData.getLeadData()?.assetLiabilityData?.applicantDetails?.get(currentPosition)?.leadApplicantNumber
        currentApplicant.applicantAssetLiabilityList = assetsList
        currentApplicant.applicantCreditCardDetailList = cardDetailList
        currentApplicant.applicantExistingObligationList = obligationsList
        return currentApplicant
    }

    //not in use further remove
    override fun onObligationEditClicked(position: Int, obligation: ObligationDetail) {

    }


    private fun selectMasterDropdownValue(spinner: Spinner, id: Int?) {
        for (index in 0 until spinner.count - 1) {
            val obj = spinner.getItemAtPosition(index) as DropdownMaster
            if (obj.typeDetailID == id) {
                spinner.setSelection(index + 1)
                return
            }
        }
    }

    //edit asset item
    private fun showDialogtoEditAssests(position: Int, asset: AssetLiability) {
        val binding = DataBindingUtil.inflate<AddAssestsDialogBinding>(LayoutInflater.from(context), R.layout.add_assests_dialog, null, false)
        val mBuilder = AlertDialog.Builder(context)
                .setView(binding.root)
                .setCancelable(true)

        addAssestsDialog = mBuilder.show()
        binding.cancelBttn.cancel_bttn?.setOnClickListener() {
            addAssestsDialog?.dismiss()
        }

        binding?.spinnerAssetType?.adapter = MasterSpinnerAdapter(context, allMasterDropDown?.AssetDetail!!)
        binding?.spinnerAssetSubType?.adapter = MasterSpinnerAdapter(context, allMasterDropDown?.AssetSubType!!)
        binding?.spinnerOwnership?.adapter = MasterSpinnerAdapter(context, allMasterDropDown?.AssetOwnership!!)
        binding?.spinnerDocumentProof?.adapter = MasterSpinnerAdapter(context, allMasterDropDown!!.DocumentProof!!)

        selectMasterDropdownValue(binding.spinnerAssetType, asset.assetDetailsTypeDetailID)
        selectMasterDropdownValue(binding.spinnerAssetSubType, asset.subTypeOfAssetTypeDetailID)
        selectMasterDropdownValue(binding.spinnerOwnership, asset.ownershipTypeDetailID)
        selectMasterDropdownValue(binding.spinnerDocumentProof, asset.documentedProofTypeDetailID)
        binding.etValue.setText(asset.assetValue.toString())
        binding.btnAddAsset.setText(R.string.edit)


        addAssestsDialog?.btnAddAsset?.setOnClickListener() {
            if (formValidation.validateAssetsDialog(binding)) {
                val currentAsset = AssetLiability()
                val assetType = binding?.spinnerAssetType?.selectedItem as DropdownMaster?
                val assetSubType = binding?.spinnerAssetSubType?.selectedItem as DropdownMaster?
                val ownership = binding?.spinnerOwnership?.selectedItem as DropdownMaster?
                val documentProof = binding?.spinnerDocumentProof?.selectedItem as DropdownMaster?
                currentAsset.assetValue = CurrencyConversion().convertToNormalValue(binding?.etValue?.text.toString()).toInt()
                currentAsset.assetDetailsTypeDetailID = assetType?.typeDetailID
                currentAsset.subTypeOfAssetTypeDetailID = assetSubType?.typeDetailID
                currentAsset.ownershipTypeDetailID = ownership?.typeDetailID
                currentAsset.documentedProofTypeDetailID = documentProof?.typeDetailID

                assetsList?.set(position, currentAsset)
                assetAdapter?.notifyItemChanged(position)
                addAssestsDialog?.dismiss()


            } else {
                //showToast(getString(R.string.validation_error))
                Toast.makeText(context, context.getString(R.string.validation_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    //edit credit card item
    private fun showDialogEditCreditCard(position: Int, card: CardDetail) {
        val binding = DataBindingUtil.inflate<AssetCreditcardDialogBinding>(LayoutInflater.from(context), R.layout.asset_creditcard_dialog, null, false)
        val mBuilder = AlertDialog.Builder(context)
                .setView(binding.root)
                .setCancelable(true)

        addCreditCardDialog = mBuilder.show()
        addCreditCardDialog?.cancel_bttn?.setOnClickListener() {
            addCreditCardDialog?.dismiss()
        }

        addCreditCardDialog?.spinnerBankName?.adapter = MasterSpinnerAdapter(context, allMasterDropDown?.BankName!!)
        addCreditCardDialog?.spinnerObligate?.adapter = MasterSpinnerAdapter(context, allMasterDropDown?.CreditCardObligation!!)

        selectMasterDropdownValue(binding.spinnerBankName, card.bankNameTypeDetailID)
        selectMasterDropdownValue(binding.spinnerObligate, card.obligateTypeDetail)
        binding.etCreditCardLimit.setText(card.cardLimit.toString())
        binding.etCurrentUtilization.setText(card.currentUtilization.toString())
        binding.etLastPaymentDate.setText(card.lastPaymentDate.toString())
        binding.btnAddcrdetail.setText(R.string.edit)



        addCreditCardDialog?.etLastPaymentDate?.setOnClickListener {
            SelectDate(addCreditCardDialog?.etLastPaymentDate!!, context)
        }

        addCreditCardDialog?.btnAddcrdetail?.setOnClickListener() {

            if (formValidation.validateCardsDialog(binding)) {
                val currentCard = CardDetail()
                val bankName = addCreditCardDialog?.spinnerBankName?.selectedItem as DropdownMaster?
                val obligate = addCreditCardDialog?.spinnerObligate?.selectedItem as DropdownMaster?
                currentCard.lastPaymentDate = addCreditCardDialog?.etLastPaymentDate?.text.toString()
                currentCard.cardLimit = addCreditCardDialog?.etCreditCardLimit!!.text.toString().toInt()
                currentCard.currentUtilization = addCreditCardDialog?.etCurrentUtilization!!.text.toString().toInt()
                currentCard.bankNameTypeDetailID = bankName?.typeDetailID
                currentCard.obligateTypeDetail = obligate?.typeDetailID

                cardDetailList?.set(position, currentCard)
                cardDetailAdapter?.notifyItemChanged(position)
                addCreditCardDialog?.dismiss()


            } else {
                Toast.makeText(context, context.getString(R.string.validation_error), Toast.LENGTH_SHORT).show()
            }


        }
    }

    //edit obligation item
    private fun showDialogToEditObligation(position: Int, obligation: ObligationDetail) {

        val binding = DataBindingUtil.inflate<AddObligationDialogBinding>(LayoutInflater.from(context), R.layout.add_obligation_dialog, null, false)
        val mBuilder = AlertDialog.Builder(context)
                .setView(binding.root)
                .setCancelable(true)

        addObligationDialog = mBuilder.show()
        addObligationDialog?.cancel_bttn?.setOnClickListener() {
            addObligationDialog?.dismiss()

        }

        addObligationDialog?.spinnerObligate?.adapter = MasterSpinnerAdapter(context, allMasterDropDown?.Obligate!!)
        addObligationDialog?.spinnerLoanOwnership?.adapter = MasterSpinnerAdapter(context, allMasterDropDown?.LoanOwnership!!)
        addObligationDialog?.spinnerLoanType?.adapter = MasterSpinnerAdapter(context, allMasterDropDown?.LoanType!!)
        addObligationDialog?.spinnerRepaymentBank?.adapter = MasterSpinnerAdapter(context, allMasterDropDown?.RepaymentBank!!)
        addObligationDialog?.spinnerEmiPaidInSameMonth?.adapter = MasterSpinnerAdapter(context, allMasterDropDown?.BounceEmiPaidInSameMonth!!)

        selectMasterDropdownValue(binding!!.spinnerObligate, obligation.obligateTypeDetailID)
        selectMasterDropdownValue(binding.spinnerLoanOwnership, obligation.loanOwnershipTypeDetailID)
        selectMasterDropdownValue(binding.spinnerLoanType, obligation.loanTypeTypeDetailID)
        selectMasterDropdownValue(binding.spinnerRepaymentBank, obligation.repaymentBankTypeDetailID)
        selectMasterDropdownValue(binding.spinnerEmiPaidInSameMonth, obligation.bounseEmiPaidInSameMonth)

        binding.etLoanAmount.setText(obligation.loanAmount.toString())
        binding.etFinancierName.setText(obligation.financerName.toString())
        binding.etAccountNum.setText(obligation.loanAccountNumber.toString())
        binding.etTenure.setText(obligation.tenure.toString())
        binding.etBalanceTenure.setText(obligation.balanceTenure.toString())
        binding.etLoanAmount.setText(obligation.loanAmount.toString())
        binding.etEmiAmount.setText(obligation.emiAmount.toString())
        binding.etBouncesInLastSixMonths.setText((obligation.numberOfBouncesInLastSixMonth.toString()))
        binding.etBouncesInLastNineMonths.setText((obligation.numberOfBouncesInLastNineMonth.toString()))
        // binding.etDisbursementDate.setText(obligation.)
        binding.btnAddObligation.setText(R.string.edit)


        addObligationDialog?.etDisbursementDate?.setOnClickListener() {
            SelectDate(addObligationDialog?.etDisbursementDate!!, context)
        }

        addObligationDialog?.cancel_bttn?.setOnClickListener() {

            addObligationDialog?.dismiss()
        }
        addObligationDialog?.btnAddObligation?.setOnClickListener() {


            if (formValidation.validateObligationDialog(binding)) {

                val currentObligation = ObligationDetail()
                val loanOwnership = binding.spinnerLoanOwnership.selectedItem as DropdownMaster?
                val obligate = addObligationDialog?.spinnerObligate?.selectedItem as DropdownMaster?
                val loanType = addObligationDialog?.spinnerLoanType?.selectedItem as DropdownMaster?
                val repaymentBank = addObligationDialog?.spinnerRepaymentBank?.selectedItem as DropdownMaster?
                val emiPaidInSameMonth = addObligationDialog?.spinnerEmiPaidInSameMonth?.selectedItem as DropdownMaster?
                currentObligation.numberOfBouncesInLastNineMonth = addObligationDialog?.etBouncesInLastNineMonths?.text.toString().toInt()
                currentObligation.numberOfBouncesInLastSixMonth = addObligationDialog?.etBouncesInLastSixMonths?.text.toString().toInt()
                currentObligation.financerName = addObligationDialog?.etFinancierName?.text.toString()
                currentObligation.loanAmount = CurrencyConversion().convertToNormalValue(addObligationDialog?.etLoanAmount?.text.toString()).toInt()
                currentObligation.emiAmount = CurrencyConversion().convertToNormalValue(addObligationDialog?.etEmiAmount?.text.toString()).toInt()
                currentObligation.loanAccountNumber = addObligationDialog?.etAccountNum?.text.toString()
                currentObligation.tenure = addObligationDialog?.etTenure?.text.toString().toInt()
                currentObligation.balanceTenure = addObligationDialog?.etBalanceTenure?.text.toString().toInt()
                currentObligation.loanOwnershipTypeDetailID = loanOwnership?.typeDetailID
                currentObligation.obligateTypeDetailID = obligate?.typeDetailID
                currentObligation.loanTypeTypeDetailID = loanType?.typeDetailID
                currentObligation.repaymentBankTypeDetailID = repaymentBank?.typeDetailID
                currentObligation.bounseEmiPaidInSameMonth = emiPaidInSameMonth?.typeDetailID

                obligationsList?.set(position, currentObligation)
                obligationAdapter?.notifyItemChanged(position)
                addObligationDialog?.dismiss()


            } else {

                //showToast(context.getString(R.string.validation_error))
                Toast.makeText(context, context.getString(R.string.validation_error), Toast.LENGTH_SHORT).show()
            }
        }

    }

}
