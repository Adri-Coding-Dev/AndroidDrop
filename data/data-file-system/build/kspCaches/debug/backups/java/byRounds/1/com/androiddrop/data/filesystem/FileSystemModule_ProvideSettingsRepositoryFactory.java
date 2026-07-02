package com.androiddrop.data.filesystem;

import com.androiddrop.domain.repository.SettingsRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class FileSystemModule_ProvideSettingsRepositoryFactory implements Factory<SettingsRepository> {
  private final Provider<SettingsRepositoryImpl> implProvider;

  public FileSystemModule_ProvideSettingsRepositoryFactory(
      Provider<SettingsRepositoryImpl> implProvider) {
    this.implProvider = implProvider;
  }

  @Override
  public SettingsRepository get() {
    return provideSettingsRepository(implProvider.get());
  }

  public static FileSystemModule_ProvideSettingsRepositoryFactory create(
      Provider<SettingsRepositoryImpl> implProvider) {
    return new FileSystemModule_ProvideSettingsRepositoryFactory(implProvider);
  }

  public static SettingsRepository provideSettingsRepository(SettingsRepositoryImpl impl) {
    return Preconditions.checkNotNullFromProvides(FileSystemModule.INSTANCE.provideSettingsRepository(impl));
  }
}
