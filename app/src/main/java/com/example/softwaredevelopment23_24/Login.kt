package com.example.softwaredevelopment23_24

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.example.softwaredevelopment23_24.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Login.newInstance] factory method to
 * create an instance of this fragment.
 */
class Login : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var userAuth : FirebaseAuth
    private lateinit var  googleSignInClient : GoogleSignInClient
    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            handleSignInResult(data)
        } else {
            Toast.makeText(requireActivity(), "Sign in failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root

        // getting rid of navigation bar so that login is required
        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        bottomNavigationView.visibility = View.GONE

        // variables for google sign in
        userAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient( requireActivity() , gso)

        // listening to the button for google sign in
        binding.googleButton.setOnClickListener {
            Toast.makeText(requireActivity(), "Logging In", Toast.LENGTH_SHORT).show()
            // only important line of code ↓
           signInLauncher.launch(googleSignInClient.signInIntent)
        }
        // Inflate the layout for this fragment
        return view
    }

    // func for how to handle post sign in result
   private fun handleSignInResult(data: Intent? ) {
       val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
       try {
           val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
           if (account !=null) {
               findNavController().navigate(R.id.navigation_home)
           }
       } catch (e: ApiException) {
           Toast.makeText(requireActivity(), e.toString(), Toast.LENGTH_SHORT).show()
       }
   }

    override fun onDestroyView() {
        // show nav bar when fragment is closed
        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        bottomNavigationView.visibility = View.VISIBLE

        super.onDestroyView()
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Login.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Login().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}