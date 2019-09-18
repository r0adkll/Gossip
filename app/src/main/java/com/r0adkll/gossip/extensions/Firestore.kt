package com.r0adkll.gossip.extensions

import androidx.lifecycle.LiveData
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import timber.log.Timber

fun <T> Task<T>.await(): T = Tasks.await(this)

inline fun <reified T> Query.liveData(
    noinline identifier: (T, String) -> Unit
): LiveData<List<T>> = QueryLiveData(this, identifier, T::class.java)

inline fun <reified T> CollectionReference.liveData(
    noinline identifier: (T, String) -> Unit
): LiveData<List<T>> = CollectionLiveData(this, identifier, T::class.java)

class QueryLiveData<T>(
    private val query: Query,
    private val identifier: (T, String) -> Unit,
    private val clazz: Class<T>
) : LiveData<List<T>>() {

    private var listener: ListenerRegistration? = null

    override fun onActive() {
        super.onActive()

        listener = query.addSnapshotListener { querySnapshot, exception ->
            if (exception == null) {
                value = querySnapshot?.documents?.map {
                    val obj = it.toObject(clazz)!!
                    identifier(obj, it.id)
                    obj
                }
            } else {
                Timber.e(exception)
            }
        }
    }

    override fun onInactive() {
        super.onInactive()
        listener?.remove()
        listener = null
    }
}

class CollectionLiveData<T>(
    private val collectionReference: CollectionReference,
    private val identifier: (T, String) -> Unit,
    private val clazz: Class<T>
) : LiveData<List<T>>() {

    private var listener: ListenerRegistration? = null

    override fun onActive() {
        super.onActive()

        listener = collectionReference.addSnapshotListener { querySnapshot, exception ->
            if (exception == null) {
                value = querySnapshot?.documents?.map {
                    val obj = it.toObject(clazz)!!
                    identifier(obj, it.id)
                    obj
                }
            } else {
                Timber.e(exception)
            }
        }
    }

    override fun onInactive() {
        super.onInactive()

        listener?.remove()
        listener = null
    }
}

