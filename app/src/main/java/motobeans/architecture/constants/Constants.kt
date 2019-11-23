package motobeans.architecture.constants

/**
 * Created by munishkumarthakur on 04/11/17.
 */
class Constants {


    private fun Constants(): Unit {}


    object Injection {
        const val API_CURRENT_URL = "currentURL"

        const val API_DEVELOPMENT_URL = "developmentURL"
        const val API_TESTING_URL = "testingURL"
        const val API_LIVE_URL = "liveURL"
        const val API_PRODUCTION_URL = "productionURL"


        /**
         * Network Class v1 constants
         */
        const val RETROFIT_V1 = "RETROFIT_V1"
        const val GSON_V1 = "GSON_V1"
        const val ENDPOINT_V1 = "GSON_V1"
        const val OKHHTP_CACHE_V1 = "OKHTTP_CACHE_V1"
        const val OKHHTP_CLIENT_V1 = "OKHTTP_CLIENT_V1"
        const val INTERCEPTOR_HEADER_V1 = "INTERCEPTOR_HEADER_V1"
        const val INTERCEPTOR_LOGGING_V1 = "INTERCEPTOR_LOGGING_V1"
        const val INTERCEPTOR_RESPONSE_V1 = "INTERCEPTOR_RESPONSE_V1"
    }

    object API {

        object URL {

            //const val URL_DEVELOPMENT = "http://optcrm.info/OptCrmApi/api/"
            const val URL_DEVELOPMENT = "http://13.235.28.32:8080/dmi/"
//            const val URL_DEVELOPMENT = "http://13.232.224.66:8080/dmi/"
            const val URL_TESTING = ""
            const val URL_LIVE = ""
            const val URL_PRODUCTION = ""
        }
    }

    object APP {
        const val TEMP = "temp"
    }
}