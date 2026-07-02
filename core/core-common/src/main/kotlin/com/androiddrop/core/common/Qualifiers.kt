package com.androiddrop.core.common

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BleRepo

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NearbyRepo

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WifiDirectRepo

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultRepo
