package com.androiddrop.data.wifidirect;

import com.androiddrop.core.network.TcpSocketManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class WiFiDirectTransferRepository_Factory implements Factory<WiFiDirectTransferRepository> {
  private final Provider<WiFiDirectManager> wifiDirectManagerProvider;

  private final Provider<TcpSocketManager> socketManagerProvider;

  public WiFiDirectTransferRepository_Factory(Provider<WiFiDirectManager> wifiDirectManagerProvider,
      Provider<TcpSocketManager> socketManagerProvider) {
    this.wifiDirectManagerProvider = wifiDirectManagerProvider;
    this.socketManagerProvider = socketManagerProvider;
  }

  @Override
  public WiFiDirectTransferRepository get() {
    return newInstance(wifiDirectManagerProvider.get(), socketManagerProvider.get());
  }

  public static WiFiDirectTransferRepository_Factory create(
      Provider<WiFiDirectManager> wifiDirectManagerProvider,
      Provider<TcpSocketManager> socketManagerProvider) {
    return new WiFiDirectTransferRepository_Factory(wifiDirectManagerProvider, socketManagerProvider);
  }

  public static WiFiDirectTransferRepository newInstance(WiFiDirectManager wifiDirectManager,
      TcpSocketManager socketManager) {
    return new WiFiDirectTransferRepository(wifiDirectManager, socketManager);
  }
}
