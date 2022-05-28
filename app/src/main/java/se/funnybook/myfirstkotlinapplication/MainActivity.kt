package se.funnybook.myfirstkotlinapplication

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class MainActivity : AppCompatActivity() {

    var recyclerView: RecyclerView? = null

    //Initialize attributes
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null

    var itemArrayAdapter: ItemArrayAdapter? = null

    // Initializing list view with the custom adapter
    var itemList = ArrayList<Item>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Initializing list view with the custom adapter
        itemArrayAdapter = ItemArrayAdapter( itemList)
        recyclerView = findViewById(R.id.item_list)
        val recyclerView: RecyclerView = findViewById(R.id.item_list)
        recyclerView.adapter = itemArrayAdapter

        recyclerView.setLayoutManager(LinearLayoutManager(this))
/*
        recyclerView.setItemAnimator(DefaultItemAnimator())
        recyclerView.setAdapter(itemArrayAdapter)
*/
        // Populating list items
        val bytes = byteArrayOf(-32, 97, -11, 73)
        itemList.add(Item("Omvägen 12", bytes))
        itemList.add(Item("Återvändsgränd 7", byteArrayOf(-116, 90, -126, 82)))
        itemList.add(Item("Genvägen 5", byteArrayOf(2, -18, 111, -31, 112, -86, 112)))
        itemList.add(Item("Bakgatan 9", byteArrayOf(2, -73, -44, -15, -80, -72, 0)))

        //Initialise NfcAdapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        //If no NfcAdapter, display that the device has no NFC
        if (nfcAdapter == null) {
            Toast.makeText(this, "NO NFC Capabilities", Toast.LENGTH_SHORT).show()
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
        nfcAdapter!!.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        //On pause stop listening
        if (nfcAdapter != null) {
            nfcAdapter!!.disableForegroundDispatch(this)
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
            Objects.requireNonNull(recyclerView!!.adapter).notifyDataSetChanged()
        }
        return sb.toString()
    }
}