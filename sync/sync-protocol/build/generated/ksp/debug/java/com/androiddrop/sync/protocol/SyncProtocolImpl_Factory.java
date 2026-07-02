package com.androiddrop.sync.protocol;

import com.androiddrop.core.network.SocketManager;
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
public final class SyncProtocolImpl_Factory implements Factory<SyncProtocolImpl> {
  private final Provider<SocketManager> socketManagerProvider;

  public SyncProtocolImpl_Factory(Provider<SocketManager> socketManagerProvider) {
    this.socketManagerProvider = socketManagerProvider;
  }

  @Override
  public SyncProtocolImpl get() {
    return newInstance(socketManagerProvider.get());
  }

  public static SyncProtocolImpl_Factory create(Provider<SocketManager> socketManagerProvider) {
    return new SyncProtocolImpl_Factory(socketManagerProvider);
  }

  public static SyncProtocolImpl newInstance(SocketManager socketManager) {
    return new SyncProtocolImpl(socketManager);
  }
}
