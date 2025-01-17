/*******************************************************************************
 * Copyright (c) 2011, 2023 Eurotech and/or its affiliates and others
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *  Eurotech
 *  Areti
 *******************************************************************************/
package org.eclipse.kura.web.shared.model;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GwtNetInterfaceConfig extends KuraBaseModel implements Serializable {

    private static final String ROUTER_DNS_PASS = "routerDnsPass";
    private static final Logger logger = Logger.getLogger(GwtNetInterfaceConfig.class.getSimpleName());
    private static final long serialVersionUID = 7079533925979145804L;

    private Gwt8021xConfig enterpriseConfig = new Gwt8021xConfig();

    public void setEnterpriseConfig(Gwt8021xConfig enterpriseConfig) {
        this.enterpriseConfig = enterpriseConfig;
    }

    public Gwt8021xConfig get8021xConfig() {
        return this.enterpriseConfig;
    }

    public Map<String, Object> getEnterpriseConfigProps() {
        return this.enterpriseConfig.getProperties();
    }

    public String getName() {
        return get("name");
    }

    public void setName(String name) {
        set("name", name);
    }

    public String getInterfaceName() {
        return get("interfaceName");
    }

    public void setInterfaceName(String interfaceName) {
        set("interfaceName", interfaceName);
    }

    public GwtNetIfStatus getStatusEnum() {
        return GwtNetIfStatus.valueOf(getStatus());
    }

    public String getStatus() {
        return get("status");
    }

    public void setStatus(String status) {
        set("status", status);
    }

    public Integer getWanPriority() {
        return get("priority");
    }

    public void setWanPriority(Integer priority) {
        set("priority", priority);
    }

    public GwtNetIfConfigMode getConfigModeEnum() {
        return GwtNetIfConfigMode.valueOf(getConfigMode());
    }

    public String getConfigMode() {
        return get("configMode");
    }

    public void setConfigMode(String configMode) {
        set("configMode", configMode);
    }

    public String getIpAddress() {
        return get("ipAddress");
    }

    public void setIpAddress(String ipAddress) {
        set("ipAddress", ipAddress);
    }

    public String getSubnetMask() {
        return get("subnetMask");
    }

    public void setSubnetMask(String subnetMask) {
        set("subnetMask", subnetMask);
    }

    public String getGateway() {
        return get("gateway");
    }

    public void setGateway(String gateway) {
        set("gateway", gateway);
    }

    public String getDnsServers() {
        return get("dnsServers");
    }

    public void setDnsServers(String dnsServers) {
        set("dnsServers", dnsServers);
    }

    public String getReadOnlyDnsServers() {
        return get("dnsReadOnlyServers");
    }

    public void setReadOnlyDnsServers(String dnsServers) {
        set("dnsReadOnlyServers", dnsServers);
    }
    
    public Integer getMtu() {
        return get("mtu");
    }

    public void setMtu(Integer mtu) {
        set("mtu", mtu);
    }

    public void setHwState(String hwState) {
        set("hwState", hwState);
    }

    public String getHwState() {
        return get("hwState");
    }

    public void setHwName(String hwName) {
        set("hwName", hwName);
    }

    public String getHwName() {
        return get("hwName");
    }

    public GwtNetIfType getHwTypeEnum() {
        GwtNetIfType typeEnum = GwtNetIfType.UNKNOWN;

        try {
            typeEnum = GwtNetIfType.valueOf(getHwType());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error getting HwType.", e);
        }

        return typeEnum;
    }

    public void setHwType(String hwType) {
        set("hwType", hwType);
    }

    public String getHwType() {
        return get("hwType");
    }

    public void setHwAddress(String hwAddress) {
        set("hwAddress", hwAddress);
    }

    public String getHwAddress() {
        return get("hwAddress");
    }

    public void setHwSerial(String hwSerial) {
        set("hwSerial", hwSerial);
    }

    public String getHwSerial() {
        return get("hwSerial");
    }

    public void setHwDriver(String hwDriver) {
        set("hwDriver", hwDriver);
    }

    public String getHwDriver() {
        return get("hwDriver");
    }

    public void setHwDriverVersion(String hwDriverVersion) {
        set("hwDriverVersion", hwDriverVersion);
    }

    public String getHwDriverVersion() {
        return get("hwDriverVersion");
    }

    public void setHwFirmware(String hwFirmware) {
        set("hwFirmware", hwFirmware);
    }

    public String getHwFirmware() {
        return get("hwFirmware");
    }

    public void setHwMTU(int mtu) {
        set("hwMTU", mtu);
    }

    public int getHwMTU() {
        if (get("hwMTU") != null) {
            return (Integer) get("hwMTU");
        } else {
            return 0;
        }
    }

    public void setHwUsbDevice(String hwUsbDevice) {
        set("hwUsbDevice", hwUsbDevice);
    }

    public String getHwUsbDevice() {
        return get("hwUsbDevice");
    }

    public String getHwRssi() {
        return get("hwRssi");
    }

    public void setHwRssi(String rssi) {
        set("hwRssi", rssi);
    }

    public GwtNetRouterMode getRouterModeEnum() {
        return GwtNetRouterMode.valueOf(getRouterMode());
    }

    public String getRouterMode() {
        return get("routerMode");
    }

    public void setRouterMode(String routerMode) {
        set("routerMode", routerMode);
    }

    public String getRouterDhcpBeginAddress() {
        return get("routerDhcpBeginAddress");
    }

    public void setRouterDhcpBeginAddress(String routerDhcpBeginAddress) {
        set("routerDhcpBeginAddress", routerDhcpBeginAddress);
    }

    public String getRouterDhcpEndAddress() {
        return get("routerDhcpEndAddress");
    }

    public void setRouterDhcpEndAddress(String routerDhcpEndAddress) {
        set("routerDhcpEndAddress", routerDhcpEndAddress);
    }

    public int getRouterDhcpDefaultLease() {
        if (get("routerDhcpDefaultLease") != null) {
            return (Integer) get("routerDhcpDefaultLease");
        }
        return 0;
    }

    public void setRouterDhcpDefaultLease(int routerDhcpDefaultLease) {
        set("routerDhcpDefaultLease", routerDhcpDefaultLease);
    }

    public int getRouterDhcpMaxLease() {
        if (get("routerDhcpMaxLease") != null) {
            return (Integer) get("routerDhcpMaxLease");
        }
        return 0;
    }

    public void setRouterDhcpMaxLease(int routerDhcpMaxLease) {
        set("routerDhcpMaxLease", routerDhcpMaxLease);
    }

    public String getRouterDhcpSubnetMask() {
        return (String) get("routerDhcpSubnetMask");
    }

    public void setRouterDhcpSubnetMask(String routerDhcpSubnetMask) {
        set("routerDhcpSubnetMask", routerDhcpSubnetMask);
    }

    public boolean getRouterDnsPass() {
        if (get(ROUTER_DNS_PASS) != null) {
            return (Boolean) get(ROUTER_DNS_PASS);
        }
        return false;
    }

    public void setRouterDnsPass(boolean routerDnsPass) {
        set(ROUTER_DNS_PASS, routerDnsPass);
    }

    public String getIpv6Status() {
        return get("ipv6.status");
    }

    public void setIpv6Status(String status) {
        set("ipv6.status", status);
    }

    public Integer getIpv6WanPriority() {
        return get("ipv6.priority");
    }

    public void setIpv6WanPriority(Integer priority) {
        set("ipv6.priority", priority);
    }

    public String getIpv6ConfigMode() {
        return get("ipv6.configmode");
    }

    public void setIpv6ConfigMode(String configMode) {
        set("ipv6.configmode", configMode);
    }

    public String getIpv6AutoconfigurationMode() {
        return get("ipv6.autoconfiguration");
    }

    public void setIpv6AutoconfigurationMode(String autoconfigMode) {
        set("ipv6.autoconfiguration", autoconfigMode);
    }

    public String getIpv6Address() {
        return get("ipv6.address");
    }

    public void setIpv6Address(String ipAddress) {
        set("ipv6.address", ipAddress);
    }

    public Integer getIpv6SubnetMask() {
        return get("ipv6.subnet");
    }

    public void setIpv6SubnetMask(Integer subnetMask) {
        set("ipv6.subnet", subnetMask);
    }

    public String getIpv6Gateway() {
        return get("ipv6.gateway");
    }

    public void setIpv6Gateway(String gateway) {
        set("ipv6.gateway", gateway);
    }

    public String getIpv6DnsServers() {
        return get("ipv6.dns");
    }

    public void setIpv6DnsServers(String dnsServers) {
        set("ipv6.dns", dnsServers);
    }

    public String getIpv6Privacy() {
        return get("ipv6.privacy");
    }

    public void setIpv6Privacy(String privacy) {
        set("ipv6.privacy", privacy);
    }

    public String getIpv6ReadOnlyDnsServers() {
        return get("ipv6.dnsReadOnly");
    }

    public void setIpv6ReadOnlyDnsServers(String dnsServers) {
        set("ipv6.dnsReadOnly", dnsServers);
    }

    public Integer getIpv6Mtu() {
        return get("ipv6.mtu");
    }

    public void setIpv6Mtu(Integer mtu) {
    	set("ipv6.mtu", mtu);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GwtNetInterfaceConfig)) {
            return false;
        }

        Map<String, Object> properties = getProperties();
        Map<String, Object> otherProps = ((GwtNetInterfaceConfig) o).getProperties();

        if (properties != null) {
            if (otherProps == null) {
                return false;
            }
            if (properties.size() != otherProps.size()) {
                logger.log(Level.FINER, "Sizes differ");
                return false;
            }

            for (Entry<String, Object> entry : properties.entrySet()) {
                final Object oldVal = entry.getValue();
                final Object newVal = otherProps.get(entry.getKey());
                if (oldVal != null) {
                    if (!oldVal.equals(newVal)) {
                        logger.log(Level.FINER, () -> "Values differ - Key: " + entry.getKey() + " oldVal: " + oldVal
                                + ", newVal: " + newVal);
                        return false;
                    }
                } else if (newVal != null) {
                    return false;
                }
            }
        } else if (otherProps != null) {
            return false;
        }

        return true;
    }
}
