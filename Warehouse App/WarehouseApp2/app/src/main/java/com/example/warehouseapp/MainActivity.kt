package com.example.warehouseapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var listStok: ListView
    private lateinit var btnCreateStok: FloatingActionButton
    private lateinit var btnLogout: Button
    private lateinit var labelHeader: TextView
    private lateinit var spinnerKategori: Spinner

    private val items = ArrayList<StokModel>() // Data stok
    private lateinit var adapter: StokAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listStok = findViewById(R.id.list_stok)
        btnCreateStok = findViewById(R.id.btn_create_stok)
        btnLogout = findViewById(R.id.btn_logout)
        labelHeader = findViewById(R.id.label_header)
        spinnerKategori = findViewById(R.id.spinner_kategori)

        val sharedPreferences = getSharedPreferences("app_preference", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("name", "User")
        labelHeader.text = "Selamat Datang, $username!"

        btnLogout.setOnClickListener {
            logout()
        }

        btnCreateStok.setOnClickListener {
            val intent = Intent(this, CreateStokActivity::class.java)
            startActivity(intent)
        }

        fetchDataFromFirestore()

        listStok.setOnItemClickListener { adapterView, _, position, _ ->
            val item = adapterView.getItemAtPosition(position) as StokModel
            val intent = Intent(this, EditStokActivity::class.java).apply {
                putExtra("stokId", item.Id)
                putExtra("stokBarang", item.Barang)
                putExtra("stokKategori", item.Kategori)
                putExtra("stokUkuran", item.Ukuran)
                putExtra("stokStok", item.Stok.toString())
            }
            startActivity(intent)
        }

        listStok.setOnItemLongClickListener { adapterView, _, position, _ ->
            val item = adapterView.getItemAtPosition(position) as StokModel
            showDeleteConfirmationDialog(item)
            true
        }

        setupSpinner()
    }

    private fun setupSpinner() {
        val kategori = arrayOf("Semua", "Kemeja", "Kaos", "Celana", "Rok", "Hijab")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, kategori)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerKategori.adapter = spinnerAdapter

        spinnerKategori.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val kategoriTerpilih = kategori[position]
                filterData(kategoriTerpilih)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
    }

    private fun filterData(kategori: String) {
        val filteredItems = when (kategori) {
            "Kemeja" -> items.filter { it.Kategori == "Kemeja" }
            "Kaos" -> items.filter { it.Kategori == "Kaos" }
            "Celana" -> items.filter { it.Kategori == "Celana" }
            "Rok" -> items.filter { it.Kategori == "Rok" }
            "Hijab" -> items.filter { it.Kategori == "Hijab" }
            else -> items
        }

        adapter = StokAdapter(this, R.layout.stok_item, filteredItems)
        listStok.adapter = adapter
    }

    private fun logout() {
        val sharedPreferences = getSharedPreferences("app_preference", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        fetchDataFromFirestore()
    }

    private fun fetchDataFromFirestore() {
        val db = Firebase.firestore
        db.collection("stok")
            .get()
            .addOnSuccessListener { result ->
                items.clear()
                for (document in result) {
                    items.add(
                        StokModel(
                            Id = document.id,
                            Barang = document.data["barang"] as? String ?: "Unknown",
                            Kategori = document.data["kategori"] as? String ?: "Unknown",
                            Ukuran = document.data["ukuran"] as? String ?: "Unknown",
                            Stok = document.data["stok"] as? Int ?: 0
                        )
                    )
                }
                adapter = StokAdapter(this, R.layout.stok_item, items)
                listStok.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
                Toast.makeText(this, "Gagal mengambil data.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDeleteConfirmationDialog(item: StokModel) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Apakah Anda yakin ingin menghapus item '${item.Barang}'?")
            .setCancelable(false)
            .setPositiveButton("Ya") { _, _ ->
                item.Id?.let { id -> item.Barang?.let { deleteItem(id, it) } }
            }
            .setNegativeButton("Tidak") { dialog, _ -> dialog.dismiss() }

        val alert = dialogBuilder.create()
        alert.setTitle("Konfirmasi Hapus")
        alert.show()
    }

    private fun deleteItem(id: String, title: String) {
        val db = Firebase.firestore
        db.collection("stok").document(id).delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Berhasil menghapus item: $title", Toast.LENGTH_SHORT).show()
                fetchDataFromFirestore() // Memperbarui tampilan setelah penghapusan
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Gagal menghapus item: $title.", Toast.LENGTH_SHORT).show()
                Log.w(TAG, "Error deleting document.", exception)
            }
    }
}