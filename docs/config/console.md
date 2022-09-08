# Gateway Administration Console

## Accessing the Kura Gateway Administration Console
Kura provides a web-based, user interface for the administration and management of your IoT gateway. The Kura Gateway Administration Console enables you to monitor the gateway status, manage the network configuration, and manage the installed application and services. Access to the Kura Gateway Administration Console requires that a unit running Eclipse Kura is reachable via its Ethernet or Wi-Fi network interfaces (i.e., eth0, eth1, or wlan0). The default (out-of-the-box) network configuration for the supported platforms is as follows:

- Raspberry Pi
    - **eth0**
      - Status: Enabled for WAN
      - Configure: DHCP (DHCP client)
    - **wlan0**
      - Status: Enabled for LAN
      - Configure: Manually (Static IP)
      - IP Address: 172.16.1.1
      - Subnet Mask: 255.255.255.0
      - Wireless Mode: Access Point
      - SSID: kura_gateway_&lt;eth0_MAC_Address>
      - Wireless Security: WPA/WPA2
      - Passphrase: testKEYS  

Connections on HTTP port 443 for these interfaces are allowed by default through the built-in firewall. The Kura Gateway Administration Console can be accessed by typing the IP address of the gateway into the browser's URL bar. Once the URL is submitted, the user is required to log in and is then redirected to the Administration Console (e.g., `https://192.168.2.8/admin/console`) shown in the screen capture below. The default login name and password is `admin/admin`.

{% include alerts.html message='It is recommended to change the default password after initial setup and before deployment, as well as limiting access to the Administration Console to a trusted local network interface using appropriate firewall rules.' %}

![Gateway Admin Console](./images/GatewayAdminConsole.png)

## Password change

Once logged in, the user can modify its password (recommended after the first login). To access the option, click on the button near the username in the header section. A dropdown menu appears with the logout and the password modification options. When clicking on "Change password", the following dialog will appear:

![Password Change Dialog](./images/PasswordChangeDialog.png)

After confirming the changes, the user will be logged out.