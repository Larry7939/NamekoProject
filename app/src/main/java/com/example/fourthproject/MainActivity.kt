package com.example.fourthproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import com.example.fourthproject.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //뷰 깜빡임 애니메이션
        val anim = AlphaAnimation(0.0f,1.0f)
        anim.duration = 250
        anim.startOffset=540
        anim.repeatMode = Animation.REVERSE
        anim.repeatCount = Animation.INFINITE
        binding.introTouchMain.startAnimation(anim)

        //화면 터치시 다음 화면 이동
        binding.introMain.setOnClickListener{
            val intent = Intent(this,SecondActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.fadein,R.anim.fadeout)
        }
    }
}
