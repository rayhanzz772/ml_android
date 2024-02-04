package com.dicoding.asclepius.view

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import org.tensorflow.lite.task.vision.classifier.Classifications

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var imageClassifierHelper: ImageClassifierHelper
    private var currentImageUri: Uri? = null

    object GlobalDataHolder {
        var globalResultLabel: String? = null
        var globalResultScore: Float = 0.0f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageClassifierHelper = ImageClassifierHelper(
            modelName = "cancer_classification.tflite",
            context = this,
            classifierListener = object : ImageClassifierHelper.ClassifierListener {
                override fun onError(error: String) {
                    Log.e("ImageClassifierHelper", "Error: $error")
                    showToast("Error: $error")
                }


                override fun onResults(
                    results: List<Classifications>?,
                ) {
                    results?.let {
                        if (it.isNotEmpty()) {
                            val topResult = it[0]
                            val categories = topResult.categories

                            val highScoreCategory = categories.find { category ->
                                category.score >= 0.5
                            }

                            highScoreCategory?.let { category ->
                                val label = category.label
                                val score = category.score * 100

                                GlobalDataHolder.globalResultLabel = label
                                GlobalDataHolder.globalResultScore = score

                                Log.d("onresult", "Result: $label $score%")
                            } ?: run {
                                showToast("No category with score above 50%")
                            }
                        } else {
                            showToast("No classification results")
                        }

                    }
                }
            }
        )
        binding.galleryButton.setOnClickListener { startGallery() }
        binding.analyzeButton.setOnClickListener {
            currentImageUri?.let {
                analyzeImage(it)
            } ?: run {
                showToast(getString(R.string.empty_image_warning))
            }
        }

    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun analyzeImage(imageUri: Uri) {
        val imageBitmap = loadBitmapFromUri(imageUri)
        imageClassifierHelper.classifyStaticImage(imageBitmap)
        moveToResult()
    }


    private fun loadBitmapFromUri(imageUri: Uri): Bitmap {
        val inputStream = contentResolver.openInputStream(imageUri)
        return BitmapFactory.decodeStream(inputStream)
    }

    private fun moveToResult() {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra(ResultActivity.EXTRA_RESULT, GlobalDataHolder.globalResultLabel)
        intent.putExtra(ResultActivity.EXTRA_RESULT_SCORE, GlobalDataHolder.globalResultScore)
        intent.putExtra(ResultActivity.EXTRA_IMAGE_URI, currentImageUri.toString())
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    companion object{
        const val EXTRA_RESULT = "extra_result"
    }

}