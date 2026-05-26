# Nestwave Device to Cloud Bridge Documentation

This repository contains source code files for Nestwave Device to Cloud Bridge (D2CBridge).

![image](https://user-images.githubusercontent.com/84769396/172661074-39a031bb-c92b-4dab-8d7c-f741423173ed.png)

Customer protocol is COAP or HTTP in the example code provided. 

## Directory structure

Nestwave DCB source code should be located in a folder named `device`.
Sensitive data are located in `security` folder.

```txt
+-----------+
|${NSW_ROOT}|
+-----------+
    |
    |    +-----+
    +----|cloud|
    |    +-----+
    |       |
    |       |    +------+
    |       +----|device| (DCB root)
    |       |    +------+
    |       |        |    +--------+
    |       |        +----|security|
    |       |        |    +--------+
    |       |        |         |
    |       |        |         +----:README.md
    |       |        |         |
    |       |        |         +----:secret.jwt
    |       |        |         |
    |       |        |         +----:sensitive-config.ini
    |       |        |
    |       |        |    +---+
    |       |        +----|src|
    |       |        |    +---+
    |       |        |      |
    |       |        |      |    +----+
    |       |        |      +----|main|
    |       |        |           +----+
    |       |        |
    |       |        +----:Makefile
    |       |
    |       |    +--------+
    |       +----|security| (TLS certificates location)
    |       |    +--------+
    |       |        |
    |       |        +----:keystore.p12
    |       |
    |       +----:docker-compose-bridge.yml
    |
    |    +-----+
    +----|tools| (DCB dependency)
         +-----+
```

> [!caution]
> Files in the `security` folder other than `README.md` shall never be committed to the GIT repository.

## System requirements

In order to work with Nestwave Device to Cloud Bridge (DCB), a Debian 11 version is recommended.
Other versions may work but are not supported.

The following required packages should be installed.

```shell
apt install git make openjdk-11-jre maven python3-configargparse docker-compose
```

The user should be added to the `docker` group:

```shell
adduser $USER docker
newgrp docker
```

## Fetching sources

One needs to execute a few steps to get a working local copy.

### Create destination directory

We will install the D2CB source in `~/Nestwave/dev/D2CBridge` but one can use any location.

```shell
mkdir -p ~/Nestwave/dev/D2CBridge/cloud
```

### Clone tools

The DC2B code requires and extra repository for tools.

This repository is provided by Nestwave as a compressed tarball,
available in [Releases](https://github.com/nestwave/D2CBridge/releases).

Execute the following commands to download and uncompress the repository.

```shell
TOOLS_VERSION=v1.9-16
wget -O ~/Downloads/tools-${TOOLS_VERSION}.tar.xz https://github.com/nestwave/D2CBridge/releases/download/bridge-${TOOLS_VERSION}/tools-${TOOLS_VERSION}.tar.xz
tar -C ~/Nestwave/dev/D2CBridge -vaxf ~/Downloads/tools-${TOOLS_VERSION}.tar.xz
```

#### Clone D2CBridge sources

Just execute the following command, and you are ready to build your device to Nestwave cloud bridge.

```shell
git -C ~/Nestwave/dev/D2CBridge/cloud clone https://github.com/Samea-Innovation/D2CBridge.git device
```

## Compilation

### Sensitive Config 

You will need to create a file `device/security/sensitive-config.ini` with required information.
Please read [security/README.md](security/README.md) for more information about how to handle sensitive data.

The file `security/secret.jwt` is created automatically during the build process.

### TLS

TLS certificate will need to be exported into p12 format and stored in a file named `keystore.p12` located in the `cloud` folder.

To create a certificate manually:

```shell
cd ~/Nestwave/dev/D2CBridge/cloud/device/security
openssl req -new -newkey rsa:4096 -x509 -sha256 -days 365 -nodes -out certificate.crt -keyout private.key
openssl pkcs12 -export -out keystore.p12 -inkey private.key -in certificate.crt -passout pass:<password>
cp keystore.p12 ~/Nestwave/dev/D2CBridge/cloud/security
```

If using Let's Encrypt, it can be generated using the following script:

```shell
#! /usr/bin/env sh

set -xe

cd `dirname $0`

HOSTNAME=${1:-`hostname -f`}
SERVICES="authentication navigation"

# Certificate remote path below is Nestwave one.
# Customers should change it to their specific path.
# If using LetsEncrypt, just replace nw.do by your domain name.
CERT_REMOTE_PATH=/etc/letsencrypt/live/nw.do
CERT_LOCAL_PATH=${PWD}/security

# Get keystore pasword from sensitive-config.ini.
KEY_STORE_PASSWORD=`sed -ne 's/KEY_STORE_PASSWORD=\(.*\)$/\1/p' security/sensitive-config.ini`

test -d ${CERT_LOCAL_PATH} || mkdir ${CERT_LOCAL_PATH}
chmod 700 ${CERT_LOCAL_PATH}

scp root@${HOSTNAME}:${CERT_REMOTE_PATH}/*.pem ${CERT_LOCAL_PATH}

# Run openssl to create the key store.
# You may get the error: "sh: 2: KEY_STORE_PASSWORD: parameter not set or null"
# This means that sed expression above failed to get KEY_STORE_PASSWORD value.
# Then, hard code it above by copying it from security/sensitive-config.ini.
openssl pkcs12 -export \
	-in ${CERT_LOCAL_PATH}/fullchain.pem \
	-inkey ${CERT_LOCAL_PATH}/privkey.pem \
	-out ${CERT_LOCAL_PATH}/keystore.p12 -name tomcat -CAfile chain.pem \
	-caname root -passout "pass:${KEY_STORE_PASSWORD:?}"
```

### Build

For compilation, execute:

```shell
cd ~/Nestwave/dev/D2CBridge/cloud/device
make clean all
```

If no NestCloud user login and password were set in `security/sensitive-config.ini`,
then the process will prompt for your Nestwave username and password.
If you don't have one, please create your account at <https://www.nw.do/register>.

In order to make the process fully automatic one can use:

```shell
make NSW_USERNAME='username@company.com' NSW_PASSWORD='password'
```

## Execution

### Docker compose file

You need to create a file `docker-compose.yaml` located in the `cloud` folder, containing:

```yaml
services:
  tracking-db:  # The tracking database is optional if you have an external instance
    container_name: tracking-db
    image: postgres:11
    restart: always
    networks:
      - backend
    environment:
      - POSTGRES_DB=tracking
      - POSTGRES_USER=<username>
      - POSTGRES_PASSWORD=<password>
    volumes:
      - tracking-db-data:/var/lib/postgresql/data:rw

  d2c-bridge:
    container_name: d2c-bridge
    image: device
    build: device
    ports:
      - "8087:8087"      # HTTP
      - "8088:8088"      # HTTPS
      - "5683:5683/udp"  # CoAP
      - "5684:5684"      # CoAPS
    networks:
      - backend
    depends_on:
      - tracking-db
    dns_search:
      - nw.do

networks:
  backend:

volumes:
  tracking-db-data:
```

Then you need to run the following command:

```shell
cd ~/Nestwave/dev/D2CBridge/cloud/device
docker compose -f docker-compose.yaml up -d --build
```

## Technical documentation

Before you start working on D2CB, please ensure you have read:

- [SAMEA D2C Bridge Specification V1.06](doc/SAMEA%20D2C%20Bridge%20Specification%20v1.06.pdf) for API version v1.8 and later.
- [NestCloud Bridge Specification V1.05](doc/NestCloud%20Bridge%20Specification%20v1.05.pdf) for API version v1.8 and later.
- [NestCloud Bridge Specification V1.04](doc/NestCloud%20Bridge%20Specification%20v1.04.pdf) for API version v1.7 and later. (This is legacy and shall not be used for new projects).
- [NestCloud Bridge Specification V1.03](doc/NestCloud%20Bridge%20Specification%20v1.03.pdf) for API version v1.6 and prior. (This is legacy and shall not be used for new projects).

### Positions tracking database

The D2CBridge allows storing device positions in a Database in order to provide them to a web frontend (an Apache based example is provided [here](src/main/html)).

In case the Database is located on a separate server the code should be reworked to use asynchronous operations.

#### Tables layout

The positions tracking Database is named `positions` and holds a set of columns that are defined [here](src/main/java/com/nestwave/device/repository/position/PositionRecord.java).

The `key.id` corresponds to device unique identifier that is referenced in this document by _device ID_.
The device ID allows identifying different devices, but needs a provisioning step upon production.

#### Database credentials

In order to connect to the tracking position Database, credentials should be provided via the variables `JDBC_USERNAME` and `JDBC_PASSWORD`.

Please read [security/README.md](security/README.md) for more information about how to handle sensitive data.

### Device ID

D2CBridge uses a data packet that holds a device unique identifier and a Fletcher32 integrity check word.

The device identifier is the first bytes of the received data packet:
- 64 bits for v1.7 and later versions,
  - bits[47 down to 0]: IMEI
  - bits[63 down to 48]: Custom device ID that can be set during manufacturing.
- 32 bits for v1.6 and prior versions. This is a legacy format and shall not be
  used anymore.

### Security Manager plugin

D2CB service uses a JSON Web Token to authenticate to Nestwave/Sequans Hybrid GNSS Cloud.
It updates it regularly using `/renew` API.
The newly retrieved JWT needs to be stored in a secure persistent storage.

As every company has its own security rules, we provide a plugin interface to handle these operations.
We provide also a plugin implementation example to show how this interface is used.

Customers __shall reimplement themselves__ in order to fit their own security standards.
It is highly recommended not to use file system operations, like it is used in the example plugin implementation, unless the customer __fully understands the security problems__ that can result from it and __takes the relevant actions to counter them__.

### Adding a plugin

Examples of plugins exists in [`src/main/java/com/basic_partner`](src/main/java/com/basic_partner) and [`src/main/java/com/traxmate`](src/main/java/com/traxmate).

In order to add a new one, just copy the folder to another name and modify the code as needed.

### Difficulties at Nestwave/Sequans account creation

The email/account validation screen is known to break and not letting you continue (yes we know it's dumb).

The `[next]` button will remain disabled (greyed out) and you will be stuck.
To continue you will need to enable the button by editing the HTML. 

1. Press `F12` to open the inspector.
2. Click the element selector in the top left corner of th inspector window (a mouse cursor hovering a rectangle).
3. Click the `[next]` button, it should have highlighted a section in the inspector.
4. Find `disabled="true"` and remove it from the element.
5. Now click on the `[next]` button (it will work even if it's greyed out).
