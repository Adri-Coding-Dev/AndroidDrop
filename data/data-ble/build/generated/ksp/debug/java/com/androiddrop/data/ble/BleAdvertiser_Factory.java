package com.androiddrop.data.ble;

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
public final class BleAdvertiser_Factory implements Factory<BleAdvertiser> {
  private final Provider<Context> contextProvider;

  public BleAdvertiser_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public BleAdvertiser get() {
    return newInstance(contextProvider.get());
  }

  public static BleAdvertiser_Factory create(Provider<Context> contextProvider) {
    return new BleAdvertiser_Factory(contextProvider);
  }

  public static BleAdvertiser newInstance(Context context) {
    return new BleAdvertiser(context);
  }
}
