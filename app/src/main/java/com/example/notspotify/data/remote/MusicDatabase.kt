package com.example.notspotify.data.remote

import com.example.notspotify.data.models.Track
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MusicDatabase {
    private val firestore = FirebaseFirestore.getInstance()
    private val songCollection = firestore.collection("tracks")

    suspend fun getAllTracks() : List<Track>{
        return try {
            songCollection.get().await().toObjects(Track::class.java)
        }catch (e:Exception){
            emptyList()
        }
    }

}