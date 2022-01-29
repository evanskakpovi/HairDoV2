package com.ekm.hairdo.things

import android.util.Log
import com.ekm.hairdo.things.Stack.Companion.toStack
import com.ekm.hairdo.vars
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseStackService {
    private const val TAG = "FirebaseProfileService"
    var lastVisible: DocumentSnapshot? = null

    //Using coroutines here to control flow of data
    //suspend fun getStackData(): Stack? {
        val db = FirebaseFirestore.getInstance()
       // return try {

            val next = db.collection(vars.STYLES).startAfter(lastVisible).limit(10  )
//
//            next.get()
//                .addOnSuccessListener { documents ->
//                if (documents.size()>0) {
//
//                    var freshStacks = mutableListOf<Stack>()
//                    for (document  in documents) {
//                        val stack = document.toStack()
//                        freshStacks.add(stack!!)
//                        }
//                    }
//
//                }
//                .addOnFailureListener{ exception ->
//                    Log.w(TAG, "Error getting Stacks: ", exception)
//                }

//        } catch (e: Exception) {
//            Log.e(TAG, "Error getting user details", e)
//            FirebaseCrashlytics.getInstance().log("Error getting user details")
////            FirebaseCrashlytics.getInstance().setCustomKey("user id", xpertSlug)
//            FirebaseCrashlytics.getInstance().recordException(e)
//            null
//        }
    //}
}