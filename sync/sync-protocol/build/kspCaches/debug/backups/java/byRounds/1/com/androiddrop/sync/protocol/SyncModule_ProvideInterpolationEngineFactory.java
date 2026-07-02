package com.androiddrop.sync.protocol;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class SyncModule_ProvideInterpolationEngineFactory implements Factory<InterpolationEngine> {
  @Override
  public InterpolationEngine get() {
    return provideInterpolationEngine();
  }

  public static SyncModule_ProvideInterpolationEngineFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static InterpolationEngine provideInterpolationEngine() {
    return Preconditions.checkNotNullFromProvides(SyncModule.INSTANCE.provideInterpolationEngine());
  }

  private static final class InstanceHolder {
    private static final SyncModule_ProvideInterpolationEngineFactory INSTANCE = new SyncModule_ProvideInterpolationEngineFactory();
  }
}
