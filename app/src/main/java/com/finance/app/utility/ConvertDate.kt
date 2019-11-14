package com.finance.app.utility

import java.text.SimpleDateFormat
import java.util.*

class ConvertDate {
    fun convertToApiFormat(mDate: String): String {
        val desirablePattern = "yyyy-MM-dd"
        val pattern = "dd-MMM-yyyy"
        val sdf = SimpleDateFormat(pattern)
        val date = sdf.parse(mDate)
        val desiredSdf = SimpleDateFormat(desirablePattern)
        return desiredSdf.format(date)
    }

    fun convertToAppFormat(mDate: String): String {
        val pattern = "yyyy-MM-dd"
        val desirablePattern = "dd-MMM-yyyy"
        val sdf = SimpleDateFormat(pattern)
        val date = sdf.parse(mDate)
        val desiredSdf = SimpleDateFormat(desirablePattern)
        return desiredSdf.format(date)
    }

    fun intFromDateString(date: String): String {
        val myFormat = SimpleDateFormat("dd-MMM-yyyy")
        val selectedDate = myFormat.parse(date)
        val todayDate = Date()
        val difference = todayDate.year - selectedDate.year
        return difference.toString()
    }
}