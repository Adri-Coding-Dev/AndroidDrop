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
public final class SyncModule_ProvideClockSynchronizerFactory implements Factory<ClockSynchronizer> {
  @Override
  public ClockSynchronizer get() {
    return provideClockSynchronizer();
  }

  public static SyncModule_ProvideClockSynchronizerFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ClockSynchronizer provideClockSynchronizer() {
    return Preconditions.checkNotNullFromProvides(SyncModule.INSTANCE.provideClockSynchronizer());
  }

  private static final class InstanceHolder {
    private static final SyncModule_ProvideClockSynchronizerFactory INSTANCE = new SyncModule_ProvideClockSynchronizerFactory();
  }
}
