package com.androiddrop.data.filesystem;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class FileRepositoryImpl_Factory implements Factory<FileRepositoryImpl> {
  private final Provider<Context> contextProvider;

  public FileRepositoryImpl_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public FileRepositoryImpl get() {
    return newInstance(contextProvider.get());
  }

  public static FileRepositoryImpl_Factory create(Provider<Context> contextProvider) {
    return new FileRepositoryImpl_Factory(contextProvider);
  }

  public static FileRepositoryImpl newInstance(Context context) {
    return new FileRepositoryImpl(context);
  }
}
