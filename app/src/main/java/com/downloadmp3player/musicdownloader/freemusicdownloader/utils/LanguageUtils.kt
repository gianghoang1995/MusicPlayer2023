package com.downloadmp3player.musicdownloader.freemusicdownloader.utils

import android.content.Context
import android.content.res.Resources
import android.preference.PreferenceManager
import android.util.Log
import com.downloadmp3player.musicdownloader.freemusicdownloader.R
import com.downloadmp3player.musicdownloader.freemusicdownloader.model.LanguageModel
import com.downloadmp3player.musicdownloader.freemusicdownloader.utils.LocaleUtils.PREF_SETTING_LANGUAGE

object LanguageUtils {


    fun checkListKeySupport(key: String): Boolean {
        var list = ArrayList<String>()
        list.add("en")
        list.add("ja")
        list.add("hi")
        list.add("es")
        list.add("fr")
        list.add("ar")
        list.add("bn")
        list.add("ru")
        list.add("it")
        list.add("id")
        list.add("de")
        list.add("ko")

        return (key in list)
    }

    fun listLanguageSupport(context: Context): ArrayList<LanguageModel> {
        var listLanguage: ArrayList<LanguageModel> = ArrayList()

//        listLanguage.add(
//            LanguageModel(
//                R.drawable.ic_language_default,
//                context.getString(R.string.default_1),
//                "",
//                false
//            )
//        )

        listLanguage.add(
            LanguageModel(
                R.drawable.flag_english,
                context.getString(R.string.english),
                "en",
                false
            )
        )

        listLanguage.add(
            LanguageModel(
                R.drawable.frag_japan,
                context.getString(R.string.japanese),
                "ja",
                false
            )
        )
//        listLanguage.add(
//            LanguageModel(
//                R.drawable.flag_vietnam,
//                context.getString(R.string.vietnamese),
//                "vi",
//                false
//            )
//        )
        listLanguage.add(
            LanguageModel(
                R.drawable.flag_india,
                context.getString(R.string.hindi),
                "hi",
                false
            )
        )
        listLanguage.add(
            LanguageModel(
                R.drawable.flag_spain,
                context.getString(R.string.spanish),
                "es",
                false
            )
        )
        listLanguage.add(
            LanguageModel(
                R.drawable.flag_france,
                context.getString(R.string.french),
                "fr",
                false
            )
        )
        listLanguage.add(
            LanguageModel(
                R.drawable.flag_arabic,
                context.getString(R.string.arabic),
                "ar",
                false
            )
        )
        listLanguage.add(
            LanguageModel(
                R.drawable.flag_bangladesh,
                context.getString(R.string.bengali),
                "bn",
                false
            )
        )
        listLanguage.add(
            LanguageModel(
                R.drawable.flag_russia,
                context.getString(R.string.russian),
                "ru",
                false
            )
        )
        listLanguage.add(
            LanguageModel(
                R.drawable.flag_italy,
                context.getString(R.string.italian),
                "it",
                false
            )
        )
        listLanguage.add(
            LanguageModel(
                R.drawable.flag_indonesia,
                context.getString(R.string.indonesia),
                "id",
                false
            )
        )
        listLanguage.add(
            LanguageModel(
                R.drawable.flag_germany,
                context.getString(R.string.german),
                "de",
                false
            )
        )
        listLanguage.add(
            LanguageModel(
                R.drawable.flag_south_korea,
                context.getString(R.string.korean),
                "ko",
                false
            )
        )

        var codeLanguageCurrent = PreferenceUtils.getValueString(PREF_SETTING_LANGUAGE)


        if (codeLanguageCurrent == null) {
            codeLanguageCurrent = Resources.getSystem().configuration.locale.language
        }
        Log.d("TAG", "listLanguageSupport: " + codeLanguageCurrent)


        if (checkListKeySupport(codeLanguageCurrent!!)) {
//            listLanguage.add(
//                0,
//                LanguageModel(
//                    R.drawable.ic_language_default,
//                    context.getString(R.string.default_1),
//                    Resources.getSystem().configuration.locale.language,
//                    false
//                )
//            )
//            var pos = 0

            for (i in listLanguage.indices) {
                if ( listLanguage[i].code == codeLanguageCurrent) {
                    listLanguage[i].isSelected = true
//                    pos = i
                }
            }
//            var item = listLanguage[pos]
//            listLanguage.removeAt(pos)
//            listLanguage.add(0, item)

        } else {
//            listLanguage.add(
//                0,
//                LanguageModel(
//                    R.drawable.ic_language_default,
//                    context.getString(R.string.default_1),
//                    Resources.getSystem().configuration.locale.language,
//                    true
//                )
//            )
            listLanguage.get(0).isSelected = true
        }
        return listLanguage
    }


}