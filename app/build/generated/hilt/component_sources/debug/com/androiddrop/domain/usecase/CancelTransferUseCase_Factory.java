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
public final class CancelTransferUseCase_Factory implements Factory<CancelTransferUseCase> {
  private final Provider<TransferRepository> transferRepositoryProvider;

  public CancelTransferUseCase_Factory(Provider<TransferRepository> transferRepositoryProvider) {
    this.transferRepositoryProvider = transferRepositoryProvider;
  }

  @Override
  public CancelTransferUseCase get() {
    return newInstance(transferRepositoryProvider.get());
  }

  public static CancelTransferUseCase_Factory create(
      Provider<TransferRepository> transferRepositoryProvider) {
    return new CancelTransferUseCase_Factory(transferRepositoryProvider);
  }

  public static CancelTransferUseCase newInstance(TransferRepository transferRepository) {
    return new CancelTransferUseCase(transferRepository);
  }
}
