package com.androiddrop.feature.fileexplorer;

import com.androiddrop.domain.usecase.SelectFileUseCase;
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
public final class FileExplorerViewModel_Factory implements Factory<FileExplorerViewModel> {
  private final Provider<SelectFileUseCase> selectFileUseCaseProvider;

  public FileExplorerViewModel_Factory(Provider<SelectFileUseCase> selectFileUseCaseProvider) {
    this.selectFileUseCaseProvider = selectFileUseCaseProvider;
  }

  @Override
  public FileExplorerViewModel get() {
    return newInstance(selectFileUseCaseProvider.get());
  }

  public static FileExplorerViewModel_Factory create(
      Provider<SelectFileUseCase> selectFileUseCaseProvider) {
    return new FileExplorerViewModel_Factory(selectFileUseCaseProvider);
  }

  public static FileExplorerViewModel newInstance(SelectFileUseCase selectFileUseCase) {
    return new FileExplorerViewModel(selectFileUseCase);
  }
}
