package com.taner.taskly.core.utils

import android.content.SharedPreferences

class AchievementUtils {


    data class Achievement(val name: String, val description: String, val condition: (UserProgress) -> Boolean)

    data class UserProgress(
        val totalMarkedDays: Int,
        val maxStreak: Int,
        val currentStreak: Int,
        val weeklyStats: List<Int>,
        val morningMarks: Int,
        val beforeBreakfastMarks: Int,
        val workMarks: Int,
        val marksWithin5Min: Int,
        val marksWithin10Min: Int,
        val marksBeforeNoon: Int,
        val marksBeforeMidnight: Int,
        val nightMarksStreak: Int,
        val hourCompletedCounts: List<Pair<Int, Int>>,
    )

    val achievements = mutableListOf(
        // Temel başarımlar
        Achievement("İlk Gün|Temel başarımlar", "İlk kez alışkanlığını işaretle.") { it.totalMarkedDays >= 1 },
        Achievement("2 Gün|Temel başarımlar", "2 gün boyunca alışkanlığını sürdür.") { it.totalMarkedDays >= 2 },
        Achievement("3 Gün|Temel başarımlar", "3 gün işaretleme.") { it.totalMarkedDays >= 3 },
        Achievement("İlk Hafta|Temel başarımlar", "7 gün boyunca işaretleme.") { it.totalMarkedDays >= 7 },
        Achievement("İlk Ay|Temel başarımlar", "30 gün boyunca işaretleme.") { it.totalMarkedDays >= 30 },
        Achievement("50 Gün|Temel başarımlar", "Toplamda 50 gün boyunca işaretle.") { it.totalMarkedDays >= 50 },
        Achievement("100 Gün|Temel başarımlar", "Toplam 100 gün işaretleme.") { it.totalMarkedDays >= 100 },
        Achievement("200 Gün|Temel başarımlar", "200 gün boyunca alışkanlığı sürdür.") { it.totalMarkedDays >= 200 },
        Achievement("365 Gün|Temel başarımlar", "1 yıl boyunca alışkanlık oluştur.") { it.totalMarkedDays >= 365 },

        // Seri başarımlar
        Achievement("3 Gün Seri|Seri başarımlar", "3 gün art arda işaretle.") { it.maxStreak >= 3 },
        Achievement("5 Gün Seri|Seri başarımlar", "5 gün art arda işaretle.") { it.maxStreak >= 5 },
        Achievement("7 Gün Seri|Seri başarımlar", "7 gün art arda işaretle.") { it.maxStreak >= 7 },
        Achievement("10 Gün Seri|Seri başarımlar", "10 gün art arda işaretle.") { it.maxStreak >= 10 },
        Achievement("14 Gün Seri|Seri başarımlar", "14 gün art arda işaretle.") { it.maxStreak >= 14 },
        Achievement("21 Gün Seri|Seri başarımlar", "21 gün art arda işaretle.") { it.maxStreak >= 21 },
        Achievement("30 Gün Seri|Seri başarımlar", "30 gün art arda işaretle.") { it.maxStreak >= 30 },
        Achievement("50 Gün Seri|Seri başarımlar", "50 gün art arda işaretle.") { it.maxStreak >= 50 },
        Achievement("60 Gün Seri|Seri başarımlar", "60 gün art arda işaretle.") { it.maxStreak >= 60 },
        Achievement("90 Gün Seri|Seri başarımlar", "90 gün art arda işaretle.") { it.maxStreak >= 90 },
        Achievement("180 Gün Seri|Seri başarımlar", "180 gün art arda işaretle.") { it.maxStreak >= 180 },
        Achievement("365 Gün Seri|Seri başarımlar", "365 gün art arda işaretle.") { it.maxStreak >= 365 },

        // Haftalık başarımlar
        Achievement("1 Hafta Full|Haftalık başarımlar", "Haftada 7/7 gün işaretle.") { it.weeklyStats.any { w -> w == 7 } },
        Achievement("3 Hafta Full|Haftalık başarımlar", "3 hafta 7/7 tamamla.") {
            it.weeklyStats.windowed(3).any { it.all { w -> w == 7 } }
        },
        Achievement("4 Hafta 5+ Gün|Haftalık başarımlar", "4 hafta boyunca haftada 5+ gün işaretle.") {
            it.weeklyStats.windowed(4).any { it.all { w -> w >= 5 } }
        },
        Achievement("10 Hafta 5+ Gün|Haftalık başarımlar", "10 farklı hafta 5+ gün işaretle.") {
            it.weeklyStats.count { it >= 5 } >= 10
        },
        Achievement("4 Hafta Full|Haftalık başarımlar", "4 hafta boyunca 7/7 işaretle.") {
            it.weeklyStats.windowed(4).any { it.all { w -> w == 7 } }
        },
        Achievement("6 Hafta Full|Haftalık başarımlar", "6 hafta boyunca her gün işaretle.") {
            it.weeklyStats.windowed(6).any { it.all { w -> w == 7 } }
        },

        // Sabah başarımları
        Achievement("Sabah 1|Sabah başarımları", "Bir sabah alışkanlığı işaretle.") { it.morningMarks >= 1 },
        Achievement("Sabah 3|Sabah başarımları", "3 sabah alışkanlık işaretle.") { it.morningMarks >= 3 },
        Achievement("Sabah 7|Sabah başarımları", "7 sabah boyunca alışkanlığı yap.") { it.morningMarks >= 7 },
        Achievement("Sabah 30|Sabah başarımları", "30 sabah alışkanlık işaretle.") { it.morningMarks >= 30 },
        Achievement("Sabah Ustası|Sabah başarımları", "100 sabah alışkanlık yap.") { it.morningMarks >= 100 },

        // Hız başarımları
        Achievement("Hızlı Başla|Hız başarımları", "5 dakikada işaretle.") { it.marksWithin5Min >= 1 },
        Achievement("10 Dakika İçinde|Hız başarımları", "10 dakikada işaretle.") { it.marksWithin10Min >= 1 },
        Achievement("Hız Canavarı|Hız başarımları", "Toplamda 30 kez 10 dakika içinde işaretle.") { it.marksWithin10Min >= 30 },

        // Günlük zaman dilimi başarımları
        Achievement("Kahvaltıdan Önce|Günlük zaman dilimi başarımları", "Alışkanlığı kahvaltıdan önce tamamla.") { it.beforeBreakfastMarks >= 1 },
        Achievement("Öğlene Kadar|Günlük zaman dilimi başarımları", "Öğlene kadar işaretle.") { it.marksBeforeNoon >= 1 },
        Achievement("Geceye Kalmadan|Günlük zaman dilimi başarımları", "Gece yarısından önce işaretle.") { it.marksBeforeMidnight >= 1 },
        Achievement("Gece |Günlük zaman dilimi başarımları", "20 gün gece yarısı öncesi işaretle.") { it.nightMarksStreak >= 20 },

        // Özel anlar
        Achievement("İş Yerinde|Özel anlar", "İş yerinde alışkanlık yap.") { it.workMarks >= 1 },
        Achievement("Tüm Zamanlarda|Özel anlar", "Farklı zamanlarda alışkanlık tamamla.") {
            it.morningMarks > 0 && it.marksBeforeMidnight > 0 && it.workMarks > 0
        },

        // Kararlılık
        Achievement("Bugünü Kaçırma|Kararlılık", "Bugün de işaretledin!") { it.currentStreak >= 1 },
        Achievement("Yeni Seri|Kararlılık", "Yeni bir seri başlattın.") { it.currentStreak == 1 },
        Achievement("Seriyi Sürdür|Kararlılık", "Serini bugün devam ettirdin.") { it.currentStreak > 1 },
        Achievement("Seri Canavarı|Kararlılık", "Şu anki serin 30 gün veya daha fazla.") { it.currentStreak >= 30 },

        // Aylık başarımlar
        Achievement("1 Ay Full|Aylık başarımlar", "Bir ay boyunca her gün işaretle.") { it.maxStreak >= 30 },
        Achievement("3 Ay Full|Aylık başarımlar", "3 ay boyunca düzenli alışkanlık.") { it.maxStreak >= 90 },
        Achievement("6 Ay Full|Aylık başarımlar", "6 ay boyunca hiç bozmadan.") { it.maxStreak >= 180 },
        Achievement("1 Yıl Full|Aylık başarımlar", "1 yıl boyunca hiç bozmadan.") { it.maxStreak >= 365 },

        // Ek başarımlar (100'e tamamlamak için eklenenler)
        Achievement("100 Gün Seri|Ek başarımlar", "100 gün art arda işaretle.") { it.maxStreak >= 100 },
        Achievement("150 Gün Seri|Ek başarımlar", "150 gün art arda işaretle.") { it.maxStreak >= 150 },
        Achievement("200 Gün Seri|Ek başarımlar", "200 gün art arda işaretle.") { it.maxStreak >= 200 },
        Achievement("1 Yıl Sürekli|Ek başarımlar", "1 yıl boyunca hiç bozmadan işaretle.") { it.maxStreak >= 365 },
        Achievement("2 Yıl Sürekli|Ek başarımlar", "2 yıl boyunca hiç bozmadan işaretle.") { it.maxStreak >= 730 },
        Achievement("3 Yıl Sürekli|Ek başarımlar", "3 yıl boyunca hiç bozmadan işaretle.") { it.maxStreak >= 1095 },
        Achievement("Tüm Haftalar Full|Ek başarımlar", "Bütün haftalar boyunca 7/7 işaretle.") { it.weeklyStats.all { w -> w == 7 } }
    )

    val advancedAchievements = listOf(
        Achievement("Başarı Avcısı", "10 farklı başarı kazandın.") {
            achievements.count { a -> a.condition(it) } >= 10
        },
        Achievement("Tecrübeli", "25 farklı başarı kazandın.") {
            achievements.count { a -> a.condition(it) } >= 25
        },
        Achievement("Usta", "50 başarıyı açtın.") {
            achievements.count { a -> a.condition(it) } >= 50
        },
        Achievement("Efsanevi", "70 başarıyı tamamladın.") {
            achievements.count { a -> a.condition(it) } >= 70
        },
        Achievement("Mitolojik", "100 başarıyı tamamladın.") {
            achievements.count { a -> a.condition(it) } >= 100
        }
    )

    fun getHighestDailyAchievement(progress: UserProgress, sp:SharedPreferences):List<Pair<String, Achievement?>>? {

        val categories = LinkedHashMap<String, List<Achievement>>()
        achievements.forEach { ach->
            val name = ach.name.substringBefore("|")
            val cat = ach.name.substringAfter("|")
            val desc = ach.description
            val list = categories[cat]?.toMutableList()?: mutableListOf()
            list.add(ach.copy ())
            categories[cat] = list
        }
        categories["Gelişmiş"] = advancedAchievements
        getAllCustomSp(sp)?.let{
            categories["Özel"] = it
        }

        val achs = mutableListOf<Pair<String, Achievement?>>()

        // Her kategori için başarılara bakıyoruz
        for ((category, categoryAchievements) in categories) {
            val highestAchievementL = categoryAchievements
                .filter { it.condition(progress) }

            val highestAchievement = highestAchievementL.maxByOrNull { achievements.indexOf(it) }

            totalCompleted+=highestAchievementL.size


            // Eğer kategori için geçerli bir başarı varsa, kategori ve başarıyı döndürüyoruz
            if (highestAchievement != null) {
                achs.add( category to highestAchievement)
            }
        }

        if(achs.isNotEmpty()) return achs

        // Eğer hiçbir başarı bulunmazsa null döndürüyoruz
        return null
    }

