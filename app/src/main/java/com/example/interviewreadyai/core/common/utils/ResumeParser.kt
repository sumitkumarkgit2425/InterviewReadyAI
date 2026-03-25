package com.example.interviewreadyai.core.common.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper

object ResumeParser {
    private const val TAG = "ResumeParser"

    fun extractTextFromUri(
            context: Context,
            uri: Uri,
            mimeType: String?,
            onSuccess: (String) -> Unit,
            onError: (String) -> Unit
    ) {
        when {
            mimeType == "application/pdf" || uri.toString().endsWith(".pdf", ignoreCase = true) -> {
                extractFromPdf(context, uri, onSuccess, onError)
            }
            mimeType?.startsWith("image/") == true -> {
                extractFromImage(context, uri, onSuccess, onError)
            }
            else -> {
                onError("Unsupported file format. Please upload a PDF or an Image.")
            }
        }
    }

    private fun extractFromPdf(
            context: Context,
            uri: Uri,
            onSuccess: (String) -> Unit,
            onError: (String) -> Unit
    ) {
        try {
            
            PDFBoxResourceLoader.init(context)

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val document = PDDocument.load(inputStream)
                val pdfStripper = PDFTextStripper()
                val parsedText = pdfStripper.getText(document)
                document.close()

                if (parsedText.trim().isNotEmpty()) {
                    onSuccess(parsedText)
                } else {
                    onError("Could not extract text from this PDF. It might be an image-based PDF.")
                }
            }
                    ?: onError("Failed to open the PDF file.")
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing PDF", e)
            onError("Error parsing PDF: ${e.message}")
        }
    }

    private fun extractFromImage(
            context: Context,
            uri: Uri,
            onSuccess: (String) -> Unit,
            onError: (String) -> Unit
    ) {
        try {
            val image = InputImage.fromFilePath(context, uri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer
                    .process(image)
                    .addOnSuccessListener { visionText ->
                        if (visionText.text.trim().isNotEmpty()) {
                            onSuccess(visionText.text)
                        } else {
                            onError("No text found in the image.")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error recognizing text in image", e)
                        onError("ML Kit Error: ${e.message}")
                    }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading image", e)
            onError("Error loading image: ${e.message}")
        }
    }
}

