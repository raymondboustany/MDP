package com.example.ifryoriginal

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.webkit.WebHistoryItem
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.os.postDelayed
import com.example.ifryoriginal.util.On1
import com.example.ifryoriginal.util.PrefUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import java.util.PropertyResourceBundle



class MainActivity : AppCompatActivity() {










        companion object {
            fun setAlarm(context: Context, nowSeconds: Long, secondsRemaining: Long): Long{
                val wakeUpTime = (nowSeconds + secondsRemaining) * 1000
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(context, TimerExpiredReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeUpTime, pendingIntent)
                PrefUtil.setAlarmSetTime(nowSeconds, context)
                return wakeUpTime
            }

            fun removeAlarm(context: Context){
                val intent = Intent(context, TimerExpiredReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.cancel(pendingIntent)
                PrefUtil.setAlarmSetTime(0, context)
            }

            val nowSeconds: Long
                get() = Calendar.getInstance().timeInMillis / 1000
        }

        enum class TimerState{
            Stopped, Paused, Running
        }







    private lateinit var Timer_text: TextView
    private lateinit var button_Start: Button
    private lateinit var button_Stop: Button
    private lateinit var button_Pause: Button

    private lateinit var Welcome_text: TextView
    private lateinit var Stage_text: TextView
    private lateinit var Temp_text: TextView
    private lateinit var Stage_value: TextView
    private lateinit var Temp_value: TextView
    private lateinit var Timevalue: TextView
    private lateinit var weightText : TextView
    private lateinit var weightValue : TextView
    private lateinit var InsertAnother : TextView
    private var weightPara = 0
    private lateinit var Hdiv: View
    private lateinit var Vdiv: View
    val handler = Handler()
        private lateinit var timer: CountDownTimer
        private var timerLengthSeconds: Long =0
        private var timerState = TimerState.Stopped

        private var secondsRemaining: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        PrefUtil.setTimerState(timerState, this)
        PrefUtil.setTimerLength(1, this)


        Timer_text = findViewById(R.id.Time_value)
        button_Start = findViewById(R.id.buttonStart)
        button_Stop = findViewById(R.id.buttonStop)
        button_Pause = findViewById(R.id.buttonPause)
        weightText = findViewById(R.id.weight_text)
        weightValue = findViewById(R.id.weight_value)
        Welcome_text = findViewById(R.id.welcoming)
        Stage_text = findViewById(R.id.stage)
        Temp_text = findViewById(R.id.temperature)
        Stage_value = findViewById(R.id.stage_value)
        Temp_value = findViewById(R.id.temperature_value)
        Timevalue = findViewById(R.id.Time_value)
        InsertAnother = findViewById(R.id.insert_another)
        Hdiv = findViewById(R.id.divider_horizontal)
        //Vdiv = findViewById(R.id.divider_vertical)



        val delayMillis = 3000L
        button_Start.setOnClickListener {





            handler.postDelayed({
                startTimer()

                weightValue.text = weightPara.toString()+"g"
                Stage_value.text = "Washing,\nPeeling,\nCutting"
                Temp_value.text = "25 C"

            }, delayMillis)

            Welcome_text.alpha = 0f
            Stage_text.alpha = 1f
            Temp_text.alpha = 1f
            Stage_value.alpha = 1f
            Temp_value.alpha = 1f
            Timevalue.alpha = 1f
            button_Pause.alpha = 1f
            button_Stop.alpha = 1f
            weightText.alpha = 1f
            weightValue.alpha = 1f

            Hdiv.alpha = 1f
            //Vdiv.alpha = 1f

            timerState = TimerState.Running
            updateButtons()
        }




        button_Pause.setOnClickListener {
            timer.cancel()
            timerState = TimerState.Paused
            updateButtons()
        }

        button_Stop.setOnClickListener {
            timer.cancel()
            onTimerFinished()
        }
    }

    override fun onResume() {
        super.onResume()

        initTimer()

        removeAlarm(this)


    }

