package com.finance.app.persistence.model

import java.io.Serializable

class AssetLiabilityModel : Serializable {
    var applicantAssetLiabilityList: ArrayList<AssetLiability> = ArrayList()
    var applicantCreditCardDetailList: ArrayList<CardDetail> = ArrayList()
    var applicantExistingObligationList: ArrayList<ObligationDetail> = ArrayList()
    var leadApplicantNumber: String? = ""
    var applicantId: Int = 0
    var isMainApplicant: Boolean = false

}