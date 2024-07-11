package com.example.myapplication;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FirestoreHelper {

    private FirebaseFirestore db;

    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
    }

    public void addUser(String username, String password) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("password", password);

        db.collection("users").add(user)
                .addOnSuccessListener(documentReference -> Log.d("Firestore", "DocumentSnapshot added with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.w("Firestore", "Error adding document", e));
    }

    public void addCertificate(String name, String designation, String date) {
        Map<String, Object> certificate = new HashMap<>();
        certificate.put("name", name);
        certificate.put("designation", designation);
        certificate.put("date", date);

        db.collection("certificates").add(certificate)
                .addOnSuccessListener(documentReference -> Log.d("Firestore", "DocumentSnapshot added with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.w("Firestore", "Error adding document", e));
    }

    public void addIDCard(String name, String designation, String number) {
        Map<String, Object> idCard = new HashMap<>();
        idCard.put("name", name);
        idCard.put("designation", designation);
        idCard.put("number", number);

        db.collection("id_cards").add(idCard)
                .addOnSuccessListener(documentReference -> Log.d("Firestore", "DocumentSnapshot added with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.w("Firestore", "Error adding document", e));
    }
}
