package com.androiddrop.data.transfer;

import com.androiddrop.domain.repository.TransferRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("com.androiddrop.core.common.DefaultRepo")
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
public final class TransferModule_ProvideTransferRepositoryFactory implements Factory<TransferRepository> {
  private final Provider<TransferRepositoryImpl> repositoryProvider;

  public TransferModule_ProvideTransferRepositoryFactory(
      Provider<TransferRepositoryImpl> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public TransferRepository get() {
    return provideTransferRepository(repositoryProvider.get());
  }

  public static TransferModule_ProvideTransferRepositoryFactory create(
      Provider<TransferRepositoryImpl> repositoryProvider) {
    return new TransferModule_ProvideTransferRepositoryFactory(repositoryProvider);
  }

  public static TransferRepository provideTransferRepository(TransferRepositoryImpl repository) {
    return Preconditions.checkNotNullFromProvides(TransferModule.INSTANCE.provideTransferRepository(repository));
  }
}
