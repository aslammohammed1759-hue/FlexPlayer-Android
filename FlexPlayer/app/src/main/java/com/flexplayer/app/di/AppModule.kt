package com.flexplayer.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** All dependencies use constructor @Inject, so this module is minimal.
 *  Add providers here only when you need to bind interfaces. */
@Module
@InstallIn(SingletonComponent::class)
object AppModule
