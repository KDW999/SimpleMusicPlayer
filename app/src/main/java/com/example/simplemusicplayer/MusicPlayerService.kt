package com.example.simplemusicplayer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.Toast

class MusicPlayerService : Service() {

    var mMediaPlayer: MediaPlayer? = null // 미디어 플레이어 객체를 null로 초기화
    var mBinder: MusicPlayerBinder = MusicPlayerBinder()

    inner class MusicPlayerBinder : Binder() {     // 바인더를 반환해 서비스 함수를 쓸 수 있게
        fun getService(): MusicPlayerService {
            return this@MusicPlayerService
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
    }

    // 바인드

    override fun onBind(intent: Intent?): IBinder? {
        return mBinder
    }

    // 시작 상태 & 백그라운드, startService()를 호출하면 실행되는 콜백함수
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    fun startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NOTIFICATION_SERVICE)
                    as NotificationManager
            val mChannel = NotificationChannel(
                "CHANNEL_ID",
                "CHANNEL_NAME",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(mChannel)
        }

        val notification: Notification = Notification.Builder(this, "ChANNEL_ID")
            .setSmallIcon(R.drawable.ic_play)
            .setContentTitle("뮤직 플레이어 앱")
            .setContentText("앱 실행 중입니다")
            .build()

        startForeground(1, notification)
    }

    // 서비스 종료
    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        }
    }

    // 재생 확인용
    fun isPlaying(): Boolean {
        return (mMediaPlayer != null && mMediaPlayer?.isPlaying ?: false)
    }


    fun play() {
        if (mMediaPlayer == null) {
            // 음악 파일 리소스를 가져와 미디어 플레이어 객체를 할당
            mMediaPlayer = MediaPlayer.create(this, R.raw.chocolate)

            mMediaPlayer?.setVolume(1.0f, 1.0f); // 볼륨 지정
            mMediaPlayer?.isLooping = true // 반복재생 여부
            mMediaPlayer?.start()
        } else { // 음악 재생
            if (mMediaPlayer!!.isPlaying) {
                Toast.makeText(this, "이미 음악 실행 중", Toast.LENGTH_SHORT).show()
            } else {
                mMediaPlayer?.start() // 음악 재생
            }
        }
    }

    // 일시 정지
    fun pause() {
        mMediaPlayer?.let {
            if (it.isPlaying) {
                it.pause() // 음악 일시정지
            }
        }
    }

    // 재생 중지
    fun stop() {
        mMediaPlayer?.let {
            if (it.isPlaying) {
                it.stop() // 음악 멈춤
                it.release() // 미디어 플레이어에 할당된 자원 해제
                mMediaPlayer = null
            }
        }
    }

}