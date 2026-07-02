package com.androiddrop.feature.discovery;

import com.androiddrop.domain.usecase.ConnectToDeviceUseCase;
import com.androiddrop.domain.usecase.DiscoverDevicesUseCase;
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
public final class DiscoveryViewModel_Factory implements Factory<DiscoveryViewModel> {
  private final Provider<DiscoverDevicesUseCase> discoverDevicesUseCaseProvider;

  private final Provider<ConnectToDeviceUseCase> connectToDeviceUseCaseProvider;

  public DiscoveryViewModel_Factory(Provider<DiscoverDevicesUseCase> discoverDevicesUseCaseProvider,
      Provider<ConnectToDeviceUseCase> connectToDeviceUseCaseProvider) {
    this.discoverDevicesUseCaseProvider = discoverDevicesUseCaseProvider;
    this.connectToDeviceUseCaseProvider = connectToDeviceUseCaseProvider;
  }

  @Override
  public DiscoveryViewModel get() {
    return newInstance(discoverDevicesUseCaseProvider.get(), connectToDeviceUseCaseProvider.get());
  }

  public static DiscoveryViewModel_Factory create(
      Provider<DiscoverDevicesUseCase> discoverDevicesUseCaseProvider,
      Provider<ConnectToDeviceUseCase> connectToDeviceUseCaseProvider) {
    return new DiscoveryViewModel_Factory(discoverDevicesUseCaseProvider, connectToDeviceUseCaseProvider);
  }

  public static DiscoveryViewModel newInstance(DiscoverDevicesUseCase discoverDevicesUseCase,
      ConnectToDeviceUseCase connectToDeviceUseCase) {
    return new DiscoveryViewModel(discoverDevicesUseCase, connectToDeviceUseCase);
  }
}
