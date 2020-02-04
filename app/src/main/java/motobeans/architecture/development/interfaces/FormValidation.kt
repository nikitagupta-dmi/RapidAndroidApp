package motobeans.architecture.development.interfaces

import com.finance.app.databinding.*
import com.finance.app.persistence.model.DropdownMaster
import com.finance.app.persistence.model.LoanProductMaster
import com.finance.app.persistence.model.LoanPurpose
import com.finance.app.view.customViews.CustomChannelPartnerView
import com.finance.app.view.customViews.CustomSpinnerViewTest

interface FormValidation {
    fun validateTemp(binding: TempActivityBinding): Boolean
    fun validateLogin(binding: ActivityLoginBinding): Boolean
    fun validatePersonalInfo(binding: LayoutCustomViewPersonalBinding, spinnerDMList: ArrayList<CustomSpinnerViewTest<DropdownMaster>>): Boolean
    fun validateLoanInformation(binding: FragmentLoanInformationBinding,
                                loanProduct: CustomSpinnerViewTest<LoanProductMaster>,
                                loanPurpose: CustomSpinnerViewTest<LoanPurpose>,
                                spinnerDMList: ArrayList<CustomSpinnerViewTest<DropdownMaster>>,
                                customChannelPartnerView: CustomChannelPartnerView): Boolean

    fun validateSalaryEmployment(salaryBinding: LayoutSalaryBinding, salarySpinnerList: ArrayList<CustomSpinnerViewTest<DropdownMaster>>): Boolean
    fun validateSenpEmployment(senpBinding: LayoutSenpBinding, senpSpinnerList: ArrayList<CustomSpinnerViewTest<DropdownMaster>>): Boolean
    fun validatePersonalInfo(binding: LayoutCustomViewPersonalBinding): Boolean
    fun validateAddLead(binding: ActivityLeadCreateBinding): Boolean
    fun validateLoanInformation(binding: FragmentLoanInformationBinding, loanProduct: LoanProductMaster?): Boolean
    fun validateSalaryEmployment(salaryBinding: LayoutSalaryBinding): Boolean
    fun validateSenpEmployment(senpBinding: LayoutSenpBinding): Boolean
    fun validateBankDetail(binding: FragmentBankDetailBinding): Boolean
    fun validateBankDetail(binding: DialogBankDetailFormBinding): Boolean
    fun validateReference(binding: FragmentReferenceBinding): Boolean
    fun validateReference(binding: DialogReferenceDetailsBinding): Boolean
    fun validateProperty(binding: FragmentPropertyInfoBinding): Boolean
    fun validateAssets(binding: FragmentAssetLiablityBinding): Boolean
    fun validateCards(binding: LayoutCreditCardDetailsBinding): Boolean
    fun validateObligations(binding: LayoutObligationBinding): Boolean
    fun validateObligationDialog(binding: AddObligationDialogBinding):Boolean
    fun validateAssetLiabilityForm(binding: FragmentAssetLiablityBinding): Boolean
    fun disableAssetLiabilityFields(binding: FragmentAssetLiablityBinding)
    fun validateCardsDialog(binding: AssetCreditcardDialogBinding):Boolean
    fun validateAssetsDialog(binding: AddAssestsDialogBinding):Boolean
    fun validateAssetLiabilityInfo(binding: LayoutCustomviewAssetliabilityBinding):Boolean



}
