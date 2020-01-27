package com.finance.app.persistence.model

import java.io.Serializable

class EmploymentApplicantsModel:Serializable {
    var addressBean: AddressDetail? = AddressDetail()
    var allEarningMembers = false
    var applicantID: Int? = 0
    var businessSetupTypeDetailID: Int? = 0
    var businessVinatgeInYear: Int? = 0
    var companyName: String? = ""
    var isMainApplicant = false
    var constitutionTypeDetailID: Int? = 0
    var dateOfIncorporation: String? = ""
    var dateOfJoining: String? = ""
    var employeeID: String? = ""
    var employmentTypeDetailID: Int? = 0
    var gstRegistration: String? = ""
    var industryTypeDetailID: Int? = 0
    var loanApplicationID: Int? = 0
    var occupationDetailID: Int? = 0
    var occupationalDetailID: Int? = 0
    var officialMailID: String? = ""
    var leadApplicantNumber: String? = ""
    var incomeConsidered: Boolean = false
    var isPensioner: Boolean = false
    var profileSegmentTypeDetailID: Int? = 0
    var retirementAge: Int = 0
    var sectorTypeDetailID: Int? = 0
    var subProfileTypeDetailID: Int? = 0
    var totalExperience: String? = ""
    var designation: String? = ""
    var incomeDetail: IncomeDetail? = IncomeDetail()
}
