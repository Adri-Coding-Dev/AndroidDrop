package com.androiddrop.security.keyexchange;

import com.androiddrop.core.crypto.CryptoManager;
import com.androiddrop.security.crypto.SessionCryptoProvider;
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
public final class KeyExchangeModule_ProvideKeyExchangeProtocolFactory implements Factory<KeyExchangeProtocol> {
  private final Provider<SessionCryptoProvider> cryptoProvider;

  private final Provider<CryptoManager> cryptoManagerProvider;

  public KeyExchangeModule_ProvideKeyExchangeProtocolFactory(
      Provider<SessionCryptoProvider> cryptoProvider,
      Provider<CryptoManager> cryptoManagerProvider) {
    this.cryptoProvider = cryptoProvider;
    this.cryptoManagerProvider = cryptoManagerProvider;
  }

  @Override
  public KeyExchangeProtocol get() {
    return provideKeyExchangeProtocol(cryptoProvider.get(), cryptoManagerProvider.get());
  }

  public static KeyExchangeModule_ProvideKeyExchangeProtocolFactory create(
      Provider<SessionCryptoProvider> cryptoProvider,
      Provider<CryptoManager> cryptoManagerProvider) {
    return new KeyExchangeModule_ProvideKeyExchangeProtocolFactory(cryptoProvider, cryptoManagerProvider);
  }

  public static KeyExchangeProtocol provideKeyExchangeProtocol(SessionCryptoProvider cryptoProvider,
      CryptoManager cryptoManager) {
    return Preconditions.checkNotNullFromProvides(KeyExchangeModule.INSTANCE.provideKeyExchangeProtocol(cryptoProvider, cryptoManager));
  }
}
