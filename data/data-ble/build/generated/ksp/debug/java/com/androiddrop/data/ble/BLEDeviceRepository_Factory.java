package com.androiddrop.data.ble;

import com.androiddrop.core.crypto.CryptoManager;
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
public final class BLEDeviceRepository_Factory implements Factory<BLEDeviceRepository> {
  private final Provider<BleAdvertiser> advertiserProvider;

  private final Provider<BleScanner> scannerProvider;

  private final Provider<BleGattServer> gattServerProvider;

  private final Provider<BleGattClient> gattClientProvider;

  private final Provider<CryptoManager> cryptoManagerProvider;

  public BLEDeviceRepository_Factory(Provider<BleAdvertiser> advertiserProvider,
      Provider<BleScanner> scannerProvider, Provider<BleGattServer> gattServerProvider,
      Provider<BleGattClient> gattClientProvider, Provider<CryptoManager> cryptoManagerProvider) {
    this.advertiserProvider = advertiserProvider;
    this.scannerProvider = scannerProvider;
    this.gattServerProvider = gattServerProvider;
    this.gattClientProvider = gattClientProvider;
    this.cryptoManagerProvider = cryptoManagerProvider;
  }

  @Override
  public BLEDeviceRepository get() {
    return newInstance(advertiserProvider.get(), scannerProvider.get(), gattServerProvider.get(), gattClientProvider.get(), cryptoManagerProvider.get());
  }

  public static BLEDeviceRepository_Factory create(Provider<BleAdvertiser> advertiserProvider,
      Provider<BleScanner> scannerProvider, Provider<BleGattServer> gattServerProvider,
      Provider<BleGattClient> gattClientProvider, Provider<CryptoManager> cryptoManagerProvider) {
    return new BLEDeviceRepository_Factory(advertiserProvider, scannerProvider, gattServerProvider, gattClientProvider, cryptoManagerProvider);
  }

  public static BLEDeviceRepository newInstance(BleAdvertiser advertiser, BleScanner scanner,
      BleGattServer gattServer, BleGattClient gattClient, CryptoManager cryptoManager) {
    return new BLEDeviceRepository(advertiser, scanner, gattServer, gattClient, cryptoManager);
  }
}
