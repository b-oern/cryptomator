/*******************************************************************************
 * Copyright (c) 2016, 2017 Sebastian Stenzel and others.
 * All rights reserved.
 * This program and the accompanying materials are made available under the terms of the accompanying LICENSE file.
 *
 * Contributors:
 *     Sebastian Stenzel - initial API and implementation
 *******************************************************************************/
package org.cryptomator.ui;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import javafx.beans.binding.Binding;
import org.apache.commons.lang3.SystemUtils;
import org.cryptomator.common.CommonsModule;
import org.cryptomator.common.settings.Settings;
import org.cryptomator.common.settings.SettingsProvider;
import org.cryptomator.frontend.webdav.WebDavServer;
import org.cryptomator.keychain.KeychainModule;
import org.cryptomator.ui.controllers.ViewControllerModule;
import org.cryptomator.ui.model.VaultComponent;
import org.fxmisc.easybind.EasyBind;

@Module(includes = {ViewControllerModule.class, CommonsModule.class, KeychainModule.class}, subcomponents = {VaultComponent.class})
public class UiModule {

	@Provides
	@Singleton
	Settings provideSettings(SettingsProvider settingsProvider) {
		return settingsProvider.get();
	}

	@Provides
	@Singleton
	ExecutorService provideExecutorService(@Named("shutdownTaskScheduler") Consumer<Runnable> shutdownTaskScheduler) {
		ExecutorService executorService = Executors.newCachedThreadPool();
		shutdownTaskScheduler.accept(executorService::shutdown);
		return executorService;
	}

	@Provides
	@Singleton
	Binding<InetSocketAddress> provideServerSocketAddressBinding(Settings settings) {
		return EasyBind.map(settings.port(), (Number port) -> {
			String host = SystemUtils.IS_OS_WINDOWS ? "127.0.0.1" : "localhost";
			return InetSocketAddress.createUnresolved(host, port.intValue());
		});
	}

	@Provides
	@Singleton
	WebDavServer provideWebDavServer(Binding<InetSocketAddress> serverSocketAddressBinding) {
		WebDavServer server = WebDavServer.create();
		// no need to unsubscribe eventually, because server is a singleton
		EasyBind.subscribe(serverSocketAddressBinding, server::bind);
		return server;
	}

}
