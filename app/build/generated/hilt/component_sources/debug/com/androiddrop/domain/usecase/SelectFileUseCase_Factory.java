package com.androiddrop.domain.usecase;

import com.androiddrop.domain.repository.FileRepository;
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
public final class SelectFileUseCase_Factory implements Factory<SelectFileUseCase> {
  private final Provider<FileRepository> fileRepositoryProvider;

  public SelectFileUseCase_Factory(Provider<FileRepository> fileRepositoryProvider) {
    this.fileRepositoryProvider = fileRepositoryProvider;
  }

  @Override
  public SelectFileUseCase get() {
    return newInstance(fileRepositoryProvider.get());
  }

  public static SelectFileUseCase_Factory create(Provider<FileRepository> fileRepositoryProvider) {
    return new SelectFileUseCase_Factory(fileRepositoryProvider);
  }

  public static SelectFileUseCase newInstance(FileRepository fileRepository) {
    return new SelectFileUseCase(fileRepository);
  }
}
