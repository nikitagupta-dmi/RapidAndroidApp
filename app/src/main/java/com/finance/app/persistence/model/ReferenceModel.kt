package com.finance.app.persistence.model

import motobeans.architecture.retrofit.request.Requests

class ReferenceModel {

    var addressBean: Requests.AddressBean? = null
    var applicantReferenceDetailID: Int? = null
    var contactNumber: String? = null
    var name: String? = null
    var knowSince: String? = null
    var address: String? = null
    var serialNumber: Int = 0
    var applicantID: Int? = null
    var relationTypeDetailID: Int? = 0
    var occupationTypeDetailID: Int? = 0
    var active: Boolean? = true
}