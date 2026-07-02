package com.androiddrop.domain.usecase;

import com.androiddrop.domain.repository.SyncRepository;
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
public final class SyncAnimationUseCase_Factory implements Factory<SyncAnimationUseCase> {
  private final Provider<SyncRepository> syncRepositoryProvider;

  public SyncAnimationUseCase_Factory(Provider<SyncRepository> syncRepositoryProvider) {
    this.syncRepositoryProvider = syncRepositoryProvider;
  }

  @Override
  public SyncAnimationUseCase get() {
    return newInstance(syncRepositoryProvider.get());
  }

  public static SyncAnimationUseCase_Factory create(
      Provider<SyncRepository> syncRepositoryProvider) {
    return new SyncAnimationUseCase_Factory(syncRepositoryProvider);
  }

  public static SyncAnimationUseCase newInstance(SyncRepository syncRepository) {
    return new SyncAnimationUseCase(syncRepository);
  }
}
