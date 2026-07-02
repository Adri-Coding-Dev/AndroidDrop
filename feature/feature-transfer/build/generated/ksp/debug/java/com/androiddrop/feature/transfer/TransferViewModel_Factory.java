package com.androiddrop.feature.transfer;

import com.androiddrop.domain.usecase.CancelTransferUseCase;
import com.androiddrop.domain.usecase.ConnectToDeviceUseCase;
import com.androiddrop.domain.usecase.DiscoverDevicesUseCase;
import com.androiddrop.domain.usecase.StartTransferUseCase;
import com.androiddrop.domain.usecase.SyncAnimationUseCase;
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
public final class TransferViewModel_Factory implements Factory<TransferViewModel> {
  private final Provider<StartTransferUseCase> startTransferUseCaseProvider;

  private final Provider<CancelTransferUseCase> cancelTransferUseCaseProvider;

  private final Provider<DiscoverDevicesUseCase> discoverDevicesUseCaseProvider;

  private final Provider<ConnectToDeviceUseCase> connectToDeviceUseCaseProvider;

  private final Provider<SyncAnimationUseCase> syncAnimationUseCaseProvider;

  public TransferViewModel_Factory(Provider<StartTransferUseCase> startTransferUseCaseProvider,
      Provider<CancelTransferUseCase> cancelTransferUseCaseProvider,
      Provider<DiscoverDevicesUseCase> discoverDevicesUseCaseProvider,
      Provider<ConnectToDeviceUseCase> connectToDeviceUseCaseProvider,
      Provider<SyncAnimationUseCase> syncAnimationUseCaseProvider) {
    this.startTransferUseCaseProvider = startTransferUseCaseProvider;
    this.cancelTransferUseCaseProvider = cancelTransferUseCaseProvider;
    this.discoverDevicesUseCaseProvider = discoverDevicesUseCaseProvider;
    this.connectToDeviceUseCaseProvider = connectToDeviceUseCaseProvider;
    this.syncAnimationUseCaseProvider = syncAnimationUseCaseProvider;
  }

  @Override
  public TransferViewModel get() {
    return newInstance(startTransferUseCaseProvider.get(), cancelTransferUseCaseProvider.get(), discoverDevicesUseCaseProvider.get(), connectToDeviceUseCaseProvider.get(), syncAnimationUseCaseProvider.get());
  }

  public static TransferViewModel_Factory create(
      Provider<StartTransferUseCase> startTransferUseCaseProvider,
      Provider<CancelTransferUseCase> cancelTransferUseCaseProvider,
      Provider<DiscoverDevicesUseCase> discoverDevicesUseCaseProvider,
      Provider<ConnectToDeviceUseCase> connectToDeviceUseCaseProvider,
      Provider<SyncAnimationUseCase> syncAnimationUseCaseProvider) {
    return new TransferViewModel_Factory(startTransferUseCaseProvider, cancelTransferUseCaseProvider, discoverDevicesUseCaseProvider, connectToDeviceUseCaseProvider, syncAnimationUseCaseProvider);
  }

  public static TransferViewModel newInstance(StartTransferUseCase startTransferUseCase,
      CancelTransferUseCase cancelTransferUseCase, DiscoverDevicesUseCase discoverDevicesUseCase,
      ConnectToDeviceUseCase connectToDeviceUseCase, SyncAnimationUseCase syncAnimationUseCase) {
    return new TransferViewModel(startTransferUseCase, cancelTransferUseCase, discoverDevicesUseCase, connectToDeviceUseCase, syncAnimationUseCase);
  }
}
