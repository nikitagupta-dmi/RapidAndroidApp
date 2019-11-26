package com.finance.app.view.fragment

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import androidx.lifecycle.Observer
import com.finance.app.R
import com.finance.app.databinding.FragmentPropertyBinding
import com.finance.app.others.AppEnums
import com.finance.app.persistence.model.*
import com.finance.app.presenter.connector.DistrictCityConnector
import com.finance.app.presenter.connector.LoanApplicationConnector
import com.finance.app.presenter.connector.PinCodeDetailConnector
import com.finance.app.presenter.presenter.*
import com.finance.app.utility.RequestConversion
import com.finance.app.utility.ResponseConversion
import com.finance.app.utility.SetPropertyMandatoryField
import com.finance.app.view.adapters.recycler.Spinner.CitySpinnerAdapter
import com.finance.app.view.adapters.recycler.Spinner.DistrictSpinnerAdapter
import com.finance.app.view.adapters.recycler.Spinner.MasterSpinnerAdapter
import com.finance.app.view.adapters.recycler.Spinner.StatesSpinnerAdapter
import com.google.android.material.textfield.TextInputEditText
import fr.ganfra.materialspinner.MaterialSpinner
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import motobeans.architecture.application.ArchitectureApp
import motobeans.architecture.constants.ConstantsApi
import motobeans.architecture.customAppComponents.activity.BaseFragment
import motobeans.architecture.development.interfaces.DataBaseUtil
import motobeans.architecture.development.interfaces.FormValidation
import motobeans.architecture.development.interfaces.SharedPreferencesUtil
import motobeans.architecture.retrofit.response.Response
import javax.inject.Inject

