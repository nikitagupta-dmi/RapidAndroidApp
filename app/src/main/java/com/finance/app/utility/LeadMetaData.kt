package com.finance.app.utility

import androidx.lifecycle.MutableLiveData
import com.finance.app.eventBusModel.AppEvents
import com.finance.app.persistence.model.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import motobeans.architecture.application.ArchitectureApp
import motobeans.architecture.development.interfaces.DataBaseUtil
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class LeadMetaData : Observable() {

    @Inject
    lateinit var dataBase: DataBaseUtil

    init {
        ArchitectureApp.instance.component.inject(this)
    }

    fun getAndPopulateLeadData(leadId: Int) {
        dataBase.provideDataBaseSource().allLeadsDao().getLead(leadId).observeForever { lead ->
            setLeadData(lead)
        }
    }

    companion object {
        private var leadData = MutableLiveData<AllLeadMaster?>()

        fun setLeadData(leadDetail: AllLeadMaster?) {
            leadData.value = leadDetail
        }

        fun getLeadObservable() = leadData
        fun getLeadData() = leadData.value
        fun getLeadId() = leadData.value?.leadID

        fun addCoApplicant() {
            val leadData = getLeadData()
            leadData?.let {
                LeadAndLoanDetail().addApplicants(leadData)
            }
            setLeadData(leadData)
        }
    }

    private fun initNewApplicantBankDetails(lead: AllLeadMaster, applicants: ArrayList<PersonalApplicantsModel>): ArrayList<BankDetailModel> {
        val applicantBankDetails: ArrayList<BankDetailModel> = ArrayList()
        applicants.forEachIndexed { _, personalApplicantsModel ->
            val bankDetailModel = BankDetailModel()
            bankDetailModel.leadApplicantNumber = lead.leadID.toString()
            bankDetailModel.firstName = personalApplicantsModel.firstName
            bankDetailModel.applicantID = personalApplicantsModel.applicantID
            bankDetailModel.isMainApplicant = personalApplicantsModel.isMainApplicant
            applicantBankDetails.add(bankDetailModel)
        }

        return applicantBankDetails
    }

    private fun initNewApplicantAssetsAndLiabilityDetails(lead: AllLeadMaster, applicants: ArrayList<PersonalApplicantsModel>): ArrayList<AssetLiabilityModel> {
        val assetLiabilityModelList: ArrayList<AssetLiabilityModel> = ArrayList()
        applicants.forEachIndexed { _, personalApplicantsModel ->
            val assetLiabilityModel = AssetLiabilityModel()
            assetLiabilityModel.isMainApplicant = personalApplicantsModel.isMainApplicant
            assetLiabilityModel.leadApplicantNumber = lead.leadID.toString()
            assetLiabilityModel.applicantId = personalApplicantsModel.applicantID
            assetLiabilityModelList.add(assetLiabilityModel)
        }
        return assetLiabilityModelList
    }

    private fun insertLeadInfoIntoDB(lead: AllLeadMaster): Job {
        return GlobalScope.launch {
            lead.isSyncWithServer = false
            dataBase.provideDataBaseSource().allLeadsDao().insertLead(lead)
            AppEvents.fireEventBackgroundSync(AppEvents.BackGroundSyncEvent.LEAD_SYNC)
        }
    }

    fun saveLoanData(data: LoanInfoModel) {
        val lead = getLeadData()
        lead?.let {
            lead.loanData = data
            insertLeadInfoIntoDB(lead)
        }
    }

    fun savePersonalData(applicants: ArrayList<PersonalApplicantsModel>) {
        val lead = getLeadData()
        lead?.let {
            lead.personalData.applicantDetails = applicants

            //TODO check that if this default mapping is required for employment
            lead.bankData.applicantBankDetails = initNewApplicantBankDetails(lead, applicants)
            lead.assetLiabilityData.applicantDetails = initNewApplicantAssetsAndLiabilityDetails(lead, applicants)

            insertLeadInfoIntoDB(lead)
        }
    }

    fun saveEmploymentData(applicants: ArrayList<EmploymentApplicantsModel>) {
        val lead = getLeadData()
        lead?.let {
            lead.employmentData.applicantDetails = applicants
            insertLeadInfoIntoDB(lead)
        }
    }

    fun saveBankData(bankDetailsList: ArrayList<BankDetailModel>) {
        val lead = getLeadData()
        lead?.let {
            lead.bankData.applicantBankDetails = bankDetailsList
            insertLeadInfoIntoDB(lead)
        }
    }

    fun saveReferenceData(referenceDetailsList: ArrayList<ReferenceModel>) {
        val lead = getLeadData()
        lead?.let {
            lead.referenceData.referenceDetails = referenceDetailsList
            insertLeadInfoIntoDB(lead)
        }
    }

    fun saveAssetLiabilityData(assetsAndLiability: ArrayList<AssetLiabilityModel>) {
        val lead: AllLeadMaster? = getLeadData()
        lead?.let {
            lead.assetLiabilityData?.applicantDetails = assetsAndLiability
            insertLeadInfoIntoDB(lead)
        }
    }

    fun savePropertyData(leadPropertyData: PropertyModel) {
        val lead: AllLeadMaster? = getLeadData()
        lead?.let {
            lead.propertyData = leadPropertyData
            insertLeadInfoIntoDB(lead)

        }
    }
}