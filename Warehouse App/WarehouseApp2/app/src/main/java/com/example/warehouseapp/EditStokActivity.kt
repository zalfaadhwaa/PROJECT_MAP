package com.example.warehouseapp

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EditStokActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "EditStokActivity"
    }

    private lateinit var etBarang: EditText
    private lateinit var spinnerKategori: Spinner
    private lateinit var spinnerUkuran: Spinner
    private lateinit var etStok: EditText
    private lateinit var btnUpdate: Button
    private lateinit var btnBack: Button
    private lateinit var stokId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_stok)

        etBarang = findViewById(R.id.et_barang)
        spinnerKategori = findViewById(R.id.spinner_kategori)
        spinnerUkuran = findViewById(R.id.spinner_ukuran)
        etStok = findViewById(R.id.et_stok)
        btnUpdate = findViewById(R.id.btn_update)
        btnBack = findViewById(R.id.btn_back)

        val kategoriList = arrayOf("Kaos", "Kemeja", "Celana", "Rok", "Hijab")
        val ukuranList = arrayOf("XS", "S", "M", "L", "XL", "XXL")

        val kategoriAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, kategoriList)
        kategoriAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerKategori.adapter = kategoriAdapter

        val ukuranAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ukuranList)
        ukuranAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerUkuran.adapter = ukuranAdapter

        stokId = intent.getStringExtra("stokId") ?: ""
        etBarang.setText(intent.getStringExtra("stokBarang") ?: "")

        val kategori = intent.getStringExtra("stokKategori") ?: ""
        val ukuran = intent.getStringExtra("stokUkuran") ?: ""

        val kategoriPosition = (spinnerKategori.adapter as ArrayAdapter<String>).getPosition(kategori)
        if (kategoriPosition >= 0) {
            spinnerKategori.setSelection(kategoriPosition)
        }

        val ukuranPosition = (spinnerUkuran.adapter as ArrayAdapter<String>).getPosition(ukuran)
        if (ukuranPosition >= 0) {
            spinnerUkuran.setSelection(ukuranPosition)
        }

        etStok.setText(intent.getIntExtra("stokStok", 0).toString())

        btnUpdate.setOnClickListener {
            updateStok()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun updateStok() {
        val barang = etBarang.text.toString()
        val kategori = spinnerKategori.selectedItem.toString()
        val ukuran = spinnerUkuran.selectedItem.toString()
        val stok = etStok.text.toString().toIntOrNull()

        if (barang.isEmpty() || kategori.isEmpty() || ukuran.isEmpty() || stok == null) {
            Toast.makeText(this, "Harap isi semua field", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Updating Stok: ID: $stokId, Barang: $barang, Kategori: $kategori, Ukuran: $ukuran, Stok: $stok")

        val db = Firebase.firestore
        val stokData = StokModel(
            Id = stokId,
            Barang = barang,
            Kategori = kategori,
            Ukuran = ukuran,
            Stok = stok
        )

        db.collection("stok")
            .document(stokId)
            .set(stokData)
            .addOnSuccessListener {
                Toast.makeText(this, "Berhasil memperbarui data!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating document", e)
                Toast.makeText(this, "Gagal memperbarui data", Toast.LENGTH_SHORT).show()
            }
    }
}