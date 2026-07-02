package com.androiddrop.data.transfer;

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
public final class TransferRepositoryImpl_Factory implements Factory<TransferRepositoryImpl> {
  private final Provider<TransferSessionManager> sessionManagerProvider;

  private final Provider<ChunkedTransferEngine> engineProvider;

  public TransferRepositoryImpl_Factory(Provider<TransferSessionManager> sessionManagerProvider,
      Provider<ChunkedTransferEngine> engineProvider) {
    this.sessionManagerProvider = sessionManagerProvider;
    this.engineProvider = engineProvider;
  }

  @Override
  public TransferRepositoryImpl get() {
    return newInstance(sessionManagerProvider.get(), engineProvider.get());
  }

  public static TransferRepositoryImpl_Factory create(
      Provider<TransferSessionManager> sessionManagerProvider,
      Provider<ChunkedTransferEngine> engineProvider) {
    return new TransferRepositoryImpl_Factory(sessionManagerProvider, engineProvider);
  }

  public static TransferRepositoryImpl newInstance(TransferSessionManager sessionManager,
      ChunkedTransferEngine engine) {
    return new TransferRepositoryImpl(sessionManager, engine);
  }
}
