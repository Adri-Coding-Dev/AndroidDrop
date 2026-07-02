package com.androiddrop.sync.protocol;

import com.androiddrop.core.network.SocketManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class SyncModule_ProvideSyncProtocolFactory implements Factory<SyncProtocol> {
  private final Provider<SocketManager> socketManagerProvider;

  public SyncModule_ProvideSyncProtocolFactory(Provider<SocketManager> socketManagerProvider) {
    this.socketManagerProvider = socketManagerProvider;
  }

  @Override
  public SyncProtocol get() {
    return provideSyncProtocol(socketManagerProvider.get());
  }

  public static SyncModule_ProvideSyncProtocolFactory create(
      Provider<SocketManager> socketManagerProvider) {
    return new SyncModule_ProvideSyncProtocolFactory(socketManagerProvider);
  }

  public static SyncProtocol provideSyncProtocol(SocketManager socketManager) {
    return Preconditions.checkNotNullFromProvides(SyncModule.INSTANCE.provideSyncProtocol(socketManager));
  }
}
