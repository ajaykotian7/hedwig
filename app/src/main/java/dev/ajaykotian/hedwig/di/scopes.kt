package dev.ajaykotian.hedwig.di

import javax.inject.Scope


@Scope
@Retention(AnnotationRetention.BINARY)
annotation class ActivityScope


@Scope
@Retention(AnnotationRetention.BINARY)
annotation class ScreenScope
