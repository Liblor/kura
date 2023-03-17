/*******************************************************************************
 * Copyright (c) 2023 Eurotech and/or its affiliates and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Eurotech
 *******************************************************************************/
package org.eclipse.kura.nm.configuration;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.configuration.ComponentConfiguration;
import org.eclipse.kura.configuration.Password;
import org.eclipse.kura.configuration.SelfConfiguringComponent;
import org.eclipse.kura.core.configuration.ComponentConfigurationImpl;
import org.eclipse.kura.crypto.CryptoService;
import org.eclipse.kura.executor.CommandExecutorService;
import org.eclipse.kura.linux.net.util.LinuxNetworkUtil;
import org.eclipse.kura.net.NetInterface;
import org.eclipse.kura.net.NetInterfaceAddress;
import org.eclipse.kura.net.NetInterfaceStatus;
import org.eclipse.kura.net.NetInterfaceType;
import org.eclipse.kura.net.NetworkService;
import org.eclipse.kura.net.configuration.NetworkConfigurationServiceCommon;
import org.eclipse.kura.nm.NMDbusConnector;
import org.eclipse.kura.nm.NetworkProperties;
import org.eclipse.kura.nm.configuration.event.NetworkConfigurationChangeEvent;
import org.eclipse.kura.nm.configuration.monitor.DhcpServerMonitor;
import org.eclipse.kura.nm.configuration.writer.DhcpServerConfigWriter;
import org.eclipse.kura.usb.UsbDevice;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NMConfigurationServiceImpl implements SelfConfiguringComponent {

    private static final Logger logger = LoggerFactory.getLogger(NMConfigurationServiceImpl.class);

    private static final String PREFIX = "net.interface.";
    private static final String NET_INTERFACES = "net.interfaces";
    private static final String MODIFIED_INTERFACE_NAMES = "modified.interface.names";
    private static final String MODEM_PORT_REGEX = "^\\d+-\\d+";
    private static final Pattern PPP_INTERFACE = Pattern.compile("ppp\\d+");

    private NetworkService networkService;
    private EventAdmin eventAdmin;
    private CommandExecutorService commandExecutorService;
    private CryptoService cryptoService;
    private DhcpServerMonitor dhcpServerMonitor;

    private LinuxNetworkUtil linuxNetworkUtil;
    private NetworkProperties networkProperties;

    private NMDbusConnector nmDbusConnector;

    // ----------------------------------------------------------------
    //
    // Dependencies
    //
    // ----------------------------------------------------------------
    public void setNetworkService(NetworkService networkService) {
        this.networkService = networkService;
    }

    public void unsetNetworkService(NetworkService networkService) {
        if (this.networkService.equals(networkService)) {
            this.networkService = null;
        }
    }

    public void setEventAdmin(EventAdmin eventAdmin) {
        this.eventAdmin = eventAdmin;
    }

    public void unsetEventAdmin(EventAdmin eventAdmin) {
        if (this.eventAdmin.equals(eventAdmin)) {
            this.eventAdmin = null;
        }
    }

    public void setExecutorService(CommandExecutorService executorService) {
        this.commandExecutorService = executorService;
    }

    public void unsetExecutorService(CommandExecutorService executorService) {
        if (this.commandExecutorService.equals(executorService)) {
            this.commandExecutorService = null;
        }
    }

    public void setCryptoService(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    public void unsetCryptoService(CryptoService cryptoService) {
        if (this.cryptoService.equals(cryptoService)) {
            this.cryptoService = null;
        }
    }

    public NMConfigurationServiceImpl() {
        try {
            this.nmDbusConnector = NMDbusConnector.getInstance();
        } catch (DBusExecutionException | DBusException e) {
            logger.error("Cannot initialize NMDbusConnector due to: ", e);
        }
    }

    public NMConfigurationServiceImpl(NMDbusConnector nmDbusConnector) {
        this.nmDbusConnector = Objects.requireNonNull(nmDbusConnector);
    }

    // ----------------------------------------------------------------
    //
    // Activation APIs
    //
    // ----------------------------------------------------------------
    public void activate(ComponentContext componentContext, Map<String, Object> properties) {
        logger.debug("Activate NetworkConfigurationService...");

        this.linuxNetworkUtil = new LinuxNetworkUtil(this.commandExecutorService);
        this.dhcpServerMonitor = new DhcpServerMonitor(this.commandExecutorService);

        if (Objects.nonNull(this.nmDbusConnector)) {
            try {
                this.nmDbusConnector.checkPermissions();
            } catch (DBusExecutionException e) {
                logger.error("Cannot check NetworkManager permissions due to: ", e);
            }
        } else {
            logger.warn("Detected null NMDbusConnector, some network configuration functionalities will not work.");
        }

        if (properties == null) {
            logger.debug("Received null properties...");
        } else {
            logger.debug("Properties... {}", properties);
            this.networkProperties = new NetworkProperties(discardModifiedNetworkInterfaces(new HashMap<>(properties)));
            update(this.networkProperties.getProperties());
        }
    }

    public void deactivate(ComponentContext componentContext) {
        logger.debug("Deactivate NetworkConfigurationService...");
        this.dhcpServerMonitor.stop();
        this.dhcpServerMonitor.clear();
    }

    public synchronized void update(Map<String, Object> receivedProperties) {
        logger.debug("Update NetworkConfigurationService...");
        if (receivedProperties == null) {
            logger.debug("Received null properties...");
            return;
        }

        this.dhcpServerMonitor.stop();
        this.dhcpServerMonitor.clear();

        final Map<String, Object> modifiedProps = migrateModemConfigs(receivedProperties);
        final Set<String> interfaces = NetworkConfigurationServiceCommon
                .getNetworkInterfaceNamesInConfig(modifiedProps);

        try {
            for (final String interfaceName : interfaces) {
                Optional<NetInterfaceType> interfaceTypeProperty = NetworkConfigurationServiceCommon
                        .getNetworkTypeFromProperties(interfaceName, modifiedProps);
                if (!interfaceTypeProperty.isPresent()) {
                    interfaceTypeProperty = Optional.of(getNetworkTypeFromSystem(interfaceName));
                    setInterfaceType(modifiedProps, interfaceName, interfaceTypeProperty.get());
                }
                if (NetInterfaceType.MODEM.equals(interfaceTypeProperty.get())) {
                    setModemPppNumber(modifiedProps, interfaceName);
                }
            }

            mergeNetworkConfigurationProperties(modifiedProps, this.networkProperties.getProperties());

            decryptAndConvertPasswordProperties(modifiedProps);
            this.networkProperties = new NetworkProperties(discardModifiedNetworkInterfaces(modifiedProps));

            writeNetworkConfigurationSettings(modifiedProps);
            writeDhcpServerConfiguration(interfaces);
            this.dhcpServerMonitor.start();

            this.eventAdmin.postEvent(new NetworkConfigurationChangeEvent(modifiedProps));
        } catch (KuraException e) {
            logger.error("Failed to apply network configuration", e);
        }
    }

    protected NetInterfaceType getNetworkTypeFromSystem(String interfaceName) throws KuraException {
        // Do be done with NM...
        if (isUsbPort(interfaceName)) {
            return this.linuxNetworkUtil.getType(this.networkService.getModemPppInterfaceName(interfaceName));
        } else {
            return this.linuxNetworkUtil.getType(interfaceName);
        }
    }

    private boolean isUsbPort(String interfaceName) {
        return interfaceName.split("\\.")[0].matches(MODEM_PORT_REGEX);
    }

    protected void setModemPppNumber(Map<String, Object> modifiedProps, String interfaceName) {
        Integer pppNum = Integer.valueOf(this.networkService.getModemPppInterfaceName(interfaceName).substring(3));
        modifiedProps.put(String.format(PREFIX + "%s.config.pppNum", interfaceName), pppNum);
    }

    protected void setInterfaceType(Map<String, Object> modifiedProps, String interfaceName, NetInterfaceType type) {
        modifiedProps.put(String.format(PREFIX + "%s.type", interfaceName), type.toString());
    }

    private boolean isEncrypted(String password) {
        try {
            this.cryptoService.decryptAes(password.toCharArray());
            return true;
        } catch (Exception unableToDecryptAes) {
            return false;
        }
    }

    private Password decryptPassword(String password) throws KuraException {
        String decryptedPassword = "";
        if (!password.isEmpty()) {
            if (isEncrypted(password)) {
                decryptedPassword = new String(this.cryptoService.decryptAes(password.toCharArray()));
            } else {
                decryptedPassword = password;
            }
        }
        return new Password(decryptedPassword);
    }

    private void decryptAndConvertPasswordProperties(Map<String, Object> modifiedProps) throws KuraException {
        for (Entry<String, Object> prop : modifiedProps.entrySet()) {
            if (prop.getKey().contains("passphrase") || prop.getKey().contains("password")) {

                Object value = prop.getValue();

                if (value instanceof Password) {
                    modifiedProps.put(prop.getKey(), decryptPassword(((Password) value).toString()));
                } else if (value instanceof String) {
                    modifiedProps.put(prop.getKey(), decryptPassword(value.toString()));
                } else {
                    modifiedProps.put(prop.getKey(), value);
                }
            }
        }
    }

    @Override
    public synchronized ComponentConfiguration getConfiguration() throws KuraException {

        return new ComponentConfigurationImpl(
                NetworkConfigurationServiceCommon.PID, NetworkConfigurationServiceCommon
                        .getDefinition(this.networkProperties.getProperties(), Optional.empty()),
                this.networkProperties.getProperties());
    }

    private void mergeNetworkConfigurationProperties(final Map<String, Object> source, final Map<String, Object> dest) {
        final Set<String> interfaces = NetworkConfigurationServiceCommon.getNetworkInterfaceNamesInConfig(source);
        interfaces.addAll(NetworkConfigurationServiceCommon.getNetworkInterfaceNamesInConfig(dest));

        dest.putAll(source);
        dest.put(NET_INTERFACES, interfaces.stream().collect(Collectors.joining(",")));
    }

    private String probeNetInterfaceConfigName(NetInterface<? extends NetInterfaceAddress> netInterface) {
        final Set<String> interfaceNamesInConfig = NetworkConfigurationServiceCommon
                .getNetworkInterfaceNamesInConfig(this.networkProperties.getProperties());

        final Optional<String> usbPort = Optional.ofNullable(netInterface.getUsbDevice()).map(UsbDevice::getUsbPort);

        if (usbPort.isPresent() && interfaceNamesInConfig.contains(usbPort.get())) {
            return usbPort.get();
        } else if (interfaceNamesInConfig.contains(netInterface.getName())) {
            return netInterface.getName();
        }
        return usbPort.orElse(netInterface.getName());
    }

    private void writeNetworkConfigurationSettings(Map<String, Object> networkProperties) {
        if (Objects.isNull(this.nmDbusConnector)) {
            logger.error("Found null NMDbusConnector. Couldn't apply network configuration settings.");
            return;
        }

        try {
            this.nmDbusConnector.apply(networkProperties);
        } catch (DBusExecutionException | DBusException e) {
            logger.error("Couldn't apply network configuration settings due to: ", e);
        }
    }

    private void writeDhcpServerConfiguration(Set<String> interfaceNames) {
        interfaceNames.forEach(interfaceName -> {
            if (isDhcpServerValid(interfaceName)) {
                DhcpServerConfigWriter dhcpServerConfigWriter = new DhcpServerConfigWriter(interfaceName,
                        this.networkProperties);
                try {
                    dhcpServerConfigWriter.writeConfiguration();
                    this.dhcpServerMonitor.putDhcpServerInterfaceConfiguration(interfaceName, true);
                } catch (UnknownHostException | KuraException e) {
                    logger.error("Failed to write DHCP Server configuration", e);
                    this.dhcpServerMonitor.putDhcpServerInterfaceConfiguration(interfaceName, false);
                }
            } else {
                this.dhcpServerMonitor.putDhcpServerInterfaceConfiguration(interfaceName, false);
            }
        });
    }

    private boolean isDhcpServerValid(String interfaceName) {
        boolean isValid = false;
        Optional<NetInterfaceType> type = NetworkConfigurationServiceCommon.getNetworkTypeFromProperties(interfaceName,
                this.networkProperties.getProperties());
        Optional<Boolean> isDhcpServerEnabled = this.networkProperties.getOpt(Boolean.class,
                "net.interface.%s.config.dhcpServer4.enabled", interfaceName);
        Optional<NetInterfaceStatus> status = getNetInterfaceStatus(interfaceName);

        if (type.isPresent()
                && (NetInterfaceType.ETHERNET.equals(type.get()) || NetInterfaceType.WIFI.equals(type.get()))
                && (isDhcpServerEnabled.isPresent() && isDhcpServerEnabled.get() && status.isPresent())
                && !status.get().equals(NetInterfaceStatus.netIPv4StatusL2Only)) {
            isValid = true;
        }
        return isValid;
    }

    private Optional<NetInterfaceStatus> getNetInterfaceStatus(String interfaceName) {
        Optional<String> interfaceStatus = this.networkProperties.getOpt(String.class,
                "net.interface.%s.config.ip4.status", interfaceName);
        if (interfaceStatus.isPresent()) {
            return Optional.of(NetInterfaceStatus.valueOf(interfaceStatus.get()));
        } else {
            return Optional.empty();
        }
    }

    private Map<String, Object> migrateModemConfigs(final Map<String, Object> properties) {

        Map<String, Object> result = new HashMap<>(properties);
        final Set<String> interfaceNames = NetworkConfigurationServiceCommon
                .getNetworkInterfaceNamesInConfig(properties);
        final Set<String> resultInterfaceNames = new HashSet<>();

        for (final String existingInterfaceName : interfaceNames) {
            if (!PPP_INTERFACE.matcher(existingInterfaceName).matches()) {
                resultInterfaceNames.add(existingInterfaceName);
                continue;
            }

            logger.info("migrating configuration for interface: {}...", existingInterfaceName);
            final String migratedInterfaceName = this.networkService.getModemUsbPort(existingInterfaceName);

            logger.info("renaming {} to {}", existingInterfaceName, migratedInterfaceName);
            result = replaceModemPropertyKeys(migratedInterfaceName, existingInterfaceName, result);
            resultInterfaceNames.add(migratedInterfaceName);

            logger.info("migrating configuration for interface: {}...done", existingInterfaceName);

        }

        result.put(NET_INTERFACES, resultInterfaceNames.stream().collect(Collectors.joining(",")));
        return result;
    }

    private Map<String, Object> replaceModemPropertyKeys(String migratedInterfaceName, String existingInterfaceName,
            Map<String, Object> properties) {
        Map<String, Object> result = new HashMap<>();
        final String migratedPrefix = PREFIX + migratedInterfaceName + ".";
        final String existingPrefix = PREFIX + existingInterfaceName + ".";

        for (final Entry<String, Object> e : properties.entrySet()) {
            final String key = e.getKey();

            if (key.startsWith(existingPrefix)) {
                final String suffix = key.substring(existingPrefix.length());
                final String migratedPropertyKey = migratedPrefix + suffix;
                final Object existingProperty = properties.get(migratedPropertyKey);

                if (existingProperty == null) {
                    result.put(migratedPropertyKey, e.getValue());
                }
            } else {
                result.put(key, e.getValue());
            }
        }
        return result;
    }

    private static Map<String, Object> discardModifiedNetworkInterfaces(final Map<String, Object> properties) {
        if (!properties.containsKey(MODIFIED_INTERFACE_NAMES)) {
            return properties;
        }

        final Map<String, Object> result = new HashMap<>(properties);
        result.remove(MODIFIED_INTERFACE_NAMES);
        return result;
    }
}