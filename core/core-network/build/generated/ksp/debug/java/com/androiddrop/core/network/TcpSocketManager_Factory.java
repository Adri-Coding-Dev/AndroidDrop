package com.androiddrop.core.network;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class TcpSocketManager_Factory implements Factory<TcpSocketManager> {
  private final Provider<OkHttpClient> okHttpClientProvider;

  public TcpSocketManager_Factory(Provider<OkHttpClient> okHttpClientProvider) {
    this.okHttpClientProvider = okHttpClientProvider;
  }

  @Override
  public TcpSocketManager get() {
    return newInstance(okHttpClientProvider.get());
  }

  public static TcpSocketManager_Factory create(Provider<OkHttpClient> okHttpClientProvider) {
    return new TcpSocketManager_Factory(okHttpClientProvider);
  }

  public static TcpSocketManager newInstance(OkHttpClient okHttpClient) {
    return new TcpSocketManager(okHttpClient);
  }
}
