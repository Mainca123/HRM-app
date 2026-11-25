package com.example.baseproject.data.local

import com.example.baseproject.R
import com.example.baseproject.domain.model.Language
import com.example.baseproject.domain.model.OnBoarding
import javax.inject.Inject

class LocalDataSource @Inject constructor() {
    fun getListLanguage(): List<Language> {
        return listOf(
            Language("en", R.drawable.flag_en, "English", "Tiếng Anh"),
            Language("vi", R.drawable.flag_vn, "Tiếng Việt", "Tiếng Việt"),
            Language("es", R.drawable.flag_span, "Español", "Tiếng Tây Ban Nha"),
            Language("fr", R.drawable.flag_fra, "Français", "Tiếng Pháp"),
            Language("zh", R.drawable.flag_china, "中文", "Tiếng Trung"),
            Language("ja", R.drawable.flag_jp, "日本語", "Tiếng Nhật"),
            Language("ko", R.drawable.flag_korea, "한국어", "Tiếng Hàn"),
            Language("de", R.drawable.flag_ger, "Deutsch", "Tiếng Đức"),
            Language("ru", R.drawable.flag_rus, "Русский", "Tiếng Nga")
        )
    }

    fun getListOnBoarding(): List<OnBoarding> {
        return buildList {
            add(OnBoarding(R.drawable.img_obd1, R.string.title_ob_1, R.string.content_ob_1))
            add(OnBoarding(R.drawable.img_obd2, R.string.title_ob_2, R.string.content_ob_2))
            add(OnBoarding(R.drawable.img_obd3, R.string.title_ob_3, R.string.content_ob_3))
        }
    }
}
