package com.snapchef.app.core.di

import com.snapchef.app.core.data.remote.createHttpClient
import com.snapchef.app.features.auth.data.remote.AuthApiService
import com.snapchef.app.features.groups.data.remote.GroupSharingApiService
import com.snapchef.app.features.home.data.remote.HomeApiService

/**
 * Central service locator for SnapChef.
 *
 * Holds a single shared HttpClient and exposes fully-constructed
 * API service instances. iOS (and Android if needed) can use these
 * directly without ever touching the HttpClient.
 *
 * Usage from Swift:
 *   let auth = SnapChefServiceLocator.shared.authApiService
 *   let home = SnapChefServiceLocator.shared.homeApiService
 */
object SnapChefServiceLocator {

    private val httpClient by lazy { createHttpClient() }

    val authApiService: AuthApiService by lazy { AuthApiService(httpClient) }
    val homeApiService: HomeApiService by lazy { HomeApiService(httpClient) }
    val groupSharingApiService: GroupSharingApiService by lazy { GroupSharingApiService(httpClient) }
}
