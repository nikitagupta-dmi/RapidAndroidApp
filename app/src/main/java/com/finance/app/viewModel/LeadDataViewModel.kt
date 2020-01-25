/*
* Copyright (C) 2017 The Munish/BrighterBrain Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.finance.app.viewModel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.finance.app.others.AppEnums
import com.finance.app.persistence.model.*
import com.finance.app.presenter.presenter.Presenter
import com.finance.app.presenter.presenter.ViewGeneric
import com.finance.app.utility.ResponseConversion
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import motobeans.architecture.application.ArchitectureApp
import motobeans.architecture.constants.Constants
import motobeans.architecture.constants.ConstantsApi
import motobeans.architecture.development.interfaces.ApiProject
import motobeans.architecture.development.interfaces.DataBaseUtil
import motobeans.architecture.development.interfaces.SharedPreferencesUtil
import motobeans.architecture.retrofit.response.Response
import javax.inject.Inject

class LeadDataViewModel(private val activity: FragmentActivity) : BaseViewModel(activity) {

    @Inject
    lateinit var sharedPreferences: SharedPreferencesUtil
    @Inject
    lateinit var dataBase: DataBaseUtil
    @Inject
    lateinit var api: ApiProject
    private val viewState: MutableLiveData<ProductViewState> = MutableLiveData()
    private val presenter = Presenter()
    private var loanMaster: LoanInfoMaster = LoanInfoMaster()
    private var personalInfoMaster: PersonalInfoMaster = PersonalInfoMaster()
    private var bankDetailMaster: BankDetailMaster = BankDetailMaster()
    private var employmentMaster = EmploymentMaster()
    private var assetLiabilityMaster = AssetLiabilityMaster()
    private var propertyMaster = PropertyMaster()
    private var referenceMaster = ReferenceMaster()
    private var lead: AllLeadMaster? = null

    init {
        ArchitectureApp.instance.component.inject(this)
        viewState.value = ProductViewState()
    }

    companion object {
        private var counter = 0
    }

    private fun currentViewState(): ProductViewState = viewState.value!!

    data class ProductViewState(var isNoDataFound: Boolean = false,
                                val isLoading: Boolean = false,
                                val isError: Boolean = false,
                                val isEmptyData: Boolean = false,
                                val errorMessage: String? = null)

    private fun isLoading(isLoading: Boolean) {
        viewState.value = currentViewState().copy(isLoading = isLoading)
    }

    private fun isError(isError: Boolean, errorMessage: String?) {
        viewState.value = currentViewState().copy(isError = isError, errorMessage = errorMessage)
    }

    private fun isEmptyList(isEmptyData: Boolean) {
        viewState.value = currentViewState().copy(isEmptyData = isEmptyData)
    }

    fun getLeadData(leadId: String) {
        presenter.callNetwork(ConstantsApi.CALL_LOAN_PRODUCT, dmiConnector = LoanProductsDropdown())
        presenter.callNetwork(ConstantsApi.CALL_ALL_STATES, dmiConnector = AllStatesList())
        presenter.callNetwork(ConstantsApi.CALL_COAPPLICANTS_LIST, dmiConnector = CallCoApplicantList(leadId))
        getLeadFromDB(leadId)

    }

    private fun getLeadFromDB(leadId: String) {
        dataBase.provideDataBaseSource().allLeadsDao().getLead(leadId.toInt()).observe(activity, Observer {
            lead = it
            lead?.let {
                getLoanApplicationData(leadId)
            }
        })
    }

    private fun getLoanApplicationData(leadId: String) {
//        presenter.callNetwork(ConstantsApi.CALL_GET_LOAN_APP, CallGetLoan(leadId, loanMaster.storageType))
//        presenter.callNetwork(ConstantsApi.CALL_GET_LOAN_APP, CallGetLoan(leadId, personalInfoMaster.storageType))
//        presenter.callNetwork(ConstantsApi.CALL_GET_LOAN_APP, CallGetLoan(leadId, bankDetailMaster.storageType))
//        presenter.callNetwork(ConstantsApi.CALL_GET_LOAN_APP, CallGetLoan(leadId, employmentMaster.storageType))
//        presenter.callNetwork(ConstantsApi.CALL_GET_LOAN_APP, CallGetLoan(leadId, assetLiabilityMaster.storageType))
        presenter.callNetwork(ConstantsApi.CALL_GET_LOAN_APP, CallGetLoan(leadId, propertyMaster.storageType))
        presenter.callNetwork(ConstantsApi.CALL_GET_LOAN_APP, CallGetLoan(leadId, referenceMaster.storageType))
    }

    inner class LoanProductsDropdown : ViewGeneric<String?, Response.ResponseLoanProduct>(context = activity) {
        override val apiRequest: String?
            get() = null

        override fun getApiSuccess(value: Response.ResponseLoanProduct) {
            if (value.responseCode == Constants.SUCCESS) {
                GlobalScope.launch {
                    dataBase.provideDataBaseSource().loanProductDao().insertLoanProductList(value.responseObj)
                }
            } else {
                showToast(value.responseMsg)
            }
        }
    }

    inner class AllStatesList : ViewGeneric<String?, Response.ResponseStatesDropdown>(context = activity) {
        override val apiRequest: String?
            get() = null

        override fun getApiSuccess(value: Response.ResponseStatesDropdown) {
            if (value.responseCode == Constants.SUCCESS) {
                GlobalScope.launch {
                    dataBase.provideDataBaseSource().statesDao().insertStates(value.responseObj)
                }
            } else {
                showToast(value.responseMsg)
            }
        }
    }

    inner class CallCoApplicantList(private val leadId: String) : ViewGeneric<String, Response.ResponseCoApplicants>(context = activity) {
        override val apiRequest: String
            get() = leadId

        override fun getApiSuccess(value: Response.ResponseCoApplicants) {
            if (value.responseCode == Constants.SUCCESS) {
                saveApplicantToDB(value.responseObj)
            }
        }

        private fun saveApplicantToDB(responseObj: ArrayList<CoApplicantsList>?) {
            GlobalScope.launch {
                val coApplicantMaster = CoApplicantsMaster()
                coApplicantMaster.coApplicantsList = responseObj
                coApplicantMaster.leadID = leadId.toInt()
                dataBase.provideDataBaseSource().coApplicantsDao().insertCoApplicants(coApplicantMaster)
            }
        }
    }

    inner class CallGetLoan(private val leadId: String, private val form: String) : ViewGeneric<ArrayList<String>?,
            Response.ResponseGetLoanApplication>(context = activity) {
        override val apiRequest: ArrayList<String>?
            get() = arrayListOf(leadId, form)

        override fun getApiSuccess(value: Response.ResponseGetLoanApplication) {
            if (value.responseCode == Constants.SUCCESS) {
                value.responseObj?.let {
                    saveDataToLead(value.responseObj)
                    counter++
                }
                if (counter >= 4) {
                    saveLead()
                }
            }
        }

        private fun saveDataToLead(responseObj: Response.LoanApplicationGetObj) {
            when (form) {
                AppEnums.FormType.LOANINFO.type -> saveLoanData(responseObj)
                AppEnums.FormType.PERSONALINFO.type -> savePersonalData(responseObj)
                AppEnums.FormType.EMPLOYMENT.type -> saveEmploymentData(responseObj)
                AppEnums.FormType.BANKDETAIL.type -> saveBankData(responseObj)
                AppEnums.FormType.LIABILITYASSET.type -> saveAssetLiabilityData(responseObj)
                AppEnums.FormType.PROPERTY.type -> savePropertyData(responseObj)
                AppEnums.FormType.REFERENCE.type -> saveReferenceData(responseObj)
            }
        }

        private fun saveLoanData(responseObj: Response.LoanApplicationGetObj) {
            loanMaster = ResponseConversion().toLoanMaster(responseObj)
            lead!!.loanData = loanMaster.draftData
        }

        private fun savePersonalData(responseObj: Response.LoanApplicationGetObj) {
            personalInfoMaster = ResponseConversion().toPersonalMaster(responseObj)
            lead!!.personalData = personalInfoMaster.draftData.applicantDetails
        }

        private fun saveEmploymentData(responseObj: Response.LoanApplicationGetObj) {
            employmentMaster = ResponseConversion().toEmploymentMaster(responseObj)
            lead!!.employmentData = employmentMaster.draftData.applicantDetails
        }

        private fun saveBankData(responseObj: Response.LoanApplicationGetObj) {
            bankDetailMaster = ResponseConversion().toBankDetailMaster(responseObj)
            lead!!.bankData = bankDetailMaster.draftData?.applicantDetails
        }

        private fun saveAssetLiabilityData(responseObj: Response.LoanApplicationGetObj) {
            assetLiabilityMaster = ResponseConversion().toAssetLiabilityMaster(responseObj)
            lead!!.assetLiabilityData = assetLiabilityMaster.draftData?.applicantDetails
        }

        private fun savePropertyData(responseObj: Response.LoanApplicationGetObj) {
            propertyMaster = ResponseConversion().toPropertyMaster(responseObj)
            lead!!.propertyData = propertyMaster.draftData
        }

        private fun saveReferenceData(responseObj: Response.LoanApplicationGetObj) {
            referenceMaster = ResponseConversion().toReferenceMaster(responseObj)
            lead!!.referenceData = referenceMaster.draftData.referenceDetails
        }

        private fun saveLead() {
            GlobalScope.launch {
                dataBase.provideDataBaseSource().allLeadsDao().insertLead(lead!!)
            }
        }
    }
}