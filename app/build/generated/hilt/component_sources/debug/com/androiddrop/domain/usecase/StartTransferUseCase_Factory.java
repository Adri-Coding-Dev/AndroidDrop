package com.androiddrop.domain.usecase;

import com.androiddrop.domain.repository.TransferRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class StartTransferUseCase_Factory implements Factory<StartTransferUseCase> {
  private final Provider<TransferRepository> transferRepositoryProvider;

  public StartTransferUseCase_Factory(Provider<TransferRepository> transferRepositoryProvider) {
    this.transferRepositoryProvider = transferRepositoryProvider;
  }

  @Override
  public StartTransferUseCase get() {
    return newInstance(transferRepositoryProvider.get());
  }

  public static StartTransferUseCase_Factory create(
      Provider<TransferRepository> transferRepositoryProvider) {
    return new StartTransferUseCase_Factory(transferRepositoryProvider);
  }

  public static StartTransferUseCase newInstance(TransferRepository transferRepository) {
    return new StartTransferUseCase(transferRepository);
  }
}
