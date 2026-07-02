package com.androiddrop.security.crypto;

import com.androiddrop.core.crypto.CryptoManager;
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
public final class CryptoModule_ProvideSessionCryptoProviderFactory implements Factory<SessionCryptoProvider> {
  private final Provider<CryptoManager> cryptoManagerProvider;

  public CryptoModule_ProvideSessionCryptoProviderFactory(
      Provider<CryptoManager> cryptoManagerProvider) {
    this.cryptoManagerProvider = cryptoManagerProvider;
  }

  @Override
  public SessionCryptoProvider get() {
    return provideSessionCryptoProvider(cryptoManagerProvider.get());
  }

  public static CryptoModule_ProvideSessionCryptoProviderFactory create(
      Provider<CryptoManager> cryptoManagerProvider) {
    return new CryptoModule_ProvideSessionCryptoProviderFactory(cryptoManagerProvider);
  }

  public static SessionCryptoProvider provideSessionCryptoProvider(CryptoManager cryptoManager) {
    return Preconditions.checkNotNullFromProvides(CryptoModule.INSTANCE.provideSessionCryptoProvider(cryptoManager));
  }
}
