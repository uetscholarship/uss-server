# uss-server

## Build
```
mvn -Dmaven.test.skip=true clean package
```
## Install on Linux
*login as root and add required environment variables*
```
#ln -s /etc/init.d/uetnews-server .../uetnews-server/target/uetnews-server-1.0-SNAPSHOT.war
#ln -s /etc/init.d/uetgrade-server .../uetgrade-server/target/uetgrade-server-1.0-SNAPSHOT.war
#ln -s /etc/init.d/ussmesseger .../uss-messenger/target/uss-messenger-1.0-SNAPSHOT.war
#/etc/init.d/ussmesseger start
#/etc/init.d/uetnews-server start
#/etc/init.d/uetgrade-server start
```
