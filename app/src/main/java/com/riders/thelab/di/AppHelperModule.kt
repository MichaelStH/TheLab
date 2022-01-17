package com.riders.thelab.di

import com.riders.thelab.data.IRepository
import com.riders.thelab.data.RepositoryImpl
import com.riders.thelab.data.local.DbImpl
import com.riders.thelab.data.local.LabDatabase
import com.riders.thelab.data.remote.ApiImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
//@InstallIn(ViewModelComponent::class) // this is new
@InstallIn(SingletonComponent::class)
object AppHelperModule {

    @Provides
    fun provideDbHelper(appDatabase: LabDatabase) =
        DbImpl(
            appDatabase.getContactDao(),
            appDatabase.getWeatherDao()
        )

    @Provides
    fun provideApiHelper() =
        ApiImpl(
            ApiModule.provideArtistsAPIService(),
            ApiModule.provideGoogleAPIService(),
            ApiModule.provideYoutubeApiService(),
            ApiModule.provideWeatherApiService(),
            ApiModule.proWeatherBulkApiService()
        )

    @Provides
//    @ViewModelScoped // this is new
    @Singleton
    fun provideRepository(dbImpl: DbImpl, apiImpl: ApiImpl) =
        RepositoryImpl(dbImpl, apiImpl) as IRepository
}