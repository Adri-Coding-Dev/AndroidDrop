package com.androiddrop.data.nearby;

import com.androiddrop.domain.repository.DeviceRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class NearbyModule_ProvideDeviceRepositoryFactory implements Factory<DeviceRepository> {
  private final Provider<NearbyDeviceRepository> repositoryProvider;

  public NearbyModule_ProvideDeviceRepositoryFactory(
      Provider<NearbyDeviceRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public DeviceRepository get() {
    return provideDeviceRepository(repositoryProvider.get());
  }

  public static NearbyModule_ProvideDeviceRepositoryFactory create(
      Provider<NearbyDeviceRepository> repositoryProvider) {
    return new NearbyModule_ProvideDeviceRepositoryFactory(repositoryProvider);
  }

  public static DeviceRepository provideDeviceRepository(NearbyDeviceRepository repository) {
    return Preconditions.checkNotNullFromProvides(NearbyModule.INSTANCE.provideDeviceRepository(repository));
  }
}
