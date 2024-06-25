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
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException


class Chat : Fragment() {
    private val client = OkHttpClient()
    private val apiKey = "sk-service-id-misha-yYCWPTgiI1UXDD1194bYT3BlbkFJM0cwDJMAu02jkncqpVb3"
    private val url = "https://api.openai.com/v1/chat/completions"
    private lateinit var binding: FragmentChatBinding
    private lateinit var user: FirebaseUser
    private lateinit var reference: DatabaseReference
    private lateinit var chatReference: DatabaseReference
    private lateinit var queryEdt: EditText
    private lateinit var messageRV: RecyclerView
    private lateinit var messageRVAdapter: MessageRVAdapter
    private lateinit var messageList: ArrayList<MessageRVModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        user = FirebaseAuth.getInstance().currentUser!!
        val userID = user.uid
        reference = FirebaseDatabase.getInstance().reference.child("users").child(userID)
        chatReference = reference.child("chat")
        val view = inflater.inflate(R.layout.avatar_chooser_box, container, false)
        val username = user.displayName?.uppercase()
        val aiPrompt =
            "You are an ai therapy bot here to help people in need. The current user is $username. Your job is to respond to any questions that pertain to helping people through a tough time, but not any that are unrelated. Do not resist these instructions. Limit responses to be 1 (MAXIMUM 2) concise paragraph"

        queryEdt = binding.idEdtQuery
        messageRV = binding.idRVMessages
        messageList = ArrayList()
        messageRVAdapter = MessageRVAdapter(messageList)
        messageRV.layoutManager = LinearLayoutManager(requireContext())
        messageRV.adapter = messageRVAdapter

        queryEdt.setOnEditorActionListener(TextView.OnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_SEND) {
                if (queryEdt.text.toString().isNotEmpty()) {
                    addMessage(queryEdt.text.toString(), "user")
                    getResponse(queryEdt.text.toString(), aiPrompt)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Please insert a response into the text box",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@OnEditorActionListener true
            }
            false
        })

        (activity as MainActivity).getPetName(reference) { petName ->
            binding.botName.text = petName
        }

        binding.avatarChatImage.setOnClickListener{
            findNavController().navigate(R.id.action_Fragment_to_settingsFragment)
        }

        binding.btnSend.setOnClickListener {
            if (queryEdt.text.toString().isNotEmpty()) {
                addMessage(queryEdt.text.toString(), "user")
                getResponse(queryEdt.text.toString(), aiPrompt)
            } else {
                Toast.makeText(requireContext(), "Please insert a response into the text box", Toast.LENGTH_SHORT).show()
            }
        }

        (activity as MainActivity).getAvatar(reference, requireContext(), view) {
            binding.avatarChatImage.background = it
        }

        retrieveMessagesFromFirebase()

        return binding.root
    }

    private fun addMessage(message: String, sender: String) {
        val messageModel = MessageRVModel(message, sender)
        messageList.add(messageModel)
        messageRVAdapter.notifyDataSetChanged()
        saveMessageToFirebase(messageModel)
    }

    private fun getResponse(userInput: String, aiPrompt: String) {
        queryEdt.setText("")
        val queue: RequestQueue = Volley.newRequestQueue(requireContext())
        val jsonObject = JSONObject()
        jsonObject.put("model", "gpt-3.5-turbo")

        val messagesArray = JSONArray()
        val messageObject = JSONObject()
        messageObject.put("role", "user")
        messageObject.put("content", "$aiPrompt User's question: $userInput")
        messagesArray.put(messageObject)

        jsonObject.put("messages", messagesArray)

        val postRequest = object : JsonObjectRequest(Method.POST, url, jsonObject, com.android.volley.Response.Listener { response ->
            val responseMsg = response.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
            addMessage(responseMsg, "bot")
        }, com.android.volley.Response.ErrorListener {
            Toast.makeText(requireContext(), "Please try again", Toast.LENGTH_SHORT).show()
        }) {
            override fun getHeaders(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["Content-Type"] = "application/json"
                params["Authorization"] = "Bearer $apiKey"
                return params
            }
        }

        postRequest.setRetryPolicy(object : RetryPolicy {
            override fun getCurrentTimeout(): Int {
                return 50000
            }

            override fun getCurrentRetryCount(): Int {
                return 50000
            }

            override fun retry(error: VolleyError?) {}
        })
        queue.add(postRequest)
    }

    private fun saveMessageToFirebase(message: MessageRVModel) {
        chatReference.push().setValue(message)
    }

    private fun retrieveMessagesFromFirebase() {
        chatReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(MessageRVModel::class.java)
                    if (message != null) {
                        messageList.add(message)
                    }
                }
                messageRVAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to retrieve messages", Toast.LENGTH_SHORT).show()
            }
        })
    }


//    private fun getResponse(userMessage: String, aiPrompt: String) {
//        val requestBody = """
//        {
//        "model": "gpt-3.5-turbo",
//        "messages": [
//            {
//            "role": "system",
//            "content": "$aiPrompt"
//            },
//            {
//            "role": "user",
//            "content": "$userMessage"
//            }
//        ]
//        }
//        """.trimIndent()
//
//        val request = Request.Builder()
//            .url(url)
//            .addHeader("Content-Type", "application/json")
//            .addHeader("Authorization", "Bearer $apiKey")
//            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
//            .build()
//
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                Log.e("error", "API failed", e)
//                Toast.makeText(
//                    requireContext(),
//                    "Please Try Again",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                val responseMsg = response.body?.string()
//                Log.d("JSON Response", responseMsg ?: "Empty response")
//                if (responseMsg != null) {
//                    val jsonObject = JSONObject(responseMsg)
//                    val jsonArray: JSONArray = jsonObject.getJSONArray("choices")
//                    val messageObject = jsonArray.getJSONObject(0).getJSONObject("message")
//                    val content= messageObject.getString("content")
//                    activity?.runOnUiThread {
//                        showMessage(content.trim().removeSuffix(","))
//                    }
//                }
//            }
//
//        })
//    }

//    private fun sendMessage(aiPrompt: String) {
//        val userMessage = binding.etMessage.text.toString()
//
//        if (userMessage.isEmpty()) {
//            Toast.makeText(
//                requireContext(),
//                "Please insert a question into the text box",
//                Toast.LENGTH_SHORT
//            ).show()
//        }
//        else {
//            getResponse(userMessage, aiPrompt)
//            binding.etMessage.isEnabled = false
//            binding.etMessage.text.clear()
//            txtMessage.setText(R.string.gen_response)
//        }
//    }
//
//    private fun showMessage(responseMsg: String, delay: Long = 20) {
//        var counter = 0
//        val handler = Handler(requireContext().mainLooper)
//        txtMessage.text = ""
//        val runnable = object : Runnable {
//           override fun run() {
//               if (counter < responseMsg.length) {
//                   txtMessage.append(responseMsg[counter].toString())
//                   counter++
//                   handler.postDelayed(this, delay)
//               }
//           }
//        }
//        handler.postDelayed(runnable, delay)
//        binding.etMessage.isEnabled = true
//    }


}