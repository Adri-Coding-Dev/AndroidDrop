package com.androiddrop.data.nearby;

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
public final class NearbyDeviceRepository_Factory implements Factory<NearbyDeviceRepository> {
  private final Provider<Context> contextProvider;

  public NearbyDeviceRepository_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public NearbyDeviceRepository get() {
    return newInstance(contextProvider.get());
  }

  public static NearbyDeviceRepository_Factory create(Provider<Context> contextProvider) {
    return new NearbyDeviceRepository_Factory(contextProvider);
  }

  public static NearbyDeviceRepository newInstance(Context context) {
    return new NearbyDeviceRepository(context);
  }
}
