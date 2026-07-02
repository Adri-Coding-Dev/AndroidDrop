package com.androiddrop.service.transfer;

import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class TransferService_MembersInjector implements MembersInjector<TransferService> {
  private final Provider<TransferNotificationManager> notificationManagerProvider;

  public TransferService_MembersInjector(
      Provider<TransferNotificationManager> notificationManagerProvider) {
    this.notificationManagerProvider = notificationManagerProvider;
  }

  public static MembersInjector<TransferService> create(
      Provider<TransferNotificationManager> notificationManagerProvider) {
    return new TransferService_MembersInjector(notificationManagerProvider);
  }

  @Override
  public void injectMembers(TransferService instance) {
    injectNotificationManager(instance, notificationManagerProvider.get());
  }

  @InjectedFieldSignature("com.androiddrop.service.transfer.TransferService.notificationManager")
  public static void injectNotificationManager(TransferService instance,
      TransferNotificationManager notificationManager) {
    instance.notificationManager = notificationManager;
  }
}
