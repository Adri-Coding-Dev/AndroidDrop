package com.androiddrop.data.filesystem;

import android.content.Context;
import com.androiddrop.domain.repository.FileRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class FileSystemModule_ProvideFileRepositoryFactory implements Factory<FileRepository> {
  private final Provider<Context> contextProvider;

  public FileSystemModule_ProvideFileRepositoryFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public FileRepository get() {
    return provideFileRepository(contextProvider.get());
  }

  public static FileSystemModule_ProvideFileRepositoryFactory create(
      Provider<Context> contextProvider) {
    return new FileSystemModule_ProvideFileRepositoryFactory(contextProvider);
  }

  public static FileRepository provideFileRepository(Context context) {
    return Preconditions.checkNotNullFromProvides(FileSystemModule.INSTANCE.provideFileRepository(context));
  }
}
