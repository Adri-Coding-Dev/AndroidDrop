package com.androiddrop.sync.protocol;

import com.androiddrop.domain.repository.SyncRepository;
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
public final class SyncModule_ProvideSyncRepositoryFactory implements Factory<SyncRepository> {
  private final Provider<SyncRepositoryImpl> implProvider;

  public SyncModule_ProvideSyncRepositoryFactory(Provider<SyncRepositoryImpl> implProvider) {
    this.implProvider = implProvider;
  }

  @Override
  public SyncRepository get() {
    return provideSyncRepository(implProvider.get());
  }

  public static SyncModule_ProvideSyncRepositoryFactory create(
      Provider<SyncRepositoryImpl> implProvider) {
    return new SyncModule_ProvideSyncRepositoryFactory(implProvider);
  }

  public static SyncRepository provideSyncRepository(SyncRepositoryImpl impl) {
    return Preconditions.checkNotNullFromProvides(SyncModule.INSTANCE.provideSyncRepository(impl));
  }
}
