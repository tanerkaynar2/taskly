package com.taner.taskly.domain.model

data class Task(
    var id: Int = 0,
    val title: String,
    val description: String?,
    val date: Long?, // Görev tarihi
    val days: String?,
    val time: String?, // Görev saati
    val category: TaskCategory,
    val customCategoryDetail: String? = null,
    var status: TaskStatus, // Görev durumu
    val priority: TaskPriority, // Görev önceliği
    val notification: Boolean, // Bildirim alacak mı?
    val repetition: TaskRepetition?, // Görev tekrarı (günlük, haftalık, vb.)
    val locations: String?, // Görevle ilişkili bir yer
    val subTasks: String?, // Alt görevler
    val color: Int?, // Renk
    val attachments: String?, // Eklentiler (dosyalar, fotoğraflar)
    var isCompleted: Boolean = false,
    var lastCompletedDate: String?=null,//yıl ay gün saat dakika format: dd MMM yyyy, HH:mm
    var lastNotificationTime: Long?=null,//timestamp
    var notificationDelayMin: Int?=null//dakika
)

/* kaögün kat  bildirim  yer altgörev attac  durum tekrar
time: Görev saati (örn. "08:00 AM").

status: Görevin durumu, örneğin "Yapılmadı", "Yapılıyor", "Tamamlandı".

priority: Görev önceliği (örn. "Düşük", "Orta", "Yüksek").

notification: Bildirim alınıp alınmayacağı.

repetition: Görev tekrar ediyorsa, tekrar sıklığı (örn. "Her gün", "Haftalık").

location: Görevle ilişkili bir konum (isteğe bağlı).

subTasks: Alt görevler, örneğin bir alışveriş görevinde "ekmek al", "süt al" gibi.

color: Göreve özel bir renk.

attachments: Göreve eklenecek dosyalar, fotoğraflar veya belgeler.
 */