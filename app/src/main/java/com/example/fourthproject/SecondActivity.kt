package com.example.fourthproject
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.fourthproject.databinding.ActivitySecondBinding
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timerTask
class SecondActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySecondBinding
    //나메코의 위치 인덱스 배열
    private var namekoIdx: ArrayList<Int> =
        arrayListOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
    //원목 위 나메코 이미지뷰들
    private lateinit var namekoImg: ArrayList<ImageView>
    //확률 배열(no.1->62%, no.2->17%, no.3->12%, no.4->9%)
    var rateArr:ArrayList<Int> = arrayListOf<Int>(62,17,12,9)
    //확률 적용배열(1이 80개, 2가 20개)
    private var rateArr100:ArrayList<Int> = ArrayList<Int>()
    private val handler = Handler(Looper.getMainLooper())
    private var timer: Timer? = null
    private var timertask: TimerTask? = null
    private var currprog:Int = 0 //현재 진행도
    private var option=0 //현재 적용되고 있는 먹이
    private var humidifier:Int =0
    private var humidifier_timeBonus:Int = 0
    var makeGson: Gson? = GsonBuilder().create()
    var listType: TypeToken<ArrayList<Int>> = object:TypeToken<ArrayList<Int>>(){}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        humidifier=intent.getIntExtra("selectedHum",0)
        //확률 배열 rateArr_100초기화
        initRate()
        //나메코 이미지 뷰들을 배열에 삽입
        namekoImg = arrayListOf(
        binding.nameko11,
        binding.nameko12,
        binding.nameko13,
        binding.nameko14,
        binding.nameko15,
        binding.nameko16,
        binding.nameko17,
        binding.nameko18,
        binding.nameko19,
        binding.nameko21,
        binding.nameko22,
        binding.nameko23,
        binding.nameko24,
        binding.nameko25,
        binding.nameko26,
        binding.nameko27,
        binding.nameko28,
        )
        //버섯 애니메이션
        mushAnim()
        //보온기 애니메이션
        warmAnim()
        //조명기 애니메이션
        lightAnim()
        //먹이 주기
        feedSet()
        //수확
        harvest()
        //할아버지 액티비티 전환(np정보 주고받기)
        tofromGrandpa()
        //가습기 보유상태에 따른 능력치 추가
        humBonus()

    }
    //게이지 진행정보 저장 및 복원 & namekoIdx저장 및 복원하면서 img set하기 -> 이 때 복원 과정에서 feeding함수의 일부 호출해야함. 인덱스에 따라 이미지 set하는 부분
    //가습기 정보 저장 및 복원
    override fun onResume() {
        super.onResume()
        val sp = getSharedPreferences("gauge_info", MODE_PRIVATE)
        binding.currentNp.text = sp.getString("currNp","1000")
        binding.currentNp.text = intent.getIntExtra("Currentnp",binding.currentNp.text.toString().toInt()).toString()
//        binding.currentNp.text = "1000" ////초기화하기 위해 임시로 저장한것임
        option = sp.getInt("option",0)
        if(option!=0){
            if(option==1) {
                feeding1()
                binding.currentNp.text = (binding.currentNp.text.toString().toInt()+120).toString()
                binding.feed1.setImageResource(R.drawable.feed1_on)
            }
            else if(option==2) {
                feeding2()
                binding.currentNp.text = (binding.currentNp.text.toString().toInt()+50).toString()
                binding.feed2.setImageResource(R.drawable.feed2_on)
            }
            else if(option==3) {
                feeding3()
                binding.currentNp.text = (binding.currentNp.text.toString().toInt()+0).toString()
                binding.feed3.setImageResource(R.drawable.feed3_on)
            }
            else if(option==4) {
                feeding4()
                binding.currentNp.text = (binding.currentNp.text.toString().toInt()+30).toString()
                binding.feed4.setImageResource(R.drawable.feed4_on)
            }
            else if(option==5) {
                feeding5()
                binding.currentNp.text = (binding.currentNp.text.toString().toInt()+80).toString()
                binding.feed5.setImageResource(R.drawable.feed5_on)
            }
            binding.progressbar.progress = sp.getInt("currprog",0)
        }
            //화면 전환 할 때마다 돈이 줄어들음. -> feeding1~5함수 호출하면서 생긴 문제 -> 돈 더해주면 된다.
            //하단 버튼 빨간색 되는 거 풀림.-> 해결
        humidifier=sp.getInt("humidifier",0)
        //나메코 인덱스 배열 불러온 다음, 이걸 나메코 이미지 배열에 적용해야함.
        var strContact:String? = sp.getString("namekoIdx", "")
        namekoIdx = makeGson?.fromJson(strContact,listType.type) ?: namekoIdx
        if(num_nameko()!=0){ // 나메코가 하나라도 있으면, for문 돌리면서 img set하고 애니메이션까지 적용시켜준다.
            for(i in 0 until namekoIdx.size){
                when {
                    namekoIdx[i]==1 -> {
                        namekoImg[i].setImageResource(R.drawable.nameko)
                        namekoImg[i].visibility=View.VISIBLE
                    }
                    namekoIdx[i]==2 -> {
                        namekoImg[i].setImageResource(R.drawable.nameko2)
                        namekoImg[i].visibility=View.VISIBLE
                    }
                    namekoIdx[i]==3 -> {
                        namekoImg[i].setImageResource(R.drawable.nameko3)
                        namekoImg[i].visibility=View.VISIBLE
                    }
                    namekoIdx[i]==4 -> {
                        namekoImg[i].setImageResource(R.drawable.nameko4)
                        namekoImg[i].visibility=View.VISIBLE
                    }
                }
                var grow = AnimationUtils.loadAnimation(applicationContext, R.anim.growinganim)
                namekoImg[i].startAnimation(grow)
                Thread {
                    Thread.sleep(2500)
                    var alive =AnimationUtils.loadAnimation(baseContext, R.anim.alivemeko)
                    handler.post {
                        if (namekoIdx[i] != 0 && namekoImg[i].visibility==View.VISIBLE) {
                            namekoImg[i].startAnimation(alive)
                        }
                    }
                }.start()
            }
        }
        }

    override fun onPause() {
        super.onPause()
        val sp = getSharedPreferences("gauge_info", MODE_PRIVATE)
        val editor = sp.edit()
        //게이지 먹이 종류 백업
        if(option!= 0){
            editor.putInt("option",option)
        }
        if(currprog>=0){
            editor.putInt("currprog",currprog)
        }
        //np백업
        editor.putString("currNp",binding.currentNp.text.toString())
        //나메코 Idx 백업
        var tempArr:ArrayList<Int> = arrayListOf()
        for(i in 0 until namekoIdx.size){
            tempArr.add(namekoIdx[i])
        }
        var strContact = makeGson!!.toJson(tempArr,listType.type)
        editor.putString("namekoIdx",strContact)
        editor.putInt("humidifier",humidifier)
        editor.commit()
    }

    private fun tofromGrandpa() {
        val intent = Intent(this,GrandPaActivity::class.java)
        binding.topBtn1Img.setOnClickListener {
            intent.putExtra("Currentnp1",binding.currentNp.text.toString().toInt())
            startActivity(intent)
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
    //버섯 게이지 애니메이션
    private fun initRate() {
        if(rateArr100.isNotEmpty()) {
            rateArr100.clear()
        }
        //rateArr 확률 적용배열 clear하고, 확률배열 rateArr의 내용을 반영해서 초기화한다.
        for(i in 0 until rateArr[0]){
            rateArr100.add(1)
        }
        for(i in 0 until rateArr[1]){
            rateArr100.add(2)
        }
        for(i in 0 until rateArr[2]) {
            rateArr100.add(3)
        }
        for(i in 0 until rateArr[3]) {
            rateArr100.add(4)
        }
    }
    private fun mushAnim() {
        if (binding.progressbar.progress == 0) {
            binding.imgForAnim.visibility = View.INVISIBLE
            binding.progressOver.visibility = View.VISIBLE
        }
        Thread {
            handler.post {
                val drawable: AnimationDrawable? = binding.imgForAnim.drawable as AnimationDrawable
                drawable?.start()
            }
        }.start()
    }
    //보온기 애니메이션
    private fun warmAnim() {
        Thread {
            handler.post {
                val drawable: AnimationDrawable? = binding.warmer.drawable as AnimationDrawable
                drawable?.start()
            }
        }.start()
    }
    //조명기 애니메이션
    private fun lightAnim() {
        Thread {
            handler.post {
                val drawable: AnimationDrawable? = binding.light.drawable as AnimationDrawable
                drawable?.start()
            }
        }.start()
    }
    //먹이주기
    private fun feeding() {
        timertask = timerTask {//이 안에 timer가 동작할 내용 구성
            runOnUiThread { //progress bar는 ui이므로 runOnUiThread로 컨트롤해야함.
                currprog = binding.progressbar.progress
                if (currprog > 0) {
                    currprog -= 1
                } else if (currprog == 0) {
                    //먹이 시간 끝나면 전부 기본 버튼으로 세팅
                    binding.feed1.setImageResource(R.drawable.feed1)
                    binding.feed2.setImageResource(R.drawable.feed2)
                    binding.feed3.setImageResource(R.drawable.feed3)
                    binding.feed4.setImageResource(R.drawable.feed4)
                    binding.feed5.setImageResource(R.drawable.feed5)
                    binding.imgForAnim.visibility = View.INVISIBLE
                    binding.progressOver.visibility = View.VISIBLE
                    timer!!.cancel() //타이머 종료
                }
                //5분짜리 먹이를 주면 15초마다 한번씩 namekoIdx의 랜덤 위치에 1을 넣고,
                //namekoIdx를 for문을 돌려서 값이 1이면 해당 위치에 이미지 set
                if(option ==1){
                    var i:Int=0
                    if (binding.progressbar.progress in 1..299 && binding.progressbar.progress%(15-humidifier_timeBonus)==0){
                        var random = Random()
                        while(true) {
                            i = random.nextInt(namekoIdx.size)
                            if(num_nameko()==namekoIdx.size){
                                break
                            }
                            else if (namekoIdx[i] == 0) {
//                                namekoIdx[i] = 1
                                var j = random.nextInt(rateArr100.size)
                                namekoIdx[i] = rateArr100[j]
//                                Toast.makeText(baseContext, "rateArr의 ${j}번째 수인 ${rateArr100[j]} 들어옴.", Toast.LENGTH_SHORT).show()
                                break
                            }
                                else
                             {
                                continue
                            }
                        }
                    }
                    if((namekoIdx[i]==1||namekoIdx[i]==2||namekoIdx[i]==3||namekoIdx[i]==4) && namekoImg[i].visibility!=View.VISIBLE){
                        if(namekoIdx[i]==1) {
                            namekoImg[i].setImageResource(R.drawable.nameko)
                        }
                        else if(namekoIdx[i]==2){
                            namekoImg[i].setImageResource(R.drawable.nameko2)
                        }
                        else if(namekoIdx[i]==3){
                            namekoImg[i].setImageResource(R.drawable.nameko3)
                        }
                        else if(namekoIdx[i]==4){
                            namekoImg[i].setImageResource(R.drawable.nameko4)
                        }
                        namekoImg[i].visibility=View.VISIBLE

                        var grow = AnimationUtils.loadAnimation(applicationContext, R.anim.growinganim)
                        namekoImg[i].startAnimation(grow)
                        Thread {
                            Thread.sleep(2500)
                            var alive =AnimationUtils.loadAnimation(baseContext, R.anim.alivemeko)
                            handler.post {
                                if (namekoIdx[i] != 0 && namekoImg[i].visibility==View.VISIBLE) {
                                    namekoImg[i].startAnimation(alive)
                                }
                            }
                        }.start()
                    }
                }
                else if(option ==2){
                    var i:Int=0
                    if (binding.progressbar.progress in 1..599 && binding.progressbar.progress%(30-humidifier_timeBonus)==0){
                        var random = Random()
                        while(true) {
                            i = random.nextInt(namekoIdx.size)
                            if(num_nameko()==namekoIdx.size){
                                break
                            }
                            else if (namekoIdx[i] == 0) {
//                                namekoIdx[i] = 1
                                var j = random.nextInt(rateArr100.size)
                                namekoIdx[i] = rateArr100[j]

                                break
                            }
                            else
                            {
                                continue
                            }
                        }
                    }
                    if((namekoIdx[i]==1||namekoIdx[i]==2||namekoIdx[i]==3||namekoIdx[i]==4) && namekoImg[i].visibility!=View.VISIBLE){
                        if(namekoIdx[i]==1) {
                            namekoImg[i].setImageResource(R.drawable.nameko)
                        }
                        else if(namekoIdx[i]==2){
                            namekoImg[i].setImageResource(R.drawable.nameko2)
                        }
                        else if(namekoIdx[i]==3){
                            namekoImg[i].setImageResource(R.drawable.nameko3)
                        }
                        else if(namekoIdx[i]==4){
                            namekoImg[i].setImageResource(R.drawable.nameko4)
                        }
                        namekoImg[i].visibility=View.VISIBLE
                        var grow = AnimationUtils.loadAnimation(applicationContext, R.anim.growinganim)
                        namekoImg[i].startAnimation(grow)
                    }
                }
                else if(option ==3){
                    var i:Int=0
                    if (binding.progressbar.progress in 1..1199 && binding.progressbar.progress%(60-humidifier_timeBonus)==0){
                        var random = Random()
                        while(true) {
                            i = random.nextInt(namekoIdx.size)
                            if(num_nameko()==namekoIdx.size){
                                break
                            }
                            else if (namekoIdx[i] == 0) {
                                var j = random.nextInt(rateArr100.size)
                                namekoIdx[i] = rateArr100[j]

                                break
                            }
                            else
                            {
                                continue
                            }
                        }
                    }
                    if((namekoIdx[i]==1||namekoIdx[i]==2||namekoIdx[i]==3||namekoIdx[i]==4) && namekoImg[i].visibility!=View.VISIBLE){
                        if(namekoIdx[i]==1) {
                            namekoImg[i].setImageResource(R.drawable.nameko)
                        }
                        else if(namekoIdx[i]==2){
                            namekoImg[i].setImageResource(R.drawable.nameko2)
                        }
                        else if(namekoIdx[i]==3){
                            namekoImg[i].setImageResource(R.drawable.nameko3)
                        }
                        else if(namekoIdx[i]==4){
                            namekoImg[i].setImageResource(R.drawable.nameko4)
                        }
                        namekoImg[i].visibility=View.VISIBLE
                        var grow = AnimationUtils.loadAnimation(applicationContext, R.anim.growinganim)
                        namekoImg[i].startAnimation(grow)
                    }
                }
                else if(option ==4){
                    var i:Int=0
                    if (binding.progressbar.progress in 1..3599 && binding.progressbar.progress%(180-humidifier_timeBonus)==0){
                        var random = Random()
                        while(true) {
                            i = random.nextInt(namekoIdx.size)
                            if(num_nameko()==namekoIdx.size){
                                break
                            }
                            else if (namekoIdx[i] == 0) {
                                var j = random.nextInt(rateArr100.size)
                                namekoIdx[i] = rateArr100[j]

                                break
                            }
                            else
                            {
                                continue
                            }
                        }
                    }
                    if((namekoIdx[i]==1||namekoIdx[i]==2||namekoIdx[i]==3||namekoIdx[i]==4) && namekoImg[i].visibility!=View.VISIBLE){
                        if(namekoIdx[i]==1) {
                            namekoImg[i].setImageResource(R.drawable.nameko)
                        }
                        else if(namekoIdx[i]==2){
                            namekoImg[i].setImageResource(R.drawable.nameko2)
                        }
                        else if(namekoIdx[i]==3){
                            namekoImg[i].setImageResource(R.drawable.nameko3)
                        }
                        else if(namekoIdx[i]==4){
                            namekoImg[i].setImageResource(R.drawable.nameko4)
                        }
                        namekoImg[i].visibility=View.VISIBLE
                        var grow = AnimationUtils.loadAnimation(applicationContext, R.anim.growinganim)
                        namekoImg[i].startAnimation(grow)
                    }
                }
                else if(option ==5){
                    var i:Int=0
                    if (binding.progressbar.progress in 1..7199 && binding.progressbar.progress%(300-humidifier_timeBonus)==0){
                        var random = Random()
                        while(true) {
                            i = random.nextInt(namekoIdx.size)
                            if(num_nameko()==namekoIdx.size){
                                break
                            }
                            else if (namekoIdx[i] == 0) {
                                var j = random.nextInt(rateArr100.size)
                                namekoIdx[i] = rateArr100[j]

                                break
                            }
                            else
                            {
                                continue
                            }
                        }
                    }
                    if((namekoIdx[i]==1||namekoIdx[i]==2||namekoIdx[i]==3||namekoIdx[i]==4) && namekoImg[i].visibility!=View.VISIBLE){
                        if(namekoIdx[i]==1) {
                            namekoImg[i].setImageResource(R.drawable.nameko)
                        }
                        else if(namekoIdx[i]==2){
                            namekoImg[i].setImageResource(R.drawable.nameko2)
                        }
                        else if(namekoIdx[i]==3){
                            namekoImg[i].setImageResource(R.drawable.nameko3)
                        }
                        else if(namekoIdx[i]==4){
                            namekoImg[i].setImageResource(R.drawable.nameko4)
                        }
                        namekoImg[i].visibility=View.VISIBLE
                        var grow = AnimationUtils.loadAnimation(applicationContext, R.anim.growinganim)
                        namekoImg[i].startAnimation(grow)
                    }
                }
                binding.progressbar.progress = currprog //진행상황 적용
            }
        }
    }
    //5분짜리 먹이
    private fun feeding1() {
        if (timer != null) {
            timer!!.cancel() //타이머 종료
        }
        binding.currentNp.text = (binding.currentNp.text.toString().toInt() - 120).toString()
        binding.imgForAnim.visibility = View.VISIBLE
        binding.progressOver.visibility = View.INVISIBLE
        binding.progressbar.max = 300  //5분짜리 먹이
        binding.progressbar.progress = 300
        option = 1
        feeding()
        timer = Timer()
        timer!!.scheduleAtFixedRate(timertask, 0, 1000)
    }
    //10분짜리 먹이
    private fun feeding2() {
        if (timer != null) {
            timer!!.cancel() //타이머 종료
        }
        binding.currentNp.text = (binding.currentNp.text.toString().toInt() - 50).toString()
        binding.imgForAnim.visibility = View.VISIBLE
        binding.progressOver.visibility = View.INVISIBLE
        binding.progressbar.max = 600
        binding.progressbar.progress = 600
        option = 2
        feeding()
        timer = Timer()
        timer!!.scheduleAtFixedRate(timertask, 0, 1000)
    }
    //20분짜리 먹이
    private fun feeding3() {
        if (timer != null) {
            timer!!.cancel() //타이머 종료
        }
        binding.imgForAnim.visibility = View.VISIBLE
        binding.progressOver.visibility = View.INVISIBLE
        binding.progressbar.max = 1200
        binding.progressbar.progress = 1200
        option = 3
        feeding()
        timer = Timer()
        timer!!.scheduleAtFixedRate(timertask, 0, 1000)
    }
    //1시간짜리 먹이
    private fun feeding4() {
        if (timer != null) {
            timer!!.cancel() //타이머 종료
        }
        binding.currentNp.text = (binding.currentNp.text.toString().toInt() - 30).toString()
        binding.imgForAnim.visibility = View.VISIBLE
        binding.progressOver.visibility = View.INVISIBLE
        binding.progressbar.max = 3600
        binding.progressbar.progress = 3600
        option = 4
        feeding()
        timer = Timer()
        timer!!.scheduleAtFixedRate(timertask, 0, 1000)
    }
    //2시간짜리 먹이
    private fun feeding5() {
        if (timer != null) {
            timer!!.cancel() //타이머 종료
        }
        binding.currentNp.text = (binding.currentNp.text.toString().toInt() - 80).toString()
        binding.imgForAnim.visibility = View.VISIBLE
        binding.progressOver.visibility = View.INVISIBLE
        binding.progressbar.max = 7200
        binding.progressbar.progress = 7200
        option = 5
        feeding()
        timer = Timer()
        timer!!.scheduleAtFixedRate(timertask, 0, 1000)
    }
    //dialog 디자인 함수
    private fun alert1() {
        var alert = AlertDialog.Builder(this)
        alert.setView(R.layout.dialog01)
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
        val msg = dialog.findViewById<TextView>(R.id.messsag_dialog)
        val ybtn = dialog.findViewById<ImageButton>(R.id.choose_yes)
        val nbtn = dialog.findViewById<ImageButton>(R.id.choose_no)
        ybtn?.isPressed = false
        nbtn?.isPressed = false
        msg!!.text = "나메코 푸드 5분은\n120Np가 필요합니다.\n사용하시겠습니까?"
        ybtn!!.setOnClickListener {
            ybtn.isPressed = true
            if (binding.currentNp.text.toString().toInt() >= 120) {
                feeding1()
                //먹이버튼 누르면 버튼 색상 변경
                //다른 버튼들은 전부 끔.
                binding.feed1.setImageResource(R.drawable.feed1_on)
                binding.feed2.setImageResource(R.drawable.feed2)
                binding.feed3.setImageResource(R.drawable.feed3)
                binding.feed4.setImageResource(R.drawable.feed4)
                binding.feed5.setImageResource(R.drawable.feed5)
                dialog.dismiss()
            } else {
                dialog.dismiss()
                alertlack()
            }
        }
        nbtn!!.setOnClickListener {
            nbtn.isPressed = true
            dialog.dismiss()
        }
    }
    private fun alert2() {
        var alert = AlertDialog.Builder(this)
        alert.setView(R.layout.dialog01)
        var dialog = alert.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        (dialog.window!!.decorView as ViewGroup)
            .getChildAt(0).startAnimation(
                AnimationUtils.loadAnimation(
                    this, R.anim.open
                )
            )
        dialog.show()
        val msg = dialog.findViewById<TextView>(R.id.messsag_dialog)
        val ybtn = dialog.findViewById<ImageButton>(R.id.choose_yes)
        val nbtn = dialog.findViewById<ImageButton>(R.id.choose_no)
        msg!!.text = "나메코 푸드 10분은\n50Np가 필요합니다.\n사용하시겠습니까?"
        ybtn!!.setOnClickListener {
            if (binding.currentNp.text.toString().toInt() >= 50) {
                feeding2()
                binding.feed1.setImageResource(R.drawable.feed1)
                binding.feed2.setImageResource(R.drawable.feed2_on)
                binding.feed3.setImageResource(R.drawable.feed3)
                binding.feed4.setImageResource(R.drawable.feed4)
                binding.feed5.setImageResource(R.drawable.feed5)
                dialog.dismiss()
            } else {
                dialog.dismiss()
                alertlack()
            }
        }
        nbtn!!.setOnClickListener {
            dialog.dismiss()
        }
    }
    private fun alert3() {
        var alert = AlertDialog.Builder(this)
        alert.setView(R.layout.dialog01)
        var dialog = alert.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        (dialog.window!!.decorView as ViewGroup)
            .getChildAt(0).startAnimation(
                AnimationUtils.loadAnimation(
                    this, R.anim.open
                )
            )
        dialog.show()
        val msg = dialog.findViewById<TextView>(R.id.messsag_dialog)
        val ybtn = dialog.findViewById<ImageButton>(R.id.choose_yes)
        val nbtn = dialog.findViewById<ImageButton>(R.id.choose_no)
        msg!!.text = "나메코 푸드 20분은\nNp가 필요없습니다.\n사용하시겠습니까?"
        ybtn!!.setOnClickListener {
            feeding3()
            binding.feed1.setImageResource(R.drawable.feed1)
            binding.feed2.setImageResource(R.drawable.feed2)
            binding.feed3.setImageResource(R.drawable.feed3_on)
            binding.feed4.setImageResource(R.drawable.feed4)
            binding.feed5.setImageResource(R.drawable.feed5)
            dialog.dismiss()
        }
        nbtn!!.setOnClickListener {
            dialog.dismiss()
        }
    }
    private fun alert4() {
        var alert = AlertDialog.Builder(this)
        alert.setView(R.layout.dialog01)
        var dialog = alert.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        (dialog.window!!.decorView as ViewGroup)
            .getChildAt(0).startAnimation(
                AnimationUtils.loadAnimation(
                    this, R.anim.open
                )
            )
        dialog.show()
        val msg = dialog.findViewById<TextView>(R.id.messsag_dialog)
        val ybtn = dialog.findViewById<ImageButton>(R.id.choose_yes)
        val nbtn = dialog.findViewById<ImageButton>(R.id.choose_no)
        msg!!.text = "나메코 푸드 1시간은\n30Np가 필요합니다.\n사용하시겠습니까?"
        ybtn!!.setOnClickListener {
            if (binding.currentNp.text.toString().toInt() >= 30) {
                feeding4()
                binding.feed1.setImageResource(R.drawable.feed1)
                binding.feed2.setImageResource(R.drawable.feed2)
                binding.feed3.setImageResource(R.drawable.feed3)
                binding.feed4.setImageResource(R.drawable.feed4_on)
                binding.feed5.setImageResource(R.drawable.feed5)
                dialog.dismiss()
            } else {
                dialog.dismiss()
                alertlack()
            }
        }
        nbtn!!.setOnClickListener {
            dialog.dismiss()
        }
    }
    private fun alert5() {
        var alert = AlertDialog.Builder(this)
        alert.setView(R.layout.dialog01)
        var dialog = alert.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        (dialog.window!!.decorView as ViewGroup)
            .getChildAt(0).startAnimation(
                AnimationUtils.loadAnimation(
                    this, R.anim.open
                )
            )
        dialog.show()
        val msg = dialog.findViewById<TextView>(R.id.messsag_dialog)
        val ybtn = dialog.findViewById<ImageButton>(R.id.choose_yes)
        val nbtn = dialog.findViewById<ImageButton>(R.id.choose_no)
        msg!!.text = "나메코 푸드 2시간은\n80Np가 필요합니다.\n사용하시겠습니까?"
        ybtn!!.setOnClickListener {
            if (binding.currentNp.text.toString().toInt() >= 80) {
                feeding5()
                binding.feed1.setImageResource(R.drawable.feed1)
                binding.feed2.setImageResource(R.drawable.feed2)
                binding.feed3.setImageResource(R.drawable.feed3)
                binding.feed4.setImageResource(R.drawable.feed4)
                binding.feed5.setImageResource(R.drawable.feed5_on)
                dialog.dismiss()
            } else {
                dialog.dismiss()
                alertlack()
            }
        }
        nbtn!!.setOnClickListener {
            dialog.dismiss()
        }
    }
    //np부족
    private fun alertlack() {
        var alertlack = AlertDialog.Builder(this)
        alertlack.setView(R.layout.dialog_lack)
        var dialog = alertlack.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        (dialog.window!!.decorView as ViewGroup)
            .getChildAt(0).startAnimation(
                AnimationUtils.loadAnimation(
                    this, R.anim.open
                )
            )
        dialog.show()
        val msg = dialog.findViewById<TextView>(R.id.messsag_dialog_lack)
        val okbtn = dialog.findViewById<ImageButton>(R.id.choose_ok)
        msg!!.text = "Np가 부족한 것 같군요.."
        okbtn?.isPressed = false
        okbtn!!.setOnClickListener {
            okbtn.isPressed = true
            dialog.dismiss()
        }
    }
    //feeding함수 set
    private fun feedSet() {
        binding.feed1.setOnClickListener {
            alert1()
        }
        binding.feed2.setOnClickListener {
            alert2()
        }
        binding.feed3.setOnClickListener {
            alert3()
        }
        binding.feed4.setOnClickListener {
            alert4()
        }
        binding.feed5.setOnClickListener {
            alert5()
        }
    }
    //현재 나메코 개수 반환
    private fun num_nameko():Int{
        var count = 0
        for (i in namekoIdx){
            if(i!=0){
                count+=1
            }
        }
        return count
    }
    //나메코 수확 (여기에 애니메이션 넣어야함.)
    private fun harvest(){
        for(i in 0 until namekoImg.size){
            namekoImg[i].setOnTouchListener { v, event ->
                when (event?.action) {
                    //터치되고 있는 동안-ACTION_MOVE , 터치를 누르는 순간-ACTION_DOWN
                    MotionEvent.ACTION_DOWN -> {
                        //No.1 나메코 수확 시 6원, No.2 나메코 수확시 20원 수익 얻고, 이미지뷰 사라지게 만듦
                        if(namekoIdx[i]==1) {
                            val np = binding.currentNp.text.toString().toInt() + 6
                            binding.currentNp.text = np.toString()
                            namekoIdx[i] = 0
                            namekoImg[i].visibility = View.GONE
                            //늘어나는 애니메이션
                            //발달린 나메코로 변경
                            //위로 이동, 좌로 이동, 박스 이동 애니메이션
                            namekoImg[i].clearAnimation() //alive nameko 애니메이션 clear
                        }
                        else if(namekoIdx[i]==2) {
                            val np = binding.currentNp.text.toString().toInt() + 20
                            binding.currentNp.text = np.toString()
                            namekoIdx[i] = 0
                            namekoImg[i].visibility = View.GONE
                            namekoImg[i].clearAnimation()
                        }
                        else if(namekoIdx[i]==3) {
                            val np = binding.currentNp.text.toString().toInt() + 40
                            binding.currentNp.text = np.toString()
                            namekoIdx[i] = 0
                            namekoImg[i].visibility = View.GONE
                            namekoImg[i].clearAnimation()
                        }
                        else if(namekoIdx[i]==4) {
                            val np = binding.currentNp.text.toString().toInt() + 65
                            binding.currentNp.text = np.toString()
                            namekoIdx[i] = 0
                            namekoImg[i].visibility = View.GONE
                            namekoImg[i].clearAnimation()
                        }
                        //각 else if마다 사라지는 애니메이션 적용해야함.
                        //인덱스 배열도 0으로 만들고, 이미지도 Gone으로 만들어야함.
                        //사라질 때에는 발 달린 나메코로 바뀌게
                        //namekoImg[i].setImageResource(R.drawable.nameko_foot)
                    }
                }

                //리턴값이 false면 이미지뷰 터치 동작 안됨
                true //or false
            }
        }
    }
    //가습기 상태에 따른 보너스
    private fun humBonus(){
        //가습기 상태 받아서 이미지뷰 바꾸고
        humidifier = intent.getIntExtra("selectedHum",0)
        if(humidifier==1){
            binding.humidifier.setImageResource(R.drawable.humidifier_1)
        }
        else if(humidifier==2){
            binding.humidifier.setImageResource(R.drawable.humidifier_2)
            humidifier_timeBonus=7
        }

    }

}


