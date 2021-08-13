package com.example.fourthproject

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.example.fourthproject.databinding.ActivityGrandPaBinding

class GrandPaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGrandPaBinding
    private var myHum = arrayListOf<Int>(0,0,0,0)
    private var usingHum = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGrandPaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        //할아버지 -> 재배 액티비티 전환
        tofromSecond()
        //가습기 버튼으로 다이얼로그 전시
    }

    //가습기 보유상태 저장용(임시로 hum[0],hum[1]만)
    override fun onResume() {
        super.onResume()
        val sp = getSharedPreferences("hum_info", MODE_PRIVATE)
        myHum[0] = sp.getInt("hum1",0)
        myHum[1] = sp.getInt("hum2",0)
        usingHum = sp.getInt("usingHum",0)
//        myHum[1] = 0//초기화하기 위한 임시 값
    }

    override fun onPause() {
        super.onPause()
        val sp = getSharedPreferences("hum_info", MODE_PRIVATE)
        val editor = sp.edit()
        //hum2보유상태 백업
        editor.putInt("hum1",myHum[0])
        editor.putInt("hum2",myHum[1])
        editor.putInt("usingHum",usingHum)
        editor.commit()
    }
    private fun tofromSecond() {
        //이거 순서 바뀌면 np안옴
        binding.currentNpGrandpa.text= intent.getIntExtra("Currentnp1",0).toString()
        val intent = Intent(this,SecondActivity::class.java)
        binding.menu2.setOnClickListener {
            var alert = AlertDialog.Builder(this)
            alert.setView(R.layout.dialog_select_hum)
            var dialog = alert.create()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
//        동작은 되지만 꺼질 때의 애니메이션이 미작동
            (dialog.window!!.decorView as ViewGroup)
                .getChildAt(0).startAnimation(
                    AnimationUtils.loadAnimation(
                        this, R.anim.open
                    )
                )
            dialog.show()
            //일단은 2번째 가습기만
            val hum1 = dialog.findViewById<ImageView>(R.id.hum_lv_1)
            val hum2 = dialog.findViewById<ImageView>(R.id.hum_lv_2)
            val using1 = dialog.findViewById<ImageView>(R.id.using1)
            val using2 = dialog.findViewById<ImageView>(R.id.using2)
            val cancle = dialog.findViewById<ImageView>(R.id.cancle)

            hum1?.setOnClickListener {
                //0은 무료이므로 조건문 안씀.
                using1?.visibility = View.VISIBLE
                using2?.visibility = View.INVISIBLE
                intent.putExtra("selectedHum", 1)
                usingHum = 1
            }
            hum2?.setOnClickListener {
                //0이면 보유하지 않은 상태이므로, 값을 지불하고 secondAcitivity에 img set해야함.
                //여기에서 intent에 2를 실어서 보내고, secondActivity에서는 변수를 따로 선언해서 저장해놨다가
                //sharedPreference활용해서 보존시키고 계속 이미지 셋 하면된다.
                //0이면 돈을 지불하고, 0이 아니면, 돈 지불 안하고 그냥 변경
                if(myHum[1]==0){
                    binding.currentNpGrandpa.text = (binding.currentNpGrandpa.text.toString().toInt()-500).toString()
                    myHum[1] = 1
                }
                using1?.visibility = View.INVISIBLE
                using2?.visibility = View.VISIBLE
                intent.putExtra("selectedHum",2)
                usingHum = 2
            }
            cancle?.setOnClickListener{
                dialog.dismiss()
            }
            if(usingHum==1){
                using1?.visibility = View.VISIBLE
                using2?.visibility = View.INVISIBLE
            }
            else if(usingHum==2){
                using1?.visibility = View.INVISIBLE
                using2?.visibility = View.VISIBLE
            }
            if(using1?.visibility==View.VISIBLE){
                intent.putExtra("selectedHum", 1)
            }
            else if(using2?.visibility==View.VISIBLE){
                intent.putExtra("selectedHum", 2)
            }
        }
            binding.topBtn2Img.setOnClickListener {
            if(usingHum==1) {
                intent.putExtra("selectedHum", 1)
            }
            else if(usingHum==2){
                intent.putExtra("selectedHum", 2)
            }
            intent.putExtra("Currentnp",binding.currentNpGrandpa.text.toString().toInt())
            startActivity(intent)
        }
    }

}