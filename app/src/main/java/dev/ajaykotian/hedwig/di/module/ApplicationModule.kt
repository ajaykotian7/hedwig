package dev.ajaykotian.hedwig.di.module

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dev.ajaykotian.hedwig.HedwigApp
import dev.ajaykotian.hedwig.data.repository.AuthRepository
import dev.ajaykotian.hedwig.data.repository.AuthRepositoryImpl
import dev.ajaykotian.hedwig.data.repository.UserRepository
import dev.ajaykotian.hedwig.data.repository.UserRepositoryImpl
import dev.ajaykotian.hedwig.di.ApplicationContext
import javax.inject.Singleton


@Module
class ApplicationModule(private val application: HedwigApp) {

    @ApplicationContext
    @Provides
    fun provideContext(): Context = application

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    fun provideAuthRepository(impl: AuthRepositoryImpl): AuthRepository = impl

    @Provides
    fun provideUserRepository(impl: UserRepositoryImpl): UserRepository = impl
}
