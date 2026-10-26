package YOUR.PACKAGE.core.platform.firebase.fcm.topic

import android.content.Context
import android.telephony.TelephonyManager

/**
 * Template — copy to `:core-platform` …/core/platform/firebase/fcm/topic/.
 * Replace YOUR.PACKAGE with applicationId root.
 *
 * QiblaCompass `FcmTopics` shape. Every name passes [sanitize] so it matches the FCM topic
 * regex `[a-zA-Z0-9-_.~%]{1,900}` (the reference lists contain spaces / accents FCM rejects).
 *
 * Language names must stay in sync with the app catalog (`LanguageDataSource` / `DpLanguages`,
 * rules/reference/languages.md) — the English `name` column is the topic.
 */
object FcmTopics {

    private const val UNKNOWN = "Unknown"
    private val invalidTopicChars = Regex("[^a-zA-Z0-9\\-_.~%]")

    fun getTopic(tokenType: TokenType): String = sanitize(
        when (tokenType) {
            is TokenType.AllUsers -> tokenType.context.packageName
            TokenType.PremiumUsers -> "premium_users"
            TokenType.NonPremiumUsers -> "non_premium_users"
            is TokenType.Language -> getLanguageName(tokenType.languageCode)
            is TokenType.Country -> getCountryName(countryIso(tokenType.context))
        }
    )

    /** Replaces every char outside the FCM topic alphabet with `_` and trims to 900. */
    fun sanitize(raw: String): String =
        raw.trim().replace(invalidTopicChars, "_").take(900).ifEmpty { UNKNOWN }

    private fun countryIso(context: Context): String {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        return telephonyManager?.simCountryIso?.takeIf { it.isNotBlank() } ?: "XXX"
    }

    private fun getLanguageName(languageCode: String): String =
        languages.firstOrNull { it.first.equals(languageCode, ignoreCase = true) }?.second ?: "English"

    private fun getCountryName(countryIso: String): String =
        countries.firstOrNull { it.first.equals(countryIso, ignoreCase = true) }?.second ?: UNKNOWN

    // Catalog language code → English name (rules/reference/languages.md). Keep in sync with LanguageDataSource.
    private val languages = listOf(
        "en" to "English", "ur" to "Urdu", "in" to "Indonesian", "hi" to "Hindi", "bn" to "Bengali",
        "ar" to "Arabic", "fa" to "Persian", "tr" to "Turkish", "am" to "Amharic", "fr" to "French",
        "ms" to "Malay", "uz" to "Uzbek", "kk" to "Kazakh", "ky" to "Kyrgyz", "tg" to "Tajik",
        "ru" to "Russian", "de" to "German", "sv" to "Swedish", "th" to "Thai", "tl" to "Filipino",
        "ja" to "Japanese", "es" to "Spanish", "ko" to "Korean", "sw" to "Swahili", "it" to "Italian",
        "ps" to "Pashto", "pa" to "Punjabi", "ta" to "Tamil", "te" to "Telugu", "mr" to "Marathi",
        "ml" to "Malayalam", "ha" to "Hausa", "so" to "Somali", "pt" to "Portuguese", "nl" to "Dutch",
        "pl" to "Polish", "ro" to "Romanian", "no" to "Norwegian", "fi" to "Finnish", "da" to "Danish",
        "cs" to "Czech", "sk" to "Slovak", "sl" to "Slovenian", "hr" to "Croatian", "hu" to "Hungarian",
        "el" to "Greek", "az" to "Azerbaijani", "ku" to "Kurdish", "vi" to "Vietnamese", "kn" to "Kannada",
        "zh-CN" to "Chinese-Simplified", "zh-TW" to "Chinese-Traditional", "af" to "Afrikaans",
        "be" to "Belarusian", "bg" to "Bulgarian", "bs" to "Bosnian", "ca" to "Catalan", "ceb" to "Cebuano",
        "co" to "Corsican", "eo" to "Esperanto", "et" to "Estonian", "eu" to "Basque", "fy" to "Frisian",
        "ga" to "Irish", "gd" to "Scottish_Gaelic", "gl" to "Galician", "gu" to "Gujarati", "haw" to "Hawaiian",
        "hmn" to "Hmong", "ht" to "Haitian_Creole", "hy" to "Armenian", "ig" to "Igbo", "is" to "Icelandic",
        "iw" to "Hebrew", "jv" to "Javanese", "ka" to "Georgian", "km" to "Khmer", "la" to "Latin",
        "lb" to "Luxembourgish", "lo" to "Lao", "lt" to "Lithuanian", "lv" to "Latvian", "mg" to "Malagasy",
        "mi" to "Maori", "mk" to "Macedonian", "mn" to "Mongolian", "mt" to "Maltese", "my" to "Myanmar",
        "ne" to "Nepali", "ny" to "Chichewa", "sd" to "Sindhi", "si" to "Sinhala", "sm" to "Samoan",
        "sn" to "Shona", "sq" to "Albanian", "st" to "Sesotho", "su" to "Sundanese", "sr" to "Serbian",
        "xh" to "Xhosa", "cy" to "Welsh", "uk" to "Ukrainian", "yo" to "Yoruba", "zu" to "Zulu",
    )

    // SIM country ISO → topic name (QiblaCompass list, spaces / accents replaced).
    private val countries = listOf(
        "id" to "Indonesia", "pk" to "Pakistan", "in" to "India", "bd" to "Bangladesh", "ng" to "Nigeria",
        "eg" to "Egypt", "ir" to "Iran", "tr" to "Turkey", "dz" to "Algeria", "sd" to "Sudan",
        "ss" to "South_Sudan", "iq" to "Iraq", "af" to "Afghanistan", "ma" to "Morocco", "sa" to "Saudi_Arabia",
        "et" to "Ethiopia", "uz" to "Uzbekistan", "ye" to "Yemen", "cn" to "China", "ne" to "Niger",
        "ml" to "Mali", "my" to "Malaysia", "sy" to "Syria", "sn" to "Senegal", "tn" to "Tunisia",
        "kz" to "Kazakhstan", "az" to "Azerbaijan", "ae" to "United_Arab_Emirates", "gb" to "United_Kingdom",
        "us" to "United_States", "de" to "Germany", "fr" to "France", "sg" to "Singapore", "th" to "Thailand",
        "ph" to "Philippines", "jp" to "Japan", "za" to "South_Africa", "it" to "Italy", "ru" to "Russia",
        "es" to "Spain", "ca" to "Canada", "kr" to "South_Korea", "au" to "Australia", "ci" to "Cote_dIvoire",
        "so" to "Somalia", "tj" to "Tajikistan", "tz" to "Tanzania", "om" to "Oman", "bh" to "Bahrain",
        "kw" to "Kuwait", "qa" to "Qatar", "br" to "Brazil", "vn" to "Vietnam", "ar" to "Argentina",
        "ua" to "Ukraine", "mx" to "Mexico", "pe" to "Peru", "co" to "Colombia", "cm" to "Cameroon",
        "gh" to "Ghana", "bj" to "Benin", "bi" to "Burundi", "rw" to "Rwanda", "cd" to "Congo",
        "cz" to "Czechia", "ro" to "Romania", "gr" to "Greece", "hu" to "Hungary", "be" to "Belgium",
        "se" to "Sweden", "ch" to "Switzerland", "bg" to "Bulgaria", "ba" to "Bosnia_Herzegovina",
        "hr" to "Croatia", "ge" to "Georgia", "md" to "Moldova", "al" to "Albania", "am" to "Armenia",
        "mk" to "North_Macedonia", "lt" to "Lithuania", "lv" to "Latvia", "ie" to "Ireland", "no" to "Norway",
        "fi" to "Finland", "pt" to "Portugal", "sk" to "Slovakia", "si" to "Slovenia", "mt" to "Malta",
        "lu" to "Luxembourg", "mc" to "Monaco", "va" to "Vatican_City", "ad" to "Andorra",
        "ag" to "Antigua_Barbuda", "as" to "American_Samoa", "aw" to "Aruba", "bb" to "Barbados",
        "bn" to "Brunei_Darussalam", "bq" to "Bonaire", "bl" to "Saint_Barthelemy", "bm" to "Bermuda",
        "bv" to "Bouvet_Island", "bs" to "Bahamas", "cc" to "Cocos_Islands", "cf" to "Central_African_Republic",
        "cg" to "Congo", "ck" to "Cook_Islands", "cw" to "Curacao", "cy" to "Cyprus", "dm" to "Dominica",
        "do" to "Dominican_Republic", "ec" to "Ecuador", "er" to "Eritrea", "fj" to "Fiji", "fm" to "Micronesia",
        "fo" to "Faroe_Islands", "ga" to "Gabon", "gd" to "Grenada", "gi" to "Gibraltar", "gl" to "Greenland",
        "gm" to "Gambia", "gn" to "Guinea", "gp" to "Guadeloupe", "gq" to "Equatorial_Guinea", "gu" to "Guam",
        "gw" to "Guinea_Bissau", "gy" to "Guyana", "hk" to "Hong_Kong", "hm" to "Heard_Island", "hn" to "Honduras",
        "ht" to "Haiti", "il" to "Israel", "im" to "Isle_Man", "is" to "Iceland", "je" to "Jersey",
        "jm" to "Jamaica", "jo" to "Jordan", "ke" to "Kenya", "kg" to "Kyrgyzstan", "kh" to "Cambodia",
        "ki" to "Kiribati", "km" to "Comoros", "kn" to "Saint_Kitts", "kp" to "Korea", "la" to "Lao_People",
        "lb" to "Lebanon", "lc" to "Saint_Lucia", "li" to "Liechtenstein", "lk" to "Sri_Lanka", "lr" to "Liberia",
        "ls" to "Lesotho", "mu" to "Mauritius", "mv" to "Maldives", "mw" to "Malawi", "nc" to "New_Caledonia",
        "ni" to "Nicaragua", "nz" to "New_Zealand", "pa" to "Panama", "pg" to "Papua_New_Guinea", "pn" to "Pitcairn",
        "pr" to "Puerto_Rico", "pw" to "Palau", "py" to "Paraguay", "re" to "Reunion", "sb" to "Solomon_Islands",
        "sc" to "Seychelles", "sh" to "Saint_Helena", "tc" to "Turks_Islands", "td" to "Chad",
        "tf" to "French_Territories", "tg" to "Togo", "tk" to "Tokelau", "tl" to "Timor_Leste", "tm" to "Turkmenistan",
        "to" to "Tonga", "tt" to "Trinidad_Tobago", "tv" to "Tuvalu", "vc" to "Saint_Vincent", "vg" to "Virgin_Islands",
        "vi" to "Virgin_Islands_US", "vu" to "Vanuatu", "wf" to "Wallis_Futuna", "ws" to "Samoa", "zm" to "Zambia",
        "zw" to "Zimbabwe",
    )
}
