package com.androiddrop.security.keyexchange;

import com.androiddrop.core.crypto.CryptoManager;
import com.androiddrop.security.crypto.SessionCryptoProvider;
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
public final class EcdhKeyExchangeProtocol_Factory implements Factory<EcdhKeyExchangeProtocol> {
  private final Provider<SessionCryptoProvider> cryptoProvider;

  private final Provider<CryptoManager> cryptoManagerProvider;

  public EcdhKeyExchangeProtocol_Factory(Provider<SessionCryptoProvider> cryptoProvider,
      Provider<CryptoManager> cryptoManagerProvider) {
    this.cryptoProvider = cryptoProvider;
    this.cryptoManagerProvider = cryptoManagerProvider;
  }

  @Override
  public EcdhKeyExchangeProtocol get() {
    return newInstance(cryptoProvider.get(), cryptoManagerProvider.get());
  }

  public static EcdhKeyExchangeProtocol_Factory create(
      Provider<SessionCryptoProvider> cryptoProvider,
      Provider<CryptoManager> cryptoManagerProvider) {
    return new EcdhKeyExchangeProtocol_Factory(cryptoProvider, cryptoManagerProvider);
  }

  public static EcdhKeyExchangeProtocol newInstance(SessionCryptoProvider cryptoProvider,
      CryptoManager cryptoManager) {
    return new EcdhKeyExchangeProtocol(cryptoProvider, cryptoManager);
  }
}