    override fun onPause() {
        super.onPause()

        if (timerState == TimerState.Running){
            timer.cancel()
            val wakeUpTime = setAlarm(this, nowSeconds, secondsRemaining)
            //TODO: show notification
        }
        else if (timerState == TimerState.Paused){
            //TODO: show notification
        }

        PrefUtil.setPreviousTimerLengthSeconds(timerLengthSeconds, this)
        PrefUtil.setSecondsRemaining(secondsRemaining, this)
        PrefUtil.setTimerState(timerState, this)
    }

    private fun initTimer(){
        timerState = PrefUtil.getTimerState(this)

        //we don't want to change the length of the timer which is already running
        //if the length was changed in settings while it was backgrounded
        if (timerState == TimerState.Stopped)
            setNewTimerLength()
        else
            setPreviousTimerLength()

        secondsRemaining = if (timerState == TimerState.Running || timerState == TimerState.Paused)
            PrefUtil.getSecondsRemaining(this)
        else
            timerLengthSeconds

        val alarmSetTime = PrefUtil.getAlarmSetTime(this)
        if (alarmSetTime > 0)
            secondsRemaining -= nowSeconds - alarmSetTime

        if (secondsRemaining <= 0)
          onTimerFinished()

        else if (timerState == TimerState.Running)
            startTimer()

        updateButtons()
        updateCountdownUI()
    }

    private fun onTimerFinished() {
        timerState = TimerState.Stopped

        //set the length of the timer to be the one set in SettingsActivity
        //if the length was changed when the timer was running
        setNewTimerLength()



        PrefUtil.setSecondsRemaining(timerLengthSeconds, this)
        secondsRemaining = timerLengthSeconds

        updateButtons()
        updateCountdownUI()
    }


    private fun startTimer() {
        timerState = TimerState.Running
        updateButtons()
        CoroutineScope(Dispatchers.IO).launch {
            val url = "http://172.20.10.4/led1on"
            try {
                val response = URL(url).readText()
                Log.d("TAG", "Response from server: $response")
            } catch (e: Exception) {
                Log.e("TAG", "Error: ${e.message}")
            }
        }



        timer = object : CountDownTimer(secondsRemaining * 1000, 1000) {
            override fun onFinish(){
                CoroutineScope(Dispatchers.IO).launch {
                    val url = "http://172.20.10.4/led1off"
                    try {
                        val response = URL(url).readText()
                        Log.d("TAG", "Response from server: $response")
                    } catch (e: Exception) {
                        Log.e("TAG", "Error: ${e.message}")
                    }
                }

                weightPara += 200
                weightValue.text = weightPara.toString() +"g"
                if (weightPara !=1000){

                    onTimerFinished()
                    InsertAnother.alpha = 1f
                    Welcome_text.alpha = 0f
                    Stage_text.alpha = 0f
                    Temp_text.alpha = 0f
                    Stage_value.alpha = 0f
                    Temp_value.alpha = 0f
                    Timevalue.alpha = 0f
                    button_Start.text = "Continue"
                    button_Pause.alpha = 0f
                    button_Stop.alpha = 0f
                    weightText.alpha = 1f
                    weightValue.alpha = 1f

                    Hdiv.alpha = 0f
                    //Vdiv.alpha = 0f

                    button_Start.setOnClickListener{
                        startTimer()
                        InsertAnother.alpha = 0f
                        Welcome_text.alpha = 0f
                        Stage_text.alpha = 1f
                        Temp_text.alpha = 1f
                        Stage_value.alpha = 1f
                        Temp_value.alpha = 1f
                        Timevalue.alpha = 1f
                        button_Start.text = "Continue"
                        button_Pause.alpha = 1f
                        button_Stop.alpha = 1f
                        weightText.alpha = 1f
                        weightValue.alpha = 1f
                        Hdiv.alpha = 1f
                        //Vdiv.alpha = 1f

                        timerState = TimerState.Running
                        updateButtons()
                    }

                }else {
                    timerState = TimerState.Running
                    updateButtons()
                    InsertAnother.text = "1000g of potato inserted\nfrying will begin"
                    InsertAnother.alpha = 1f

                    Stage_text.alpha = 0f
                    Temp_text.alpha = 0f
                    Stage_value.alpha = 0f
                    Temp_value.alpha = 0f
                    Timevalue.alpha = 0f
                    button_Start.text = "Continue"
                    button_Pause.alpha = 0f
                    button_Stop.alpha = 0f
                    weightText.alpha = 1f
                    weightValue.alpha = 1f
                    button_Start.text="Please\nWait"
                    Hdiv.alpha = 0f
                    //Vdiv.alpha = 0f




                    handler.postDelayed({
                        Stage_value.text = "..."
                        Stage_value.alpha = 1f
                        Stage_text.alpha = 1f
                        Temp_value.text = "50 C"
                        Temp_value.alpha = 1f
                        Temp_text.alpha = 1f
                        Timer_text.alpha =1f
                        Timevalue.alpha = 1f
                        InsertAnother.alpha = 0f
                        weightText.alpha = 1f
                        weightValue.alpha = 1f
                        //Vdiv.alpha = 1f
                        Hdiv.alpha = 1f


                    },5000)



                    handler.postDelayed({
                        startTimer2()

                        Stage_value.text = "Frying"
                        Temp_value.text = "180 C"
                        button_Pause.alpha = 0f
                        button_Stop.alpha = 0f
                        button_Pause.isEnabled = false
                        button_Stop.isEnabled = false

                    }, 5000)
                }
            }

            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000
                updateCountdownUI()
            }
        }.start()


    }

    private fun startTimer2() {
        val intent2 = Intent(this, DoneActivity::class.java)
        timerState = TimerState.Running
        updateButtons()
        PrefUtil.setTimerLength(1,this)
        secondsRemaining = 60


        CoroutineScope(Dispatchers.IO).launch {
            val url = "http://172.20.10.4/led2on"
            try {
                val response = URL(url).readText()
                Log.d("TAG", "Response from server: $response")
            } catch (e: Exception) {
                Log.e("TAG", "Error: ${e.message}")
            }
        }


        timer = object : CountDownTimer(secondsRemaining * 1000, 1000) {
            override fun onFinish(){
                onTimerFinished()
                CoroutineScope(Dispatchers.IO).launch {
                    val url = "http://172.20.10.4/led2off"
                    try {
                        val response = URL(url).readText()
                        Log.d("TAG", "Response from server: $response")
                    } catch (e: Exception) {
                        Log.e("TAG", "Error: ${e.message}")
                    }
                }
                startActivity(intent2)

            }

            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000
                updateCountdownUI()
            }
        }.start()


    }


    private fun setNewTimerLength() {
        val lengthInMinutes = PrefUtil.getTimerLength(this)
        timerLengthSeconds = (lengthInMinutes * 60L)
    }

    private fun setPreviousTimerLength() {
        timerLengthSeconds = PrefUtil.getPreviousTimerLengthSeconds(this)
    }


    private fun updateCountdownUI() {
        val minutesUntilFinished = secondsRemaining / 60
        val secondsInMinuteUntilFinished = secondsRemaining - minutesUntilFinished * 60
        val secondsStr = secondsInMinuteUntilFinished.toString()
        Timer_text.text = "$minutesUntilFinished:${if (secondsStr.length == 2) secondsStr else "0" + secondsStr}"

    }

    private fun updateButtons(){
        when (timerState) {
            TimerState.Running ->{
                button_Start.isEnabled = false
                button_Pause.isEnabled = true
                button_Stop.isEnabled = true
            }
            TimerState.Stopped -> {
                button_Start.isEnabled = true
                button_Pause.isEnabled = false
                button_Stop.isEnabled = false
            }
            TimerState.Paused -> {
                button_Start.isEnabled = true
                button_Pause.isEnabled = false
                button_Stop.isEnabled = true
            }
        }
    }

}