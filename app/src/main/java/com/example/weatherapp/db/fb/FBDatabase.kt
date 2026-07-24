package com.example.weatherapp.db.fb

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow

class FBDatabase {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val user: Flow<FBUser>
        get() {
            if (auth.currentUser == null) return emptyFlow()
            return callbackFlow {
                val listener = db.collection("users")
                    .document(auth.currentUser!!.uid)
                    .addSnapshotListener { snapshot, ex ->
                        if (ex != null) { close(ex); return@addSnapshotListener }
                        if (snapshot != null && snapshot.exists()) {
                            val user = snapshot.toObject(FBUser::class.java)
                            if (user != null) trySend(user)
                        }
                    }
                awaitClose { listener.remove() }
            }
        }

    val cities: Flow<List<FBCity>>
        get() {
            if (auth.currentUser == null) return emptyFlow()
            return callbackFlow {
                val listener = db.collection("users")
                    .document(auth.currentUser!!.uid)
                    .collection("cities")
                    .addSnapshotListener { snapshot, ex ->
                        if (ex != null) { close(ex); return@addSnapshotListener }
                        if (snapshot != null) {
                            trySend(snapshot.toObjects(FBCity::class.java))
                        }
                    }
                awaitClose { listener.remove() }
            }
        }

    fun register(user: FBUser) {
        if (auth.currentUser == null)
            throw RuntimeException("User not logged in!")
        val uid = auth.currentUser!!.uid
        db.collection("users").document(uid + "").set(user)
    }

    fun add(city: FBCity) {
        if (auth.currentUser == null)
            throw RuntimeException("User not logged in!")
        if (city.name == null || city.name!!.isEmpty())
            throw RuntimeException("City with null or empty name!")
        val uid = auth.currentUser!!.uid
        db.collection("users").document(uid).collection("cities")
            .document(city.name!!).set(city)
    }

    fun remove(city: FBCity) {
        if (auth.currentUser == null)
            throw RuntimeException("User not logged in!")
        if (city.name == null || city.name!!.isEmpty())
            throw RuntimeException("City with null or empty name!")
        val uid = auth.currentUser!!.uid
        db.collection("users").document(uid).collection("cities")
            .document(city.name!!).delete()
    }

    fun update(city: FBCity) {
        if (auth.currentUser == null) throw RuntimeException("Not logged in!")
        val uid = auth.currentUser!!.uid
        val changes = mapOf("lat" to city.lat,"lng" to city.lng,
            "monitored" to city.monitored )
        db.collection("users").document(uid)
            .collection("cities").document(city.name!!).update(changes)
    }
}
