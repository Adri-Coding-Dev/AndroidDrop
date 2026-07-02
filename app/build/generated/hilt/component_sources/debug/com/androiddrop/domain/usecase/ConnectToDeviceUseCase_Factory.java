package com.androiddrop.domain.usecase;

import com.androiddrop.domain.repository.DeviceRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("com.androiddrop.core.common.NearbyRepo")
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
public final class ConnectToDeviceUseCase_Factory implements Factory<ConnectToDeviceUseCase> {
  private final Provider<DeviceRepository> deviceRepositoryProvider;

  public ConnectToDeviceUseCase_Factory(Provider<DeviceRepository> deviceRepositoryProvider) {
    this.deviceRepositoryProvider = deviceRepositoryProvider;
  }

  @Override
  public ConnectToDeviceUseCase get() {
    return newInstance(deviceRepositoryProvider.get());
  }

  public static ConnectToDeviceUseCase_Factory create(
      Provider<DeviceRepository> deviceRepositoryProvider) {
    return new ConnectToDeviceUseCase_Factory(deviceRepositoryProvider);
  }

  public static ConnectToDeviceUseCase newInstance(DeviceRepository deviceRepository) {
    return new ConnectToDeviceUseCase(deviceRepository);
  }
}
