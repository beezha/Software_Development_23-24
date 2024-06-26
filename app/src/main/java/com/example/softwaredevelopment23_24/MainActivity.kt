package com.example.softwaredevelopment23_24

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.softwaredevelopment23_24.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import java.util.Calendar

const val THIRST_DIVISOR: Int = 2
const val ENJOYMENT_DIVISOR: Int = 3
const val HUNGER_DIVISOR: Int = 5
const val PET_STATUS_CHANNEL_ID = "PET_STATUS_CHANNEL"
const val DAILY_REMINDER_CHANNEL_ID = "DAILY_REMINDER_CHANNEL"

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var reference: DatabaseReference
    private lateinit var user: FirebaseAuth
    private var petHunger: Int = 50
    private var petThirst: Int = 50
    private var petEnjoyment: Int = 50

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment)


        navView.setupWithNavController(navController)

        createNotificationChannels()

        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navController.popBackStack(R.id.navigation_home, false)
                    true
                }
                R.id.navigation_calendar -> {
                    navController.navigate(R.id.navigation_calendar)
                    true
                }
                R.id.navigation_pet -> {
                    navController.navigate(R.id.navigation_pet)
                    true
                }
                R.id.navigation_chat -> {
                    navController.navigate(R.id.navigation_chat)
                    true
                }
                R.id.navigation_settings -> {
                    navController.navigate(R.id.navigation_settings)
                    true
                }
                else -> false
            }
        }

        user = FirebaseAuth.getInstance()

        if (user.currentUser != null) {
            val userID = user.currentUser!!.uid
            reference = FirebaseDatabase.getInstance().reference.child("users").child(userID)
            updateLoginDay(reference, this) {
                updateLoginTime(reference, this)
                resetStreak(reference, this)
                navController.navigate(R.id.navigation_home)
            }
        } else {
            navController.navigate(R.id.navigation_login)
        }

        generateStats(reference, this) { hunger, thirst, enjoyment, _ ->
            petHunger = hunger
            petThirst = thirst
            petEnjoyment = enjoyment

            getPetName(reference) { petName ->
                val nameText = petName ?: "Your pet"


                if (petHunger <= 0) {
                    sendNotification(
                        this,
                        PET_STATUS_CHANNEL_ID,
                        "Your pet needs attention!",
                        "$nameText is hungry \uD83D\uDE16"
                    )
                }

                if (petThirst <= 0) {
                    sendNotification(
                        this,
                        PET_STATUS_CHANNEL_ID,
                        "Time for pet care duties!",
                        "$nameText is thirsty \uD83E\uDD75"
                    )
                }

                if (petEnjoyment <= 0) {
                    sendNotification(
                        this,
                        PET_STATUS_CHANNEL_ID,
                        "Your pet misses you",
                        "$nameText is bored \uD83D\uDE2A"
                    )
                }
            }
        }
        val midnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, 1) // Next day
        }.timeInMillis

        val currentTime = System.currentTimeMillis()
        val timeUntilMidnight = midnight - currentTime
        val timeBeforeMidnight = timeUntilMidnight - (60 * 60 * 1000) // One hour before midnight

        val reminderTime = currentTime + timeBeforeMidnight

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)

        sendNotification(this, DAILY_REMINDER_CHANNEL_ID, "It's almost midnight!", "Complete a task to continue your streak!")
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    PET_STATUS_CHANNEL_ID,
                    getString(R.string.channel_pet_status_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = getString(R.string.channel_pet_status_description)
                },
                NotificationChannel(
                    DAILY_REMINDER_CHANNEL_ID,
                    getString(R.string.channel_daily_reminder_name),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = getString(R.string.channel_daily_reminder_description)
                }
            )

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    private fun sendNotification(context: Context, channelId: String, title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.notification_sound}")

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(soundUri)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Request the missing permissions
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1
            )
            return
        }


        with(NotificationManagerCompat.from(context)) {
            notify(System.currentTimeMillis().toInt(), builder.build()) // Unique notification ID
        }
    }

    fun generateDatabase(reference: DatabaseReference, userID: String, username: String, email: String, context: Context, onComplete: () -> Unit) {
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        val currentTime = System.currentTimeMillis()
        val userData = hashMapOf(
            "username" to username,
            "email" to email,
            "task1" to true,
            "task2" to true,
            "task3" to true,
            "task4" to true,
            "task5" to true,
            "task6" to true,
            "task7" to true,
            "task8" to true,
            "petHunger" to 50,
            "petThirst" to 50,
            "petEnjoyment" to 50,
            "coins" to 20,
            "loginTime" to currentTime,
            "loginDay" to currentDay,
            "petName" to "Pet",
            "taskStatus1" to false,
            "taskStatus2" to false,
            "taskStatus3" to false,
            "taskStatus4" to false,
            "taskStatus5" to false,
            "taskStatus6" to false,
            "taskStatus7" to false,
            "taskStatus8" to false,
            "task1Progress" to 0,
            "task2Progress" to 0,
            "task3Progress" to 0,
            "dayStreak1" to false,
            "dayStreak2" to false,
            "dayStreak3" to false,
            "dayStreak4" to false,
            "dayStreak5" to false,
            "dayStreak6" to false,
            "dayStreak7" to false,
            "dayStreak8" to false,
            "dayStreak9" to false,
            "dayStreak10" to false,
            "dayStreak11" to false,
            "dayStreak12" to false,
            "dayStreak13" to false,
            "dayStreak14" to false,
            "dayStreak15" to false,
            "dayStreak16" to false,
            "dayStreak17" to false,
            "dayStreak18" to false,
            "dayStreak19" to false,
            "dayStreak20" to false,
            "dayStreak21" to false,
            "dayStreak22" to false,
            "dayStreak23" to false,
            "dayStreak24" to false,
            "dayStreak25" to false,
            "dayStreak26" to false,
            "dayStreak27" to false,
            "dayStreak28" to false,
            "dayStreak29" to false,
            "dayStreak30" to false,
            "dayStreak31" to false,
            "loginMonth" to currentMonth,
            "avatar" to 1
        )
        reference.child("users").child(userID).setValue(userData)
            .addOnSuccessListener { onComplete.invoke() }
            .addOnFailureListener {
                Toast.makeText(
                    context,
                    "Failed to add user to database. ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    fun generateStats(
        reference: DatabaseReference,
        context: Context,
        callback: (petHunger: Int, petThirst: Int, petEnjoyment: Int, userCoins: Int) -> Unit
    ) {
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val petThirst = (snapshot.child("petThirst").getValue(Int::class.java) ?: 0)
                val petHunger = (snapshot.child("petHunger").getValue(Int::class.java) ?: 0)
                val petEnjoyment = (snapshot.child("petEnjoyment").getValue(Int::class.java) ?: 0)
                val coins = snapshot.child("coins").getValue(Int::class.java) ?: 0
                callback(petHunger, petThirst, petEnjoyment, coins)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    context,
                    "Could not read data from database: ${error.toException()}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    fun updateLoginTime(reference: DatabaseReference, context: Context) {
        val currentTime = System.currentTimeMillis()
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.hasChild("loginTime")) {
                    val loginTime = snapshot.child("loginTime").getValue(Long::class.java)
                    val timeDifference = (currentTime - loginTime!!) / 60000
                    val hungerDepreciation = timeDifference / HUNGER_DIVISOR
                    val thirstDepreciation = timeDifference / THIRST_DIVISOR
                    val enjoymentDepreciation = timeDifference / ENJOYMENT_DIVISOR

                    val petHunger = (snapshot.child("petHunger").value as Long - hungerDepreciation).toInt()
                    val petThirst = (snapshot.child("petThirst").value as Long - thirstDepreciation).toInt()
                    val petEnjoyment = (snapshot.child("petEnjoyment").value as Long - enjoymentDepreciation).toInt()

                    val timeData = hashMapOf(
                        "loginTime" to currentTime,
                        "petHunger" to maxOf(0, petHunger),
                        "petThirst" to maxOf(0, petThirst),
                        "petEnjoyment" to maxOf(0, petEnjoyment),
                    )

                    reference.updateChildren(timeData as Map<String, Any>)
                        .addOnCompleteListener {
                            if (!it.isSuccessful) {
                                Toast.makeText(
                                    context,
                                    "Failed to update login time",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(
                        context,
                        "Login time not found",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    context,
                    "Error reading login time ${error.toException()}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    fun getPetName(reference: DatabaseReference, callback: (String?) -> Unit) {
        reference.child("petName").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val petName = snapshot.getValue(String::class.java)
                callback(petName)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
    }

    fun getTaskPreferences(
        reference: DatabaseReference,
        context: Context,
        callback: (taskPreferences: List<Any?>, taskCompleteList: List<Boolean>) -> Unit
    ) {
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val task1 = snapshot.child("task1").value
                val task2 = snapshot.child("task2").value
                val task3 = snapshot.child("task3").value
                val task4 = snapshot.child("task4").value
                val task5 = snapshot.child("task5").value
                val task6 = snapshot.child("task6").value
                val task7 = snapshot.child("task7").value
                val task8 = snapshot.child("task8").value

                val taskStatus1 = snapshot.child("taskStatus1").getValue(Boolean::class.java) ?: false
                val taskStatus2 = snapshot.child("taskStatus2").getValue(Boolean::class.java) ?: false
                val taskStatus3 = snapshot.child("taskStatus3").getValue(Boolean::class.java) ?: false
                val taskStatus4 = snapshot.child("taskStatus4").getValue(Boolean::class.java) ?: false
                val taskStatus5 = snapshot.child("taskStatus5").getValue(Boolean::class.java) ?: false
                val taskStatus6 = snapshot.child("taskStatus6").getValue(Boolean::class.java) ?: false
                val taskStatus7 = snapshot.child("taskStatus7").getValue(Boolean::class.java) ?: false
                val taskStatus8 = snapshot.child("taskStatus8").getValue(Boolean::class.java) ?: false

                val taskPreferences = listOf(task1, task2, task3, task4, task5, task6, task7, task8)
                val taskCompleteList = listOf(taskStatus1, taskStatus2, taskStatus3, taskStatus4, taskStatus5, taskStatus6, taskStatus7, taskStatus8)

                callback(taskPreferences, taskCompleteList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    context,
                    "Could not find task preferences",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    fun getCoins(
        reference: DatabaseReference,
        context: Context,
        callback: (coins: Int) -> Unit
    ) {
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val coins = snapshot.child("coins").getValue<Int>()
                if (coins != null) {
                    callback(coins)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    context,
                    "Could not get user coin amount",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    fun getTaskProgress(
        reference: DatabaseReference,
        context: Context,
        taskIndex: Int,
        callback: (taskProgress: Int) -> Unit
    ) {
        var taskProgress: Int
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                taskProgress = when (taskIndex) {
                    1 -> snapshot.child("task1Progress").getValue<Int>()!!
                    2 -> snapshot.child("task2Progress").getValue<Int>()!!
                    3 -> snapshot.child("task3Progress").getValue<Int>()!!
                    else -> 0
                }
                callback(taskProgress)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    context,
                    "Could not find task progress. Please try again. ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    fun updateLoginDay(reference: DatabaseReference, context: Context, onComplete: () -> Unit) {
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH).toLong()
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lastLoginDay = snapshot.child("loginDay").getValue<Long>()
                val lastLoginTime = snapshot.child("loginTime").getValue<Long>()
                if (lastLoginTime == null || lastLoginDay == null || !isSameDay(lastLoginTime, lastLoginDay, currentDay)) {
                    val newValues = hashMapOf(
                        "taskStatus1" to false,
                        "taskStatus2" to false,
                        "taskStatus3" to false,
                        "taskStatus4" to false,
                        "taskStatus5" to false,
                        "taskStatus6" to false,
                        "taskStatus7" to false,
                        "taskStatus8" to false,
                        "task1Progress" to 0,
                        "task2Progress" to 0,
                        "task3Progress" to 0,
                        "loginDay" to currentDay
                    )
                    reference.updateChildren(newValues as Map<String, Any>)
                        .addOnCompleteListener { onComplete.invoke() }
                } else {
                    onComplete.invoke()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    context,
                    "Could not retrieve current day",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun isSameDay(lastLoginTime: Long, lastLoginDay: Long, currentDay: Long): Boolean {
        val currentTime = System.currentTimeMillis()

        return if (currentDay != lastLoginDay) {
            false
        } else if (currentTime - lastLoginTime >= 86400000L) {
            false
        } else {
            true
        }
    }

    fun getStreak(reference: DatabaseReference, context: Context, callback: (MutableList<Boolean>) -> Unit) {
        reference.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val streak: MutableList<Boolean> = mutableListOf()
                for (i in 1..31) {
                    val dayStatus = snapshot.child("dayStreak$i").getValue(Boolean::class.java) ?: false
                    streak.add(dayStatus)
                }
                callback(streak)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("error", "Could not get database values for day streak")
                Toast.makeText(
                    context,
                    "Could not get streak data",
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }

    fun resetStreak(reference: DatabaseReference, context: Context) {
        val currentMonth = (Calendar.getInstance().get(Calendar.MONTH) + 1).toLong()
        reference.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lastLoginMonth = snapshot.child("loginMonth").getValue<Long>()
                val lastLoginTime = snapshot.child("loginTime").getValue<Long>()
                if (lastLoginMonth == null || !isSameMonth(lastLoginTime, lastLoginMonth, currentMonth)) {
                    val newValues = hashMapOf<String, Any>()
                    for (i in 1..31) {
                        newValues["dayStreak$i"] = false
                    }
                    newValues["loginMonth"] = currentMonth
                    reference.updateChildren(newValues)
                        .addOnCompleteListener {
                            if (!it.isSuccessful) {
                                Toast.makeText(
                                    context,
                                    "Could not update values",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    context,
                    "Could not retrieve last login month.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }

    private fun isSameMonth(lastLoginTime: Long?, lastLoginMonth: Long?, currentMonth: Long): Boolean {
        return if (lastLoginMonth != null && lastLoginTime != null) {
            val lastLoginCalendar = Calendar.getInstance()
            lastLoginCalendar.timeInMillis = lastLoginTime
            val lastLoginMonthInCalendar = lastLoginCalendar.get(Calendar.MONTH) + 1
            lastLoginMonthInCalendar == currentMonth.toInt()
        } else {
            false
        }
    }

    fun getAvatar(
        reference: DatabaseReference,
        context: Context,
        view: View,
        callback: (Drawable?) -> Unit) {
        reference.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val avatarInt = snapshot.child("avatar").getValue<Int>()
                val avatarView: ImageView? = when (avatarInt) {
                    1 -> view.findViewById(R.id.avatar1)
                    2 -> view.findViewById(R.id.avatar2)
                    3 -> view.findViewById(R.id.avatar3)
                    4 -> view.findViewById(R.id.avatar4)
                    5 -> view.findViewById(R.id.avatar5)
                    else -> null
                }
                val avatarImage = avatarView?.background
                callback(avatarImage)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    context,
                    "Could not find avatar",
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }
}