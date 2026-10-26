package dev.ajaykotian.hedwig.di.component

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Component
import dev.ajaykotian.hedwig.di.ApplicationContext
import dev.ajaykotian.hedwig.di.module.ApplicationModule
import javax.inject.Singleton


@Singleton
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {

    @ApplicationContext
    fun getContext(): Context

    fun getFirebaseAuth(): FirebaseAuth

    fun getFirebaseFirestore(): FirebaseFirestore
}
