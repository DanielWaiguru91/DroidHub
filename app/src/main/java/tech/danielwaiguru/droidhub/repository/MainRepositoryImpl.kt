package tech.danielwaiguru.droidhub.repository

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await
import tech.danielwaiguru.droidhub.common.Constants.FILES_BUCKET
import tech.danielwaiguru.droidhub.common.Constants.USERS_COLLECTION
import tech.danielwaiguru.droidhub.model.FileUpload
import tech.danielwaiguru.droidhub.model.ResultWrapper
import tech.danielwaiguru.droidhub.model.User

class MainRepositoryImpl: MainRepository {
    private val auth: FirebaseAuth = Firebase.auth
    private val storage: FirebaseStorage = Firebase.storage
    private val database: FirebaseFirestore = Firebase.firestore
    private val filesCollection = database.collection(FILES_BUCKET)
    override suspend fun signUp(user: User, password: String): ResultWrapper<AuthResult> =
            try {
                val taskResult = auth.createUserWithEmailAndPassword(user.email, password).await()
                createUser(user)
                ResultWrapper.Success(taskResult)
            }
            catch (e: FirebaseAuthException){
                ResultWrapper.Failure(e.message.toString())
            }

    override suspend fun signIn(email: String, password: String) : ResultWrapper<AuthResult> =
            try {
                ResultWrapper.Success(auth.signInWithEmailAndPassword(email, password).await())
            }
            catch (e: Exception) {
                ResultWrapper.Failure(e.message.toString())
            }

    override fun createUser(user: User) {
        val userCollection = database.collection(USERS_COLLECTION)
        userCollection.add(user)
    }

    override fun saveFile(fileName: String, downloadUrl: String): ResultWrapper<Task<Void>> =
            try {
                val fileId = filesCollection.document().id
                val  file = FileUpload(fileId, fileName, downloadUrl)
                val resourceDocument = filesCollection.document(fileId).set(file)
                ResultWrapper.Success(resourceDocument)
            }
            catch (e: FirebaseFirestoreException){
                ResultWrapper.Failure(e.message.toString())
            }

    override suspend fun uploadFile(fileUri: Uri, fileName: String) : ResultWrapper<String> =
            try {
                val filesBucket = storage.reference.child(FILES_BUCKET)
                val downloadUrl = filesBucket.child(fileName).putFile(fileUri)
                        .await()
                        .storage
                        .downloadUrl
                        .await()
                        .toString()
                ResultWrapper.Success(downloadUrl)
            }
            catch (e: FirebaseException){
                ResultWrapper.Failure(e.message.toString())
            }

    override fun getFiles(): Query {
        return database.collection(FILES_BUCKET).orderBy("fileName", Query.Direction.ASCENDING)
    }

    override fun deleteFile(documentId: String): ResultWrapper<Task<Void>> =
            try {
                ResultWrapper.Success(filesCollection.document(documentId).delete())
            }
            catch (e: FirebaseFirestoreException){
                ResultWrapper.Failure(e.message.toString())
            }

    override fun freeStorage(fileName: String): ResultWrapper<Task<Void>> =
            try {
                ResultWrapper.Success(storage.reference.child(FILES_BUCKET).child(fileName).delete())
            }
            catch (e: Exception) {
                ResultWrapper.Failure(e.message.toString())
            }

    override fun getCurrentUser() = auth.currentUser

    override fun signOut() {
        auth.signOut()
    }
}