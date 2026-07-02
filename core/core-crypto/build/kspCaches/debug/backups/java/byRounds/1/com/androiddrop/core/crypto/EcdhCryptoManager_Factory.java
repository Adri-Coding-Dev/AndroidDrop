package com.androiddrop.core.crypto;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import java.security.SecureRandom;
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
public final class EcdhCryptoManager_Factory implements Factory<EcdhCryptoManager> {
  private final Provider<SecureRandom> secureRandomProvider;

  public EcdhCryptoManager_Factory(Provider<SecureRandom> secureRandomProvider) {
    this.secureRandomProvider = secureRandomProvider;
  }

  @Override
  public EcdhCryptoManager get() {
    return newInstance(secureRandomProvider.get());
  }

  public static EcdhCryptoManager_Factory create(Provider<SecureRandom> secureRandomProvider) {
    return new EcdhCryptoManager_Factory(secureRandomProvider);
  }

  public static EcdhCryptoManager newInstance(SecureRandom secureRandom) {
    return new EcdhCryptoManager(secureRandom);
  }
}