    var totalCompleted = 0

    fun getAchievementList(cat: String?=null,sp:SharedPreferences): List<Achievement>{
        val categories = LinkedHashMap<String, List<Achievement>>()
        achievements.forEach { ach->
            val name = ach.name.substringBefore("|")
            val cat = ach.name.substringAfter("|")
            val desc = ach.description
            val list = categories[cat]?.toMutableList()?: mutableListOf()
            list.add(ach.copy (name=name))
            categories[cat] = list
        }
        categories["Gelişmiş"]=advancedAchievements
        getAllCustomSp(sp)?.let{
            categories["Özel"] = it
        }

         return if(cat!=null) categories[cat]?.map { it }?: emptyList() else {
             val list = mutableListOf<Achievement>()
             categories.forEach {
                 it.value.forEach {
                     list.add(it)
                 }
             }
             list
         }
    }
    
    

    var customCount = 0
    
    
    fun addCustomToSp(
        sp: SharedPreferences,
        name: String,
        description: String,
        condition_totalMarkedDays: String,
        condition_maxStreak: String,
        condition_currentStreak: String,
        condition_weeklyStats1: String,
        condition_weeklyStats2: String,
        condition_morningMarks: String,
        condition_count: String,
        condition_hour1: String,
        condition_hour2: String
    ){
        val before = sp.getString("achievement_customs", null)?.split(",|,")?.toMutableList()?: mutableListOf()

        before += "$name<|>$description|Özel<|>$condition_totalMarkedDays<>$condition_maxStreak<>$condition_currentStreak<>$condition_weeklyStats1<>$condition_weeklyStats2<>$condition_morningMarks<>$condition_count<>$condition_hour1<>$condition_hour2"

        sp.edit().putString("achievement_customs",before.joinToString(",|,")).apply()


    }

