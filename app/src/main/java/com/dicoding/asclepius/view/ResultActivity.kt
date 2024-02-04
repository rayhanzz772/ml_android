package com.dicoding.asclepius.view

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.asclepius.databinding.ActivityResultBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import org.tensorflow.lite.task.vision.classifier.Classifications

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUri = Uri.parse(intent.getStringExtra(EXTRA_IMAGE_URI))


        imageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.resultImage.setImageURI(it)
        }

        val label = intent.getStringExtra(EXTRA_RESULT)
        val score = intent.getFloatExtra(EXTRA_RESULT_SCORE, 0.1F)
        val scoreFormatted = "%.1f".format(score)

        if (label == null) {
            binding.resultText.text = "Nilai Kosong !"
        } else {
            binding.resultText.text = label
            binding.resultScore.text = scoreFormatted + "%"
        }
    }



    companion object {
        const val EXTRA_IMAGE_URI = "extra image uri"
        const val EXTRA_RESULT = "extra_result"
        const val EXTRA_RESULT_SCORE = "extra_result_score"
    }
}
