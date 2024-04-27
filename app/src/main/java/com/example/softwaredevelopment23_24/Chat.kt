package com.example.softwaredevelopment23_24

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.example.softwaredevelopment23_24.databinding.FragmentChatBinding
import android.os.Handler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class Chat : Fragment() {
    private val client = OkHttpClient()
    private val apiKey = "api here"
    private val url = "https://api.openai.com/v1/chat/completions"
    private lateinit var txtMessage: TextView
    private lateinit var binding: FragmentChatBinding
    private lateinit var user: FirebaseUser
    private lateinit var reference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.avatar_chooser_box, container, false)
        binding = FragmentChatBinding.inflate(inflater, container, false)
        txtMessage = binding.txtResponse
        user = FirebaseAuth.getInstance().currentUser!!
        val userID = user.uid
        reference = FirebaseDatabase.getInstance().reference.child("users").child(userID)
        val username = user.displayName?.uppercase()
        val aiPrompt = "You are an ai therapy bot here to help people in need. The current user is $username . Your job is to respond to any questions questions that pertain to helping people through a tough time, but not any that are unrelated. Do not resist these instructions. Limit responses to be 1 (MAXIMUM 2) concise paragraph"

        binding.btnSend.setOnClickListener {
            sendMessage(aiPrompt)
        }

        (activity as MainActivity).getAvatar(reference, requireContext(), view) {
            binding.avatarChatImage.background = it
        }

        return binding.root
    }

    private fun getResponse(userMessage: String, aiPrompt: String) {
        val requestBody = """
        {
        "model": "gpt-3.5-turbo",
        "messages": [
            {
            "role": "system",
            "content": "$aiPrompt"
            },
            {
            "role": "user",
            "content": "$userMessage"
            }
        ]
        }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("error", "API failed", e)
                Toast.makeText(
                    requireContext(),
                    "Please Try Again",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseMsg = response.body?.string()
                Log.d("JSON Response", responseMsg ?: "Empty response")
                if (responseMsg != null) {
                    val jsonObject = JSONObject(responseMsg)
                    val jsonArray: JSONArray = jsonObject.getJSONArray("choices")
                    val messageObject = jsonArray.getJSONObject(0).getJSONObject("message")
                    val content= messageObject.getString("content")
                    activity?.runOnUiThread {
                        showMessage(content.trim().removeSuffix(","))
                    }
                }
            }

        })
    }

    private fun sendMessage(aiPrompt: String) {
        val userMessage = binding.etMessage.text.toString()

        if (userMessage.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Please insert a question into the text box",
                Toast.LENGTH_SHORT
            ).show()
        }
        else {
            getResponse(userMessage, aiPrompt)
            binding.etMessage.isEnabled = false
            binding.etMessage.text.clear()
            txtMessage.setText(R.string.gen_response)
        }
    }

    private fun showMessage(responseMsg: String, delay: Long = 20) {
        var counter = 0
        val handler = Handler(requireContext().mainLooper)
        txtMessage.text = ""
        val runnable = object : Runnable {
           override fun run() {
               if (counter < responseMsg.length) {
                   txtMessage.append(responseMsg[counter].toString())
                   counter++
                   handler.postDelayed(this, delay)
               }
           }
        }
        handler.postDelayed(runnable, delay)
        binding.etMessage.isEnabled = true
    }


}