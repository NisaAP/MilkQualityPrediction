package com.example.milkqualityprediction

import android.content.res.AssetManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class SimulasiModel : AppCompatActivity() {
    private lateinit var interpreter: Interpreter
    private val mModelPath = "milkquality.tflite"

    private lateinit var resultText: TextView
    private lateinit var pH: EditText
    private lateinit var Temprature: EditText
    private lateinit var TasteGroup: RadioGroup
    private lateinit var OdorGroup: RadioGroup
    private lateinit var FatGroup: RadioGroup
    private lateinit var TurbidityGroup: RadioGroup
    private lateinit var Colour: EditText
    private lateinit var checkButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simulasi_model)

        resultText = findViewById(R.id.txtResult)
        pH = findViewById(R.id.pH)
        Temprature = findViewById(R.id.Temprature)
        TasteGroup = findViewById(R.id.TasteGroup)
        OdorGroup = findViewById(R.id.OdorGroup)
        FatGroup = findViewById(R.id.FatGroup)
        TurbidityGroup = findViewById(R.id.TurbidityGroup)
        Colour = findViewById(R.id.Colour)
        checkButton = findViewById(R.id.btnCheck)

        checkButton.setOnClickListener {
            val inputPH = pH.text.toString()
            val inputTemprature = Temprature.text.toString()
            val inputColour = Colour.text.toString()

            if (inputPH.isEmpty() || inputTemprature.isEmpty() || inputColour.isEmpty()) {
                Toast.makeText(this, "Kolom tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val result = doInference(
                inputPH,
                inputTemprature,
                getSelectedRadioText(TasteGroup),
                getSelectedRadioText(OdorGroup),
                getSelectedRadioText(FatGroup),
                getSelectedRadioText(TurbidityGroup),
                inputColour
            )

            runOnUiThread {
                when (result) {
                    0 -> resultText.text = "High"
                    1 -> resultText.text = "Low"
                    2 -> resultText.text = "Medium"
                }
            }
        }

        initInterpreter()
    }

    private fun initInterpreter() {
        val options = Interpreter.Options()
        options.setNumThreads(8)
        options.setUseNNAPI(true)
        interpreter = Interpreter(loadModelFile(assets, mModelPath), options)
    }

    private fun doInference(
        input1: String,
        input2: String,
        input3: String,
        input4: String,
        input5: String,
        input6: String,
        input7: String
    ): Int {
        val inputVal = FloatArray(7)
        inputVal[0] = input1.toFloat()
        inputVal[1] = input2.toFloat()
        inputVal[2] = if (input3 == "Baik") 1.0f else 0.0f // Taste
        inputVal[3] = if (input4 == "Baik") 1.0f else 0.0f // Odor
        inputVal[4] = if (input5 == "Tinggi") 1.0f else 0.0f // Fat
        inputVal[5] = if (input6 == "Tinggi") 1.0f else 0.0f // Turbidity
        inputVal[6] = input7.toFloat()
        val output = Array(1) { FloatArray(3) }
        interpreter.run(inputVal, output)

        Log.e("result", (output[0].toList() + " ").toString())

        return output[0].indexOfFirst { it == output[0].maxOrNull() }
    }

    private fun getSelectedRadioText(radioGroup: RadioGroup): String {
        val selectedRadioButtonId = radioGroup.checkedRadioButtonId
        val selectedRadioButton = findViewById<RadioButton>(selectedRadioButtonId)
        return selectedRadioButton.text.toString()
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
}
