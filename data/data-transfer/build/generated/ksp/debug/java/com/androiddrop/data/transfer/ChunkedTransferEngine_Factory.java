package com.androiddrop.data.transfer;

import com.androiddrop.core.crypto.CryptoManager;
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
public final class ChunkedTransferEngine_Factory implements Factory<ChunkedTransferEngine> {
  private final Provider<CryptoManager> cryptoManagerProvider;

  private final Provider<TransferSessionManager> sessionManagerProvider;

  public ChunkedTransferEngine_Factory(Provider<CryptoManager> cryptoManagerProvider,
      Provider<TransferSessionManager> sessionManagerProvider) {
    this.cryptoManagerProvider = cryptoManagerProvider;
    this.sessionManagerProvider = sessionManagerProvider;
  }

  @Override
  public ChunkedTransferEngine get() {
    return newInstance(cryptoManagerProvider.get(), sessionManagerProvider.get());
  }

  public static ChunkedTransferEngine_Factory create(Provider<CryptoManager> cryptoManagerProvider,
      Provider<TransferSessionManager> sessionManagerProvider) {
    return new ChunkedTransferEngine_Factory(cryptoManagerProvider, sessionManagerProvider);
  }

  public static ChunkedTransferEngine newInstance(CryptoManager cryptoManager,
      TransferSessionManager sessionManager) {
    return new ChunkedTransferEngine(cryptoManager, sessionManager);
  }
}
