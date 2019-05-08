package com.apx.sc2brackets.activities

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.apx.sc2brackets.parsers.PlayerBio
import kotlinx.android.synthetic.main.activity_player_profile.*
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import android.graphics.BitmapFactory
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import android.view.Gravity
import android.view.View
import android.view.View.*
import android.widget.FrameLayout
import android.widget.Toast
import com.apx.sc2brackets.FinishAlert
import com.apx.sc2brackets.R
import com.apx.sc2brackets.brackets.MyResourceLoader
import com.apx.sc2brackets.models.Player

private const val TAG = "PlayerProfileActivity"

class PlayerProfileActivity : AppCompatActivity(), CoroutineScope by CoroutineScope(Dispatchers.Main) {

    private var isPhotoExpanded = false

    private val loader = MyResourceLoader(context = this)
    private val okHttpClient = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_profile)

        // Get the Intent that started this activity and extract the string
        val playerName = intent.getStringExtra(BracketActivity.PLAYER_NAME_INTENT_MESSAGE)
        val playerRace = intent.getSerializableExtra(BracketActivity.PLAYER_RACE_INTENT_MESSAGE) as Player.Race

        player_bio__header.text = playerName
        player_bio__race_icon.setImageDrawable(loader.getRaceLogo(playerRace))
        player__bio_layout.setBackgroundColor(loader.getRaceBackgroundColor(playerRace))
        player_bio__zoom_icon.visibility = GONE

        if (playerName != Player.TO_BE_DEFINED) {
            launch {
                val (success, code, body) = fetchPage(playerName)
                if (!success) {
                    Log.e(TAG, "Incorrect network response: $code $body")
                    FinishAlert(this@PlayerProfileActivity, "Player data not available from site").show()
                }
                else body?.let {
                    if (it.length > 1000000) {
                        Log.w(TAG, "Large size of document: ${it.length}, stream parsing should be used")
                    }
                    val bio = parsePage(it, playerPageURI(playerName))
                    displayBio(bio)
                }
            }
            player_bio__brief_intro.text = "Player data is fetched..."
        } else {
            player_bio__brief_intro.text = "Player is to be defined by results of the previous matches"
        }
    }

    override fun onDestroy() {
        player_bio__photo.setImageDrawable(null)
        player_bio__photo_enlarged.setImageDrawable(null)
        super.onDestroy()
        cancel(CancellationException("PlayerProfileActivity destroyed"))
    }

    private suspend fun displayBio(bio: PlayerBio) {
        try {
            val loadJob = loadImageAsync(bio.photoURI)
            displayTextData(bio)
            val (_, _, photo) = loadJob.await()
            photo?.apply {
                Log.i(TAG, "Photo loaded, width=$width, height=$height")
                player_bio__photo.setImageBitmap(this)
                player_bio__zoom_icon.visibility = VISIBLE
                if (width > height) {
                    // stretch to fill width
                    player_bio__photo.adjustViewBounds = true
                } else {
                    // match layout width to image width, avoid zoom icon displacement
                    player_bio__photo.layoutParams.width = ConstraintLayout.LayoutParams.WRAP_CONTENT
                }
                player_bio__photo.setOnClickListener(togglePhotoExpand)
                player_bio__photo_enlarged.setOnClickListener(togglePhotoExpand)

            } ?: run {
                Toast.makeText(this, "Failed to load player photo", Toast.LENGTH_SHORT).show()
                player_bio__photo.setImageDrawable(
                    ContextCompat.getDrawable(this, android.R.drawable.ic_delete)
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during parsing: ${e.stackTrace}")
            FinishAlert(this@PlayerProfileActivity, "Exception happened when parsing page").show()
        } finally {
            bio.freeMemory()
        }
    }

    private fun displayTextData(bio: PlayerBio) {
        player_bio__header.text = bio.name
        player_bio__brief_intro.text = bio.briefIntro
        with(bio.fullIntro) {
            if (size > 1) {
                player_bio__full_intro.text = drop(1).joinToString(separator = "\n")
            } else {
                player_bio__full_intro.visibility = GONE
            }
        }
        player_bio__name.text = bio.realName
        player_bio__birth.text = bio.birth
        player_bio__team.text = bio.team
        player_bio__earn.text = bio.earnings

        bio.wcsCircuit?.let {
            player_bio__wcs_circuit_rank.text = it
        } ?: run {
            player_bio__wcs_circuit_rank.visibility = GONE
            player_bio__wcs_circuit_tag.visibility = GONE
        }
        bio.wcsKorea?.let {
            player_bio__wcs_korea_rank.text = it
        } ?: run {
            player_bio__wcs_korea_rank.visibility = GONE
            player_bio__wcs_korea_tag.visibility = GONE
        }
    }

    private suspend fun fetchPage(playerName: String) = withContext(Dispatchers.IO) {

        val request = Request.Builder()
            .url(playerPageURI(playerName))
            .build()
        lateinit var res: Triple<Boolean, Int, String?>
        okHttpClient.newCall(request).execute().use {
            res = Triple(it.isSuccessful, it.code(), it.body()?.string())
        }
        res
    }

    private fun loadImageAsync(imageURL: String) = async(Dispatchers.IO) {
        Log.i(TAG, "url=$imageURL")
        val request = Request.Builder().url(imageURL).build()
        lateinit var res: Triple<Boolean, Int, Bitmap?>
        okHttpClient.newCall(request).execute().use {
            var bmp: Bitmap? = null
            if (it.isSuccessful) {
                bmp = BitmapFactory.decodeStream(it.body()?.byteStream())
            } else {
                Log.e(
                    TAG,
                    "Illegal response when loading player photo $imageURL\n: ${it.code()} ${it.body()?.string()}"
                )
            }
            res = Triple(it.isSuccessful, it.code(), bmp)
        }
        res
    }

    private suspend fun parsePage(body: String, baseURI: String) = withContext(Dispatchers.Default) {
        PlayerBio.parseHTMLContent(body, baseURI)
    }

    private fun playerPageURI(playerName: String) = "https://liquipedia.net/starcraft2/$playerName"

    private val togglePhotoExpand = View.OnClickListener {
        if (isPhotoExpanded) {
            player_bio__photo_enlarged.setImageDrawable(null)
            player_bio__photo_enlarged.visibility = INVISIBLE
            player_bio__photo.visibility = VISIBLE
            player_bio__zoom_icon.visibility = VISIBLE
        } else {
            val photo = player_bio__photo.drawable
            val params = Pair(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT
            )
            player_bio__photo_enlarged.layoutParams = if (photo.intrinsicWidth > photo.intrinsicHeight) {
                FrameLayout.LayoutParams(params.first, params.second)
            } else {
                FrameLayout.LayoutParams(params.second, params.first)
            }.apply {
                gravity = Gravity.CENTER
            }
            player_bio__photo_enlarged.setImageDrawable(photo)
            player_bio__photo_enlarged.visibility = VISIBLE

            player_bio__photo.visibility = GONE
            player_bio__zoom_icon.visibility = GONE
        }
        isPhotoExpanded = !isPhotoExpanded
    }
}
