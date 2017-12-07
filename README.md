# uetscholarship-server

## Build
```
mvn clean package
```
## Install on Linux
*login as root and add required environment variables*
```
#ln -s /etc/init.d/uetserver .../uetscholarship-server/target/uetscholarship-server-1.0-SNAPSHOT.war
#ln -s /etc/init.d/uetmesseger .../uetscholarship-messenger/target/uetscholarship-messenger-1.0-SNAPSHOT.war
#/etc/init.d/uetmesseger start
#/etc/init.d/uetserver start
```
