package com.taner.taskly.core.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BackupUtils{
    fun exportAppDataAsZip(context: Context) {
        val dbFile = File(context.getDatabasePath("task_db").absolutePath)
        val prefsFile = File(
            context.applicationInfo.dataDir + "/shared_prefs/${context.packageName}.xml"
        )


        val tempDir = File(context.cacheDir, "app_export_temp").apply { mkdirs() }

        val dbCopy = File(tempDir, "veritabani.db")
        val prefsCopy = File(tempDir, "ayarlar.xml")

        try {
            // Dosyaları kopyala
            dbFile.copyTo(dbCopy, overwrite = true)
            prefsFile.copyTo(prefsCopy, overwrite = true)

            // ZIP dosyasını oluştur
            val fileName = "taskly_backup_${System.currentTimeMillis()}.zip"
            val zipFile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )


            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zipOut ->
                listOf(dbCopy, prefsCopy).forEach { file ->
                    FileInputStream(file).use { input ->
                        val entry = ZipEntry(file.name)
                        zipOut.putNextEntry(entry)
                        input.copyTo(zipOut)
                        zipOut.closeEntry()
                    }
                }
            }

            Toast.makeText(context, "✅ Veriler dışa aktarıldı:\n${zipFile.absolutePath}", Toast.LENGTH_LONG).show()


            val zipUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                zipFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, zipUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Yedeği paylaş"))



        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "❌ Hata: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            tempDir.deleteRecursively()
        }
    }



















    // ZIP dosyasını açmak ve veritabanı ve prefs dosyalarını içe aktarmak için
    fun importAppDataFromZip(context: Context, uri: Uri) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val tempDir = File(context.cacheDir, "app_import_temp").apply { mkdirs() }
            unzip(context,inputStream, tempDir){
            importAppDataFromZip(context, tempDir)}
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Hata: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun unzip(context: Context, inputStream: InputStream?, outputDir: File,success:()->Unit) {
        try {
            ZipInputStream(inputStream).use { zipInputStream ->
                var entry: ZipEntry?
                while (zipInputStream.nextEntry.also { entry = it } != null) {
                    val file = File(outputDir, entry!!.name)
                    FileOutputStream(file).use { fileOutput ->
                        val buffer = ByteArray(1024)
                        var len: Int
                        while (zipInputStream.read(buffer).also { len = it } > 0) {
                            fileOutput.write(buffer, 0, len)
                        }
                        fileOutput.flush()

                    }
                    zipInputStream.closeEntry()
                }

                success.invoke()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "İçe aktarma sırasında bir hata oluştu", Toast.LENGTH_SHORT).show()
        }
    }

    // Dosyaları kopyalamak için
    private fun importAppDataFromZip(context: Context, tempDir: File) {
        val dbFile = File(tempDir, "veritabani.db")
        val prefsFile = File(tempDir, "ayarlar.xml")

        try {
            val db = File(context.getDatabasePath("task_db").absolutePath)
            val prefsDestination = File("${context.applicationInfo.dataDir}/shared_prefs/${context.packageName}.xml")

            // Veritabanı dosyasını kopyala
            if (dbFile.exists()) {
                dbFile.copyTo(db, overwrite = true)
            }

            // Shared Preferences dosyasını kopyala
            if (prefsFile.exists()) {
                prefsFile.copyTo(prefsDestination, overwrite = true)
            }

            Toast.makeText(context, "Veritabanı ve Ayarlar başarıyla içe aktarıldı.", Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed({
                android.os.Process.killProcess(android.os.Process.myPid())
            }, 1500)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "İçe aktarma sırasında bir hata oluştu.", Toast.LENGTH_SHORT).show()
        } finally {
            tempDir.deleteRecursively()
        }
    }












}
