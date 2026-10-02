package com.latihan.logisticstrackerapp

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var editTextTrackingNumber: EditText
    private lateinit var buttonTrack: Button
    private lateinit var progressBarLoading: ProgressBar
    private lateinit var cardResult: CardView
    private lateinit var textViewErrorMessage: TextView
    private lateinit var textViewResiTitle: TextView
    private lateinit var textViewStatusBadge: TextView
    private lateinit var textViewCourier: TextView
    private lateinit var textViewLocation: TextView
    private lateinit var textViewRecipient: TextView
    private lateinit var textViewEta: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        editTextTrackingNumber = findViewById(R.id.editTextTrackingNumber)
        buttonTrack = findViewById(R.id.buttonTrack)
        progressBarLoading = findViewById(R.id.progressBarLoading)
        cardResult = findViewById(R.id.cardResult)
        textViewErrorMessage = findViewById(R.id.textViewErrorMessage)
        textViewResiTitle = findViewById(R.id.textViewResiTitle)
        textViewStatusBadge = findViewById(R.id.textViewStatusBadge)
        textViewCourier = findViewById(R.id.textViewCourier)
        textViewLocation = findViewById(R.id.textViewLocation)
        textViewRecipient = findViewById(R.id.textViewRecipient)
        textViewEta = findViewById(R.id.textViewEta)

        buttonTrack.setOnClickListener {

            val nomorResi = editTextTrackingNumber.text
                .toString()
                .trim()

            if (nomorResi.isNotEmpty()) {
                lacakPaket(nomorResi)
            } else {
                Toast.makeText(
                    this,
                    "Silakan ketik nomor resi terlebih dahulu (contoh: EXP-8801)",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun lacakPaket(nomorResi: String) {

        progressBarLoading.visibility = View.VISIBLE
        cardResult.visibility = View.GONE
        textViewErrorMessage.visibility = View.GONE
        buttonTrack.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {

            try {

                val response = ApiClient.apiService
                    .getTrackingDetail(nomorResi)

                withContext(Dispatchers.Main) {

                    progressBarLoading.visibility = View.GONE
                    buttonTrack.isEnabled = true

                    if (response.isSuccessful && response.body() != null) {

                        tampilkanDataPaket(response.body()!!)

                    } else {

                        textViewErrorMessage.text =
                            "Nomor resi [$nomorResi] tidak ditemukan pada database server (HTTP ${response.code()})"

                        textViewErrorMessage.visibility = View.VISIBLE
                    }
                }

            } catch (e: Exception) {

                withContext(Dispatchers.Main) {

                    progressBarLoading.visibility = View.GONE
                    buttonTrack.isEnabled = true

                    textViewErrorMessage.text =
                        "Koneksi internet bermasalah: ${
                            e.localizedMessage ?: "Gagal terhubung ke server"
                        }"

                    textViewErrorMessage.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun tampilkanDataPaket(data: TrackingResponse) {

        textViewResiTitle.text = data.trackingNumber

        textViewCourier.text =
            "Kurir: ${data.courierName} (${data.serviceType})"

        textViewLocation.text =
            "Lokasi: ${data.lastLocation}"

        textViewRecipient.text =
            "Penerima: ${data.recipientName}"

        textViewEta.text =
            "Estimasi: ${data.estimatedDelivery}"

        textViewStatusBadge.text = data.status

        when (data.status) {

            "DELIVERED" -> {

                textViewStatusBadge.setBackgroundColor(
                    Color.parseColor("#DCFCE7")
                )

                textViewStatusBadge.setTextColor(
                    Color.parseColor("#15803D")
                )
            }

            "IN_TRANSIT" -> {

                textViewStatusBadge.setBackgroundColor(
                    Color.parseColor("#FEF3C7")
                )

                textViewStatusBadge.setTextColor(
                    Color.parseColor("#B45309")
                )
            }

            else -> {

                textViewStatusBadge.setBackgroundColor(
                    Color.parseColor("#F1F5F9")
                )

                textViewStatusBadge.setTextColor(
                    Color.parseColor("#475569")
                )
            }
        }

        cardResult.visibility = View.VISIBLE
    }
}