package com.androiddrop.data.ble;

import com.androiddrop.domain.repository.DeviceRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("com.androiddrop.core.common.BleRepo")
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
public final class BLEModule_ProvideDeviceRepositoryFactory implements Factory<DeviceRepository> {
  private final Provider<BLEDeviceRepository> repositoryProvider;

  public BLEModule_ProvideDeviceRepositoryFactory(
      Provider<BLEDeviceRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public DeviceRepository get() {
    return provideDeviceRepository(repositoryProvider.get());
  }

  public static BLEModule_ProvideDeviceRepositoryFactory create(
      Provider<BLEDeviceRepository> repositoryProvider) {
    return new BLEModule_ProvideDeviceRepositoryFactory(repositoryProvider);
  }

  public static DeviceRepository provideDeviceRepository(BLEDeviceRepository repository) {
    return Preconditions.checkNotNullFromProvides(BLEModule.INSTANCE.provideDeviceRepository(repository));
  }
}
