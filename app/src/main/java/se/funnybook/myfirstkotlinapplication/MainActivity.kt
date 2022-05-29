package se.funnybook.myfirstkotlinapplication

import android.app.AlertDialog
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var pendingIntent: PendingIntent
    private lateinit var itemArrayAdapter: ItemArrayAdapter
    private var itemList = ArrayList<Item>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Populating list items
        itemList.add(Item("Omvägen 12", byteArrayOf(-32, 97, -11, 73)))
        itemList.add(Item("Återvändsgränd 7", byteArrayOf(-116, 90, -126, 82)))
        itemList.add(Item("Genvägen 5", byteArrayOf(2, -18, 111, -31, 112, -86, 112)))
        itemList.add(Item("Bakgatan 9", byteArrayOf(2, -73, -44, -15, -80, -72, 0)))
        itemList.add(Item("Vintergatan 9999999", byteArrayOf(-48, 105, -11, 73)))
        itemList.add(Item("Mammas gata 1", byteArrayOf(-80, 111, -11, 73)))
        itemList.add(Item("Landsvägen 287", byteArrayOf(-80, 121, -11, 73)))

        // Initializing list view with the custom adapter
        itemArrayAdapter = ItemArrayAdapter( itemList)
        recyclerView = findViewById(R.id.item_list)
        recyclerView.adapter = itemArrayAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        //Initialise NfcAdapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (!nfcAdapter.isEnabled) {


            val alertBox = AlertDialog.Builder(this)
            alertBox.setTitle("Info")
            alertBox.setMessage("NFC måste vara på i inställningarna")

            alertBox.setPositiveButton("Slå på"
            ) { dialog, which ->
                val intent = Intent(Settings.ACTION_NFC_SETTINGS)
                startActivity(intent)
            }
            alertBox.setNegativeButton("Avbryt",
                { dialog, which -> })
            alertBox.show()
        }


        //If no NfcAdapter, display that the device has no NFC
        if (nfcAdapter == null) {
            Toast.makeText(this, "No NFC Capabilities", Toast.LENGTH_SHORT).show()
            finish()
        }
        //Create a PendingIntent object so the Android system can
        //populate it with the details of the tag when it is scanned.
        pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, this.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            0
        )
    }

    override fun onResume() {
        super.onResume()
        assert(nfcAdapter != null)
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        //On pause stop listening
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        resolveIntent(intent)
    }

    private fun resolveIntent(intent: Intent) {
        val action = intent.action
        if (NfcAdapter.ACTION_TAG_DISCOVERED == action || NfcAdapter.ACTION_TECH_DISCOVERED == action || NfcAdapter.ACTION_NDEF_DISCOVERED == action) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)!!
            val payload = detectTagData(tag).toByteArray()
        }
    }

    private fun detectTagData(tag: Tag?): String {
        val sb = StringBuilder()
        val id = tag!!.id
        var i = 0
        while (i < itemList.size && !Arrays.equals(itemList[i].id, id)) {
            i++
        }
        if (i == itemList.size) {
            Toast.makeText(this, "Brevlådan finns inte på slingan.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, itemList[i].name + " slutförd!", Toast.LENGTH_LONG)
                .show()
            // Remove item from list
            itemList.remove(itemList[i])
            recyclerView.adapter?.notifyItemRemoved(i)
        }
        return sb.toString()
    }
}