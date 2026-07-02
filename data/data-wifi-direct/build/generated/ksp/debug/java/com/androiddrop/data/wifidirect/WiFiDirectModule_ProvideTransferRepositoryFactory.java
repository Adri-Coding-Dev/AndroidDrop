package com.androiddrop.data.wifidirect;

import com.androiddrop.domain.repository.TransferRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("com.androiddrop.core.common.WifiDirectRepo")
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
public final class WiFiDirectModule_ProvideTransferRepositoryFactory implements Factory<TransferRepository> {
  private final Provider<WiFiDirectTransferRepository> repositoryProvider;

  public WiFiDirectModule_ProvideTransferRepositoryFactory(
      Provider<WiFiDirectTransferRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public TransferRepository get() {
    return provideTransferRepository(repositoryProvider.get());
  }

  public static WiFiDirectModule_ProvideTransferRepositoryFactory create(
      Provider<WiFiDirectTransferRepository> repositoryProvider) {
    return new WiFiDirectModule_ProvideTransferRepositoryFactory(repositoryProvider);
  }

  public static TransferRepository provideTransferRepository(
      WiFiDirectTransferRepository repository) {
    return Preconditions.checkNotNullFromProvides(WiFiDirectModule.INSTANCE.provideTransferRepository(repository));
  }
}
