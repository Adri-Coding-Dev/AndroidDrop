package com.androiddrop.core.crypto;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import java.security.SecureRandom;
import javax.annotation.processing.Generated;

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
public final class CoreCryptoModule_Companion_ProvideSecureRandomFactory implements Factory<SecureRandom> {
  @Override
  public SecureRandom get() {
    return provideSecureRandom();
  }

  public static CoreCryptoModule_Companion_ProvideSecureRandomFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SecureRandom provideSecureRandom() {
    return Preconditions.checkNotNullFromProvides(CoreCryptoModule.Companion.provideSecureRandom());
  }

  private static final class InstanceHolder {
    private static final CoreCryptoModule_Companion_ProvideSecureRandomFactory INSTANCE = new CoreCryptoModule_Companion_ProvideSecureRandomFactory();
  }
}
