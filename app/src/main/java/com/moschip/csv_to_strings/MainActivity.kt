package com.moschip.csv_to_strings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.moschip.csv_to_strings.ui.theme.CSVtostringsTheme
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import com.opencsv.CSVReaderBuilder
import com.opencsv.ICSVParser
import com.opencsv.RFC4180ParserBuilder
import org.intellij.lang.annotations.JdkConstants
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader

var columnNo = mutableStateOf(4)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CSVtostringsTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    ScreenUi()

                }
            }
        }
    }
}

@Composable
fun ScreenUi(){

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(100.dp))
            ColumnNoInputEditText()
            Spacer(modifier = Modifier.height(100.dp))
            ShareButton("Share Xml")
        }

    }
}

@Composable
fun ShareButton(text: String) {

    val context = LocalContext.current
    Button(
        onClick = {
            checkPermissionsAndReadCsv(context, columnNo.value)
        },
        modifier = Modifier.fillMaxWidth(0.7f)
    ) {
        Modifier.align(Alignment.Bottom)
        Text(text = text)
    }
}

@Composable
fun ColumnNoInputEditText() {

    TextField(value = columnNo.value.toString(), onValueChange = {
        columnNo.value = it.toIntOrNull()?:0

    },
        label = { Text(text = "Enter Column No") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CSVtostringsTheme {
        ShareButton("Android")
    }
}


 fun checkPermissionsAndReadCsv(context: Context,columnNo: Int) {
    val permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    Permissions.check(context, permissions, null, null, object : PermissionHandler() {
        override fun onGranted() {

            readCsv(context, columnNo)
        }
    })
}

private fun readCsv(context: Context, columnNo: Int) {


    val myInput: InputStream
// initialize asset manager
    val assetManager = context.assets

    myInput = assetManager.open("csv_file.csv")

    var xmlDataToWrite: String? = null

    xmlDataToWrite += "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
    xmlDataToWrite += System.getProperty("line.separator")
    xmlDataToWrite += "<resources>"
    xmlDataToWrite += System.getProperty("line.separator")

    val rfc4180Parser: ICSVParser = RFC4180ParserBuilder().build()
    val builder = CSVReaderBuilder(InputStreamReader(myInput))
    val reader = builder.withCSVParser(rfc4180Parser).build()


    var nextLine = reader.readNext()

    while (nextLine != null) {

        var languageString = nextLine[columnNo]

        if (!languageString.isNullOrBlank()) {

//            if (languageString.contains("&")) {
//                languageString = languageString.replace("&", "&amp;")
//            }
//
//            if (nextLine[1].endsWith("_u")) {
//                languageString = "<u>$languageString</u>"
//            }

            // check if android key exists
            if (!nextLine[0].isNullOrBlank()) {
                val scriptKey = nextLine[2].lowercase()
                if (scriptKey != "r" && scriptKey != "d") {
                    languageString = languageString.replace("'","\\'")
                    xmlDataToWrite += ("<string name=\"${nextLine[0].trim()}\">${languageString.trim()}</string>")
                    xmlDataToWrite += System.getProperty("line.separator")
                }
            }
        }

        nextLine = reader.readNext()
    }

    xmlDataToWrite += System.getProperty("line.separator")
    xmlDataToWrite += "</resources>"
    writeDataToFile(context, xmlDataToWrite)

}


private fun writeDataToFile(context: Context, data: String?) {

    Log.d("Data", data.toString())

    if (data.isNullOrBlank()) {
        Toast.makeText(context, "No data", Toast.LENGTH_LONG).show()
        return
    }

    val file = getFile(context)
    val stream = FileOutputStream(file)
    stream.use { stream ->
        stream.write(data.toByteArray())
    }
    Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()

    shareFile(context, file)
}

private fun getFile(context: Context): File {
    val path: File = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!

    return File(path, "strings.txt")
}


private fun shareFile(context: Context, file: File) {
    val intentShareFile = Intent(Intent.ACTION_SEND)

    if (file.exists()) {

        val fileUri = FileProvider.getUriForFile(
            context,
            context.getApplicationContext().getPackageName().toString() + ".provider",
            file
        )

        intentShareFile.type = "text/plain"
        intentShareFile.putExtra(Intent.EXTRA_STREAM, fileUri)
        intentShareFile.putExtra(
            Intent.EXTRA_SUBJECT,
            "Sharing File..."
        )
        intentShareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...")
        context.startActivity(Intent.createChooser(intentShareFile, "Share File"))
    }
}