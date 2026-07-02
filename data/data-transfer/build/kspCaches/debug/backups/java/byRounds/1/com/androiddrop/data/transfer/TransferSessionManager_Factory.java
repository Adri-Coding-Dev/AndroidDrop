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
public final class TransferSessionManager_Factory implements Factory<TransferSessionManager> {
  private final Provider<CryptoManager> cryptoManagerProvider;

  public TransferSessionManager_Factory(Provider<CryptoManager> cryptoManagerProvider) {
    this.cryptoManagerProvider = cryptoManagerProvider;
  }

  @Override
  public TransferSessionManager get() {
    return newInstance(cryptoManagerProvider.get());
  }

  public static TransferSessionManager_Factory create(
      Provider<CryptoManager> cryptoManagerProvider) {
    return new TransferSessionManager_Factory(cryptoManagerProvider);
  }

  public static TransferSessionManager newInstance(CryptoManager cryptoManager) {
    return new TransferSessionManager(cryptoManager);
  }
}
