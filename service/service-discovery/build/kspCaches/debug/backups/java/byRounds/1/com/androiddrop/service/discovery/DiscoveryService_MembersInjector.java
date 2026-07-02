package com.androiddrop.service.discovery;

import android.content.Context;
import com.androiddrop.data.ble.BleAdvertiser;
import com.androiddrop.data.ble.BleScanner;
import dagger.MembersInjector;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class DiscoveryService_MembersInjector implements MembersInjector<DiscoveryService> {
  private final Provider<DiscoveryNotificationManager> notificationManagerProvider;

  private final Provider<BleAdvertiser> bleAdvertiserProvider;

  private final Provider<BleScanner> bleScannerProvider;

  private final Provider<Context> appContextProvider;

  public DiscoveryService_MembersInjector(
      Provider<DiscoveryNotificationManager> notificationManagerProvider,
      Provider<BleAdvertiser> bleAdvertiserProvider, Provider<BleScanner> bleScannerProvider,
      Provider<Context> appContextProvider) {
    this.notificationManagerProvider = notificationManagerProvider;
    this.bleAdvertiserProvider = bleAdvertiserProvider;
    this.bleScannerProvider = bleScannerProvider;
    this.appContextProvider = appContextProvider;
  }

  public static MembersInjector<DiscoveryService> create(
      Provider<DiscoveryNotificationManager> notificationManagerProvider,
      Provider<BleAdvertiser> bleAdvertiserProvider, Provider<BleScanner> bleScannerProvider,
      Provider<Context> appContextProvider) {
    return new DiscoveryService_MembersInjector(notificationManagerProvider, bleAdvertiserProvider, bleScannerProvider, appContextProvider);
  }

  @Override
  public void injectMembers(DiscoveryService instance) {
    injectNotificationManager(instance, notificationManagerProvider.get());
    injectBleAdvertiser(instance, bleAdvertiserProvider.get());
    injectBleScanner(instance, bleScannerProvider.get());
    injectAppContext(instance, appContextProvider.get());
  }

  @InjectedFieldSignature("com.androiddrop.service.discovery.DiscoveryService.notificationManager")
  public static void injectNotificationManager(DiscoveryService instance,
      DiscoveryNotificationManager notificationManager) {
    instance.notificationManager = notificationManager;
  }

  @InjectedFieldSignature("com.androiddrop.service.discovery.DiscoveryService.bleAdvertiser")
  public static void injectBleAdvertiser(DiscoveryService instance, BleAdvertiser bleAdvertiser) {
    instance.bleAdvertiser = bleAdvertiser;
  }

  @InjectedFieldSignature("com.androiddrop.service.discovery.DiscoveryService.bleScanner")
  public static void injectBleScanner(DiscoveryService instance, BleScanner bleScanner) {
    instance.bleScanner = bleScanner;
  }

  @InjectedFieldSignature("com.androiddrop.service.discovery.DiscoveryService.appContext")
  @ApplicationContext
  public static void injectAppContext(DiscoveryService instance, Context appContext) {
    instance.appContext = appContext;
  }
}
