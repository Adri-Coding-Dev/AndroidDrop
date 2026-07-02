package com.androiddrop.sync.protocol;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class SyncRepositoryImpl_Factory implements Factory<SyncRepositoryImpl> {
  private final Provider<SyncProtocol> syncProtocolProvider;

  public SyncRepositoryImpl_Factory(Provider<SyncProtocol> syncProtocolProvider) {
    this.syncProtocolProvider = syncProtocolProvider;
  }

  @Override
  public SyncRepositoryImpl get() {
    return newInstance(syncProtocolProvider.get());
  }

  public static SyncRepositoryImpl_Factory create(Provider<SyncProtocol> syncProtocolProvider) {
    return new SyncRepositoryImpl_Factory(syncProtocolProvider);
  }

  public static SyncRepositoryImpl newInstance(SyncProtocol syncProtocol) {
    return new SyncRepositoryImpl(syncProtocol);
  }
}