class PropertyFragment : BaseFragment(), LoanApplicationConnector.PostLoanApp,
        LoanApplicationConnector.GetLoanApp, PinCodeDetailConnector.PinCode,
        DistrictCityConnector.District, DistrictCityConnector.City {

    @Inject
    lateinit var formValidation: FormValidation
    @Inject
    lateinit var dataBase: DataBaseUtil
    @Inject
    lateinit var sharedPreferences: SharedPreferencesUtil
    private lateinit var binding: FragmentPropertyBinding
    private lateinit var mContext: Context
    private val responseConversion = ResponseConversion()
    private val requestConversion = RequestConversion()
    private lateinit var allMasterDropDown: AllMasterDropDown
    private lateinit var states: List<StatesMaster>
    private val loanAppGetPresenter = LoanAppGetPresenter(this)
    private val loanAppPostPresenter = LoanAppPostPresenter(this)
    private val pinCodePresenter = PinCodeDetailPresenter(this)
    private val districtPresenter = DistrictPresenter(this)
    private val cityPresenter = CityPresenter(this)
    private var propertyMaster: PropertyMaster = PropertyMaster()
    private var propertyModel: PropertyModel? = PropertyModel()
    private var pinCodeObj: Response.PinCodeObj? = null
    private var mPinCode: String = ""
    private var mStateId: String = ""
    private var mDistrictId: String = ""
    private var mLead: AllLeadMaster? = null
    private var empId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = initBinding(inflater, container, R.layout.fragment_property)
        init()
        return binding.root
    }

    override fun init() {
        ArchitectureApp.instance.component.inject(this)
        mContext = context!!
        getPropertyInfo()
        SetPropertyMandatoryField(binding)
        setClickListeners()
    }

    private fun getPropertyInfo() {
        mLead = sharedPreferences.getLeadDetail()
        empId = sharedPreferences.getLoginData()!!.responseObj.userDetails.userBasicDetails.tablePrimaryID.toString()
        loanAppGetPresenter.callNetwork(ConstantsApi.CALL_GET_LOAN_APP)
    }

    override val leadId: String
        get() = mLead!!.leadID.toString()

    override val storageType: String
        get() = propertyMaster.storageType

    override fun getLoanAppGetFailure(msg: String) = getDataFromDB()

    private fun getDataFromDB() {
        dataBase.provideDataBaseSource().propertyDao().getProperty(leadId).observe(this, Observer { propertyInfo ->
            propertyInfo?.let {
                propertyMaster = propertyInfo
                propertyModel = propertyMaster.draftData
            }
            showData(propertyModel)
        })
    }

    override fun getLoanAppGetSuccess(value: Response.ResponseGetLoanApplication) {
        value.responseObj?.let {
            propertyMaster = responseConversion.toPropertyMaster(value.responseObj)
            propertyModel = propertyMaster.draftData
        }
        showData(propertyModel)
    }

    private fun showData(property: PropertyModel?) {
        getDropDownsFromDB()
        fillFormWithPropertyData(property!!)
    }

    private fun fillFormWithPropertyData(property: PropertyModel) {
        binding.cbIsFirstProperty.isChecked = property.isFirstProperty
        binding.etDistanceFromBranch.setText(property.distanceFromBranch)
        binding.etDistanceFromResidence.setText(property.distanceFromExistingResidence)
        binding.etPropertyArea.setText(property.propertyAreaSquareFt.toString())
        binding.etPropertyAddress.setText(property.propertyAddress)
        binding.etLandmark.setText(property.landmark)
        binding.etPinCode.setText(property.pinCode)
    }

    private fun getDropDownsFromDB() {
        dataBase.provideDataBaseSource().allMasterDropDownDao().getMasterDropdownValue().observe(viewLifecycleOwner, Observer { masterDrownDownValues ->
            masterDrownDownValues.let {
                allMasterDropDown = it
                setMasterDropDown(allMasterDropDown)
            }
        })

        dataBase.provideDataBaseSource().statesDao().getAllStates().observe(viewLifecycleOwner, Observer {
            states = it
            setStateDropDown(states)
        })
    }

    private fun setMasterDropDown(allMasterDropDown: AllMasterDropDown) {
        binding.spinnerUnitType.adapter = MasterSpinnerAdapter(mContext, allMasterDropDown.PropertyUnitType!!)
        binding.spinnerOwnership.adapter = MasterSpinnerAdapter(mContext, allMasterDropDown.Ownership!!)
        binding.spinnerOwnedProperty.adapter = MasterSpinnerAdapter(mContext, allMasterDropDown.AlreadyOwnedProperty!!)
        selectMasterDropdownValue(binding.spinnerOwnedProperty, propertyModel!!.alreadyOwnedPropertyTypeDetailID)
        selectMasterDropdownValue(binding.spinnerOwnership, propertyModel!!.ownershipTypeDetailID)
        selectMasterDropdownValue(binding.spinnerUnitType, propertyModel!!.unitTypeTypeDetailID)
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

    private fun setStateDropDown(states: List<StatesMaster>) {
        binding.spinnerState.adapter = StatesSpinnerAdapter(mContext, states)
        binding.spinnerCity.adapter = CitySpinnerAdapter(mContext, ArrayList())
        binding.spinnerDistrict.adapter = DistrictSpinnerAdapter(mContext, ArrayList())
        binding.spinnerState?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position >= 0) {
                    val state = parent.selectedItem as StatesMaster
                    mStateId = state.stateID.toString()
                    districtPresenter.callDistrictApi()
                }
            }
        }
    }

    private fun setClickListeners() {
        binding.btnSaveAndContinue.setOnClickListener {
            if (formValidation.validateProperty(binding)) {
                loanAppPostPresenter.callNetwork(ConstantsApi.CALL_POST_LOAN_APP)
            }
        }
        pinCodeListener(binding.etPinCode)
    }

    private fun pinCodeListener(pinCodeField: TextInputEditText?) {
        pinCodeField!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (pinCodeField.text!!.length == 6) {
                    mPinCode = pinCodeField.text.toString()
                    pinCodePresenter.callPinCodeDetailApi()
                }
            }
        })
    }

    override val pinCode: String
        get() = mPinCode

    override fun getPinCodeFailure(msg: String) = clearPinCodeData()

    override fun getPinCodeSuccess(value: Response.ResponsePinCodeDetail, addressType: AppEnums.ADDRESS_TYPE?) {
        if (value.responseObj!!.size > 0) {
            pinCodeObj = value.responseObj[0]
            selectStateValue(binding.spinnerState)
        } else {
            clearPinCodeData()
        }
    }

    private fun selectStateValue(spinner: Spinner) {
        for (index in 0 until spinner.count - 1) {
            val obj = spinner.getItemAtPosition(index) as StatesMaster
            if (obj.stateID == pinCodeObj!!.stateID) {
                spinner.setSelection(index + 1)
                mStateId = pinCodeObj!!.stateID.toString()
                districtPresenter.callDistrictApi()
                spinner.isEnabled = false
                return
            }
        }
    }

    override val stateId: String
        get() = mStateId

    override fun getDistrictFailure(msg: String) = showToast(msg)

    override fun getDistrictSuccess(value: Response.ResponseDistrict, addressType: AppEnums.ADDRESS_TYPE?) {
        if (value.responseObj != null && value.responseObj.size > 0) {
            setDistrict(binding.spinnerDistrict, value)
        }
    }

    private fun setDistrict(spinner: MaterialSpinner, response: Response.ResponseDistrict) {
        spinner.adapter = DistrictSpinnerAdapter(mContext, response.responseObj!!)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position >= 0) {
                    val district = parent.selectedItem as Response.DistrictObj
                    mDistrictId = district.districtID.toString()
                    cityPresenter.callCityApi()
                }
            }
        }
        selectDistrictValue(spinner)
    }

    private fun selectDistrictValue(spinner: Spinner) {
        for (index in 0 until spinner.count - 1) {
            val obj = spinner.getItemAtPosition(index) as Response.DistrictObj
            if (obj.districtID == pinCodeObj!!.districtID) {
                spinner.setSelection(index + 1)
                mDistrictId = obj.districtID.toString()
                cityPresenter.callCityApi()
                spinner.isEnabled = false
                return
            }
        }
    }

    override val districtId: String
        get() = mDistrictId

    override fun getCityFailure(msg: String) = showToast(msg)

    override fun getCitySuccess(value: Response.ResponseCity, addressType: AppEnums.ADDRESS_TYPE?) {
        if (value.responseObj != null && value.responseObj.size > 0) {
            setCityValue(binding.spinnerCity, value.responseObj)
        }
    }

    private fun setCityValue(spinner: MaterialSpinner, responseObj: ArrayList<Response.CityObj>) {
        spinner.adapter = CitySpinnerAdapter(mContext, responseObj)
        for (index in 0 until spinner.count - 1) {
            val obj = spinner.getItemAtPosition(index) as Response.CityObj
            if (obj.cityID == pinCodeObj!!.cityID) {
                spinner.setSelection(index + 1)
                spinner.isEnabled = false
                return
            }
        }
    }

    private fun clearPinCodeData() {
        binding.spinnerState.isEnabled = true
        binding.spinnerDistrict.isEnabled = true
        binding.spinnerCity.isEnabled = true
        binding.spinnerState.adapter = StatesSpinnerAdapter(mContext, states)
        binding.spinnerDistrict.adapter = DistrictSpinnerAdapter(mContext, ArrayList())
        binding.spinnerCity.adapter = CitySpinnerAdapter(mContext, ArrayList())
    }

    private fun getPropertyModel(): PropertyModel {
        val propertyModel = PropertyModel()
        val cCity = binding.spinnerCity.selectedItem as Response.CityObj?
        val cState = binding.spinnerState.selectedItem as StatesMaster?
        val cDistrict = binding.spinnerDistrict.selectedItem as Response.DistrictObj?
        val unitType = binding.spinnerUnitType.selectedItem as DropdownMaster?
        val ownership = binding.spinnerOwnership.selectedItem as DropdownMaster?
        val ownedProperty = binding.spinnerOwnedProperty.selectedItem as DropdownMaster?

        propertyModel.leadID = leadId.toInt()
        propertyModel.cityID = cCity?.cityID
        propertyModel.districtID = cDistrict?.districtID
        propertyModel.stateID = cState?.stateID
        propertyModel.unitTypeTypeDetailID = unitType?.typeDetailID
        propertyModel.alreadyOwnedPropertyTypeDetailID = ownedProperty?.typeDetailID
        propertyModel.ownershipTypeDetailID = ownership?.typeDetailID
        propertyModel.propertyAreaSquareFt = binding.etPropertyArea.text.toString().toInt()
        propertyModel.propertyAddress = binding.etPropertyArea.text.toString()
        propertyModel.landmark = binding.etLandmark.text.toString()
        propertyModel.pinCode = binding.etPinCode.text.toString()
        propertyModel.distanceFromBranch = binding.etDistanceFromBranch.text.toString()
        propertyModel.isFirstProperty = binding.cbIsFirstProperty.isChecked
        propertyModel.distanceFromExistingResidence = binding.etDistanceFromResidence.text.toString()
        return propertyModel
    }

    private fun getPropertyMaster(): PropertyMaster {
        propertyMaster.draftData = getPropertyModel()
        propertyMaster.leadID = leadId.toInt()
        return propertyMaster
    }

    override val loanAppRequestPost: LoanApplicationRequest
        get() = requestConversion.propertyRequest(getPropertyMaster())

    override fun getLoanAppPostFailure(msg: String) {
        saveDataToDB(getPropertyMaster())
        showToast(msg)
    }

    override fun getLoanAppPostSuccess(value: Response.ResponseGetLoanApplication) {
        saveDataToDB(getPropertyMaster())
        gotoNextFragment()
    }

    private fun gotoNextFragment() {
        val ft = fragmentManager?.beginTransaction()
        ft?.replace(R.id.secondaryFragmentContainer, DocumentCheckListFragment())
        ft?.addToBackStack(null)
        ft?.commit()
    }

    private fun saveDataToDB(property: PropertyMaster) {
        GlobalScope.launch {
            dataBase.provideDataBaseSource().propertyDao().insertProperty(property)
        }
    }

}