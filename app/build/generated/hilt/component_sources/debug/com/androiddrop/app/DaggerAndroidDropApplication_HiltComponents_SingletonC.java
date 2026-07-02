package com.androiddrop.app;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import com.androiddrop.core.crypto.CoreCryptoModule_Companion_ProvideSecureRandomFactory;
import com.androiddrop.core.crypto.CryptoManager;
import com.androiddrop.core.crypto.EcdhCryptoManager;
import com.androiddrop.core.network.NetworkModule_Companion_ProvideOkHttpClientFactory;
import com.androiddrop.core.network.SocketManager;
import com.androiddrop.core.network.TcpSocketManager;
import com.androiddrop.data.ble.BleAdvertiser;
import com.androiddrop.data.ble.BleScanner;
import com.androiddrop.data.filesystem.FileSystemModule_ProvideFileRepositoryFactory;
import com.androiddrop.data.filesystem.FileSystemModule_ProvideSettingsRepositoryFactory;
import com.androiddrop.data.filesystem.SettingsRepositoryImpl;
import com.androiddrop.data.nearby.NearbyDeviceRepository;
import com.androiddrop.data.nearby.NearbyModule_ProvideDeviceRepositoryFactory;
import com.androiddrop.data.transfer.ChunkedTransferEngine;
import com.androiddrop.data.transfer.TransferModule_ProvideTransferRepositoryFactory;
import com.androiddrop.data.transfer.TransferRepositoryImpl;
import com.androiddrop.data.transfer.TransferSessionManager;
import com.androiddrop.domain.repository.DeviceRepository;
import com.androiddrop.domain.repository.FileRepository;
import com.androiddrop.domain.repository.SettingsRepository;
import com.androiddrop.domain.repository.SyncRepository;
import com.androiddrop.domain.repository.TransferRepository;
import com.androiddrop.domain.usecase.CancelTransferUseCase;
import com.androiddrop.domain.usecase.ConnectToDeviceUseCase;
import com.androiddrop.domain.usecase.DiscoverDevicesUseCase;
import com.androiddrop.domain.usecase.SelectFileUseCase;
import com.androiddrop.domain.usecase.StartTransferUseCase;
import com.androiddrop.domain.usecase.SyncAnimationUseCase;
import com.androiddrop.feature.diagnostics.DiagnosticsViewModel;
import com.androiddrop.feature.diagnostics.DiagnosticsViewModel_HiltModules;
import com.androiddrop.feature.discovery.DiscoveryViewModel;
import com.androiddrop.feature.discovery.DiscoveryViewModel_HiltModules;
import com.androiddrop.feature.fileexplorer.FileExplorerViewModel;
import com.androiddrop.feature.fileexplorer.FileExplorerViewModel_HiltModules;
import com.androiddrop.feature.settings.SettingsViewModel;
import com.androiddrop.feature.settings.SettingsViewModel_HiltModules;
import com.androiddrop.feature.transfer.TransferViewModel;
import com.androiddrop.feature.transfer.TransferViewModel_HiltModules;
import com.androiddrop.service.discovery.DiscoveryNotificationManager;
import com.androiddrop.service.discovery.DiscoveryService;
import com.androiddrop.service.discovery.DiscoveryService_MembersInjector;
import com.androiddrop.service.transfer.TransferNotificationManager;
import com.androiddrop.service.transfer.TransferService;
import com.androiddrop.service.transfer.TransferService_MembersInjector;
import com.androiddrop.sync.protocol.SyncModule_ProvideSyncProtocolFactory;
import com.androiddrop.sync.protocol.SyncModule_ProvideSyncRepositoryFactory;
import com.androiddrop.sync.protocol.SyncProtocol;
import com.androiddrop.sync.protocol.SyncRepositoryImpl;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.IdentifierNameString;
import dagger.internal.KeepFieldType;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;

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
public final class DaggerAndroidDropApplication_HiltComponents_SingletonC {
  private DaggerAndroidDropApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public AndroidDropApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements AndroidDropApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public AndroidDropApplication_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements AndroidDropApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public AndroidDropApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements AndroidDropApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public AndroidDropApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements AndroidDropApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public AndroidDropApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements AndroidDropApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public AndroidDropApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements AndroidDropApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public AndroidDropApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements AndroidDropApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public AndroidDropApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends AndroidDropApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends AndroidDropApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends AndroidDropApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends AndroidDropApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity arg0) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(5).put(LazyClassKeyProvider.com_androiddrop_feature_diagnostics_DiagnosticsViewModel, DiagnosticsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_androiddrop_feature_discovery_DiscoveryViewModel, DiscoveryViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_androiddrop_feature_fileexplorer_FileExplorerViewModel, FileExplorerViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_androiddrop_feature_settings_SettingsViewModel, SettingsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_androiddrop_feature_transfer_TransferViewModel, TransferViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_androiddrop_feature_fileexplorer_FileExplorerViewModel = "com.androiddrop.feature.fileexplorer.FileExplorerViewModel";

      static String com_androiddrop_feature_settings_SettingsViewModel = "com.androiddrop.feature.settings.SettingsViewModel";

      static String com_androiddrop_feature_diagnostics_DiagnosticsViewModel = "com.androiddrop.feature.diagnostics.DiagnosticsViewModel";

      static String com_androiddrop_feature_transfer_TransferViewModel = "com.androiddrop.feature.transfer.TransferViewModel";

      static String com_androiddrop_feature_discovery_DiscoveryViewModel = "com.androiddrop.feature.discovery.DiscoveryViewModel";

      @KeepFieldType
      FileExplorerViewModel com_androiddrop_feature_fileexplorer_FileExplorerViewModel2;

      @KeepFieldType
      SettingsViewModel com_androiddrop_feature_settings_SettingsViewModel2;

      @KeepFieldType
      DiagnosticsViewModel com_androiddrop_feature_diagnostics_DiagnosticsViewModel2;

      @KeepFieldType
      TransferViewModel com_androiddrop_feature_transfer_TransferViewModel2;

      @KeepFieldType
      DiscoveryViewModel com_androiddrop_feature_discovery_DiscoveryViewModel2;
    }
  }

  private static final class ViewModelCImpl extends AndroidDropApplication_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<DiagnosticsViewModel> diagnosticsViewModelProvider;

    private Provider<DiscoveryViewModel> discoveryViewModelProvider;

    private Provider<FileExplorerViewModel> fileExplorerViewModelProvider;

    private Provider<SettingsViewModel> settingsViewModelProvider;

    private Provider<TransferViewModel> transferViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    private DiscoverDevicesUseCase discoverDevicesUseCase() {
      return new DiscoverDevicesUseCase(singletonCImpl.provideDeviceRepositoryProvider.get());
    }

    private ConnectToDeviceUseCase connectToDeviceUseCase() {
      return new ConnectToDeviceUseCase(singletonCImpl.provideDeviceRepositoryProvider.get());
    }

    private SelectFileUseCase selectFileUseCase() {
      return new SelectFileUseCase(singletonCImpl.provideFileRepositoryProvider.get());
    }

    private StartTransferUseCase startTransferUseCase() {
      return new StartTransferUseCase(singletonCImpl.provideTransferRepositoryProvider.get());
    }

    private CancelTransferUseCase cancelTransferUseCase() {
      return new CancelTransferUseCase(singletonCImpl.provideTransferRepositoryProvider.get());
    }

    private SyncAnimationUseCase syncAnimationUseCase() {
      return new SyncAnimationUseCase(singletonCImpl.provideSyncRepositoryProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.diagnosticsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.discoveryViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.fileExplorerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.transferViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(5).put(LazyClassKeyProvider.com_androiddrop_feature_diagnostics_DiagnosticsViewModel, ((Provider) diagnosticsViewModelProvider)).put(LazyClassKeyProvider.com_androiddrop_feature_discovery_DiscoveryViewModel, ((Provider) discoveryViewModelProvider)).put(LazyClassKeyProvider.com_androiddrop_feature_fileexplorer_FileExplorerViewModel, ((Provider) fileExplorerViewModelProvider)).put(LazyClassKeyProvider.com_androiddrop_feature_settings_SettingsViewModel, ((Provider) settingsViewModelProvider)).put(LazyClassKeyProvider.com_androiddrop_feature_transfer_TransferViewModel, ((Provider) transferViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_androiddrop_feature_transfer_TransferViewModel = "com.androiddrop.feature.transfer.TransferViewModel";

      static String com_androiddrop_feature_settings_SettingsViewModel = "com.androiddrop.feature.settings.SettingsViewModel";

      static String com_androiddrop_feature_fileexplorer_FileExplorerViewModel = "com.androiddrop.feature.fileexplorer.FileExplorerViewModel";

      static String com_androiddrop_feature_discovery_DiscoveryViewModel = "com.androiddrop.feature.discovery.DiscoveryViewModel";

      static String com_androiddrop_feature_diagnostics_DiagnosticsViewModel = "com.androiddrop.feature.diagnostics.DiagnosticsViewModel";

      @KeepFieldType
      TransferViewModel com_androiddrop_feature_transfer_TransferViewModel2;

      @KeepFieldType
      SettingsViewModel com_androiddrop_feature_settings_SettingsViewModel2;

      @KeepFieldType
      FileExplorerViewModel com_androiddrop_feature_fileexplorer_FileExplorerViewModel2;

      @KeepFieldType
      DiscoveryViewModel com_androiddrop_feature_discovery_DiscoveryViewModel2;

      @KeepFieldType
      DiagnosticsViewModel com_androiddrop_feature_diagnostics_DiagnosticsViewModel2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.androiddrop.feature.diagnostics.DiagnosticsViewModel 
          return (T) new DiagnosticsViewModel(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 1: // com.androiddrop.feature.discovery.DiscoveryViewModel 
          return (T) new DiscoveryViewModel(viewModelCImpl.discoverDevicesUseCase(), viewModelCImpl.connectToDeviceUseCase());

          case 2: // com.androiddrop.feature.fileexplorer.FileExplorerViewModel 
          return (T) new FileExplorerViewModel(viewModelCImpl.selectFileUseCase());

          case 3: // com.androiddrop.feature.settings.SettingsViewModel 
          return (T) new SettingsViewModel(singletonCImpl.provideSettingsRepositoryProvider.get(), ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 4: // com.androiddrop.feature.transfer.TransferViewModel 
          return (T) new TransferViewModel(viewModelCImpl.startTransferUseCase(), viewModelCImpl.cancelTransferUseCase(), viewModelCImpl.discoverDevicesUseCase(), viewModelCImpl.connectToDeviceUseCase(), viewModelCImpl.syncAnimationUseCase());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends AndroidDropApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends AndroidDropApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }

    private BleAdvertiser bleAdvertiser() {
      return new BleAdvertiser(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));
    }

    private BleScanner bleScanner() {
      return new BleScanner(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));
    }

    @Override
    public void injectDiscoveryService(DiscoveryService arg0) {
      injectDiscoveryService2(arg0);
    }

    @Override
    public void injectTransferService(TransferService arg0) {
      injectTransferService2(arg0);
    }

    private DiscoveryService injectDiscoveryService2(DiscoveryService instance) {
      DiscoveryService_MembersInjector.injectNotificationManager(instance, singletonCImpl.discoveryNotificationManagerProvider.get());
      DiscoveryService_MembersInjector.injectBleAdvertiser(instance, bleAdvertiser());
      DiscoveryService_MembersInjector.injectBleScanner(instance, bleScanner());
      DiscoveryService_MembersInjector.injectAppContext(instance, ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));
      return instance;
    }

    private TransferService injectTransferService2(TransferService instance) {
      TransferService_MembersInjector.injectNotificationManager(instance, singletonCImpl.transferNotificationManagerProvider.get());
      return instance;
    }
  }

  private static final class SingletonCImpl extends AndroidDropApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<DeviceRepository> provideDeviceRepositoryProvider;

    private Provider<FileRepository> provideFileRepositoryProvider;

    private Provider<SettingsRepositoryImpl> settingsRepositoryImplProvider;

    private Provider<SettingsRepository> provideSettingsRepositoryProvider;

    private Provider<SecureRandom> provideSecureRandomProvider;

    private Provider<EcdhCryptoManager> ecdhCryptoManagerProvider;

    private Provider<CryptoManager> bindCryptoManagerProvider;

    private Provider<TransferRepository> provideTransferRepositoryProvider;

    private Provider<OkHttpClient> provideOkHttpClientProvider;

    private Provider<TcpSocketManager> tcpSocketManagerProvider;

    private Provider<SocketManager> bindSocketManagerProvider;

    private Provider<SyncProtocol> provideSyncProtocolProvider;

    private Provider<SyncRepositoryImpl> syncRepositoryImplProvider;

    private Provider<SyncRepository> provideSyncRepositoryProvider;

    private Provider<DiscoveryNotificationManager> discoveryNotificationManagerProvider;

    private Provider<TransferNotificationManager> transferNotificationManagerProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    private NearbyDeviceRepository nearbyDeviceRepository() {
      return new NearbyDeviceRepository(ApplicationContextModule_ProvideContextFactory.provideContext(applicationContextModule));
    }

    private TransferSessionManager transferSessionManager() {
      return new TransferSessionManager(bindCryptoManagerProvider.get());
    }

    private ChunkedTransferEngine chunkedTransferEngine() {
      return new ChunkedTransferEngine(bindCryptoManagerProvider.get(), transferSessionManager());
    }

    private TransferRepositoryImpl transferRepositoryImpl() {
      return new TransferRepositoryImpl(transferSessionManager(), chunkedTransferEngine());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideDeviceRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<DeviceRepository>(singletonCImpl, 0));
      this.provideFileRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<FileRepository>(singletonCImpl, 1));
      this.settingsRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<SettingsRepositoryImpl>(singletonCImpl, 3));
      this.provideSettingsRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<SettingsRepository>(singletonCImpl, 2));
      this.provideSecureRandomProvider = DoubleCheck.provider(new SwitchingProvider<SecureRandom>(singletonCImpl, 6));
      this.ecdhCryptoManagerProvider = new SwitchingProvider<>(singletonCImpl, 5);
      this.bindCryptoManagerProvider = DoubleCheck.provider((Provider) ecdhCryptoManagerProvider);
      this.provideTransferRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<TransferRepository>(singletonCImpl, 4));
      this.provideOkHttpClientProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 11));
      this.tcpSocketManagerProvider = new SwitchingProvider<>(singletonCImpl, 10);
      this.bindSocketManagerProvider = DoubleCheck.provider((Provider) tcpSocketManagerProvider);
      this.provideSyncProtocolProvider = DoubleCheck.provider(new SwitchingProvider<SyncProtocol>(singletonCImpl, 9));
      this.syncRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<SyncRepositoryImpl>(singletonCImpl, 8));
      this.provideSyncRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<SyncRepository>(singletonCImpl, 7));
      this.discoveryNotificationManagerProvider = DoubleCheck.provider(new SwitchingProvider<DiscoveryNotificationManager>(singletonCImpl, 12));
      this.transferNotificationManagerProvider = DoubleCheck.provider(new SwitchingProvider<TransferNotificationManager>(singletonCImpl, 13));
    }

    @Override
    public void injectAndroidDropApplication(AndroidDropApplication androidDropApplication) {
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // @com.androiddrop.core.common.NearbyRepo com.androiddrop.domain.repository.DeviceRepository 
          return (T) NearbyModule_ProvideDeviceRepositoryFactory.provideDeviceRepository(singletonCImpl.nearbyDeviceRepository());

          case 1: // com.androiddrop.domain.repository.FileRepository 
          return (T) FileSystemModule_ProvideFileRepositoryFactory.provideFileRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 2: // com.androiddrop.domain.repository.SettingsRepository 
          return (T) FileSystemModule_ProvideSettingsRepositoryFactory.provideSettingsRepository(singletonCImpl.settingsRepositoryImplProvider.get());

          case 3: // com.androiddrop.data.filesystem.SettingsRepositoryImpl 
          return (T) new SettingsRepositoryImpl(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 4: // @com.androiddrop.core.common.DefaultRepo com.androiddrop.domain.repository.TransferRepository 
          return (T) TransferModule_ProvideTransferRepositoryFactory.provideTransferRepository(singletonCImpl.transferRepositoryImpl());

          case 5: // com.androiddrop.core.crypto.EcdhCryptoManager 
          return (T) new EcdhCryptoManager(singletonCImpl.provideSecureRandomProvider.get());

          case 6: // java.security.SecureRandom 
          return (T) CoreCryptoModule_Companion_ProvideSecureRandomFactory.provideSecureRandom();

          case 7: // com.androiddrop.domain.repository.SyncRepository 
          return (T) SyncModule_ProvideSyncRepositoryFactory.provideSyncRepository(singletonCImpl.syncRepositoryImplProvider.get());

          case 8: // com.androiddrop.sync.protocol.SyncRepositoryImpl 
          return (T) new SyncRepositoryImpl(singletonCImpl.provideSyncProtocolProvider.get());

          case 9: // com.androiddrop.sync.protocol.SyncProtocol 
          return (T) SyncModule_ProvideSyncProtocolFactory.provideSyncProtocol(singletonCImpl.bindSocketManagerProvider.get());

          case 10: // com.androiddrop.core.network.TcpSocketManager 
          return (T) new TcpSocketManager(singletonCImpl.provideOkHttpClientProvider.get());

          case 11: // okhttp3.OkHttpClient 
          return (T) NetworkModule_Companion_ProvideOkHttpClientFactory.provideOkHttpClient();

          case 12: // com.androiddrop.service.discovery.DiscoveryNotificationManager 
          return (T) new DiscoveryNotificationManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 13: // com.androiddrop.service.transfer.TransferNotificationManager 
          return (T) new TransferNotificationManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
