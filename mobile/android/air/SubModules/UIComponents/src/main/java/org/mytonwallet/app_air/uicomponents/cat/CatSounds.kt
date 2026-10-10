package org.mytonwallet.app_air.uicomponents.cat

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import org.mytonwallet.app_air.uicomponents.R

/**
 * Meows and purrs. Plays only while the ringer is on, the same way incoming transaction sounds do.
 */
object CatSounds {
    private const val VOLUME = 0.7f

    private var soundPool: SoundPool? = null
    private var meowSoundId = 0
    private var purrSoundId = 0
    private val loadedSoundIds = HashSet<Int>()

    fun preload(context: Context) {
        obtainSoundPool(context)
    }

    fun meow(context: Context, pitch: Float = 1f) {
        play(context, meowSoundId, pitch)
    }

    fun purr(context: Context) {
        play(context, purrSoundId, 1f)
    }

    private fun play(context: Context, soundId: Int, rate: Float) {
        val audioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        if (audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL) return

        val pool = obtainSoundPool(context)
        if (!loadedSoundIds.contains(soundId)) return
        pool.play(soundId, VOLUME, VOLUME, 1, 0, rate.coerceIn(0.5f, 2f))
    }

    private fun obtainSoundPool(context: Context): SoundPool {
        soundPool?.let { return it }

        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val pool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(attributes)
            .build()
        pool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) loadedSoundIds.add(sampleId)
        }
        val appContext = context.applicationContext
        meowSoundId = pool.load(appContext, R.raw.cat_meow, 1)
        purrSoundId = pool.load(appContext, R.raw.cat_purr, 1)
        soundPool = pool
        return pool
    }
}
