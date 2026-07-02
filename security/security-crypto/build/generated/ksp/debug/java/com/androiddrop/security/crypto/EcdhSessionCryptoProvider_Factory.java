package com.androiddrop.security.crypto;

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
public final class EcdhSessionCryptoProvider_Factory implements Factory<EcdhSessionCryptoProvider> {
  private final Provider<CryptoManager> cryptoManagerProvider;

  public EcdhSessionCryptoProvider_Factory(Provider<CryptoManager> cryptoManagerProvider) {
    this.cryptoManagerProvider = cryptoManagerProvider;
  }

  @Override
  public EcdhSessionCryptoProvider get() {
    return newInstance(cryptoManagerProvider.get());
  }

  public static EcdhSessionCryptoProvider_Factory create(
      Provider<CryptoManager> cryptoManagerProvider) {
    return new EcdhSessionCryptoProvider_Factory(cryptoManagerProvider);
  }

  public static EcdhSessionCryptoProvider newInstance(CryptoManager cryptoManager) {
    return new EcdhSessionCryptoProvider(cryptoManager);
  }
}