    fun delCustomToSp(
        sp: SharedPreferences,
        name: String,
    ){
        val before = sp.getString("achievement_customs", null)?.split(",|,")?.toMutableList()?: mutableListOf()

        before.filter { it.split("<|>")[0] == name }.let{
            before -= it
        }

        sp.edit().putString("achievement_customs",before.joinToString(",|,")).apply()


    }


    fun getAllCustomSp(sp:SharedPreferences): List<Achievement>?{
        return sp.getString("achievement_customs", null)?.split(",|,")?.toMutableList()?.let{
            try {
                val li =  it.map {
                Achievement(
                    it.split("<|>")[0],
                    it.split("<|>")[1],

                    {data->
                        it.split("<|>")[2].split("<>").let{
                            val condition_totalMarkedDays = it[0]
                            val condition_maxStreakcondition_maxStreak = it[1]
                            val condition_currentStreak = it[2]
                            val condition_weeklyStats1 = it[3]
                            val condition_weeklyStats2 = it[4]
                            val condition_morningMarks = it[5]
                            val condition_count = it[6]
                            val condition_hour1 = it[7]
                            val condition_hour2 = it[8]

                            (condition_totalMarkedDays.toIntOrNull()?.let{
                                data.totalMarkedDays>=it
                            }?:true) &&
                            (condition_maxStreakcondition_maxStreak.toIntOrNull()?.let{
                                data.maxStreak>=it
                            }?:true) &&
                            (condition_currentStreak.toIntOrNull()?.let{
                                data.currentStreak>=it
                            }?:true) &&
                            (condition_weeklyStats1.toIntOrNull()?.let{stats1->
                                (condition_weeklyStats2.toIntOrNull()?.let{stats2->
                                    data.weeklyStats.count { it >= stats2 } >= stats1
                                }?:true)
                            }?:true) &&
                                    (condition_morningMarks.toIntOrNull()?.let{
                                        data.morningMarks>=it
                                    }?:true) &&

                                    (condition_count.toIntOrNull()?.let{c->
                                        (condition_hour1.toIntOrNull()?.let{hour->
                                            (condition_hour2.toIntOrNull()?.let{endHour->


                                                var count = 0
                                                data.hourCompletedCounts.filter { it.first in (hour..endHour) }
                                                        .forEach {
                                                            count+=it.second
                                                        }

                                                count>=c

                                            }?:true)
                                        }?:true)
                                    }?:true)




                        }

                    }
                )
            }
                customCount=li.size
                li
        } catch (e: Exception) {
            null
        }
        }
    }







}
