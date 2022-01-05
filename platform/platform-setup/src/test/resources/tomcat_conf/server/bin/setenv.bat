@echo on

rem Set some JVM system properties required by Bonita

set PLATFORM_SETUP="-Dorg.bonitasoft.platform.setup.folder=%CATALINA_HOME%\..\setup"
set H2_DATABASE_DIR="-Dorg.bonitasoft.h2.database.dir=%CATALINA_HOME%\..\h2_database"
set INCIDENT_LOG_DIR="-Dorg.bonitasoft.engine.incident.folder=%CATALINA_HOME%\logs"

rem Define the RDMBS vendor use by Bonita Engine to store data. Valid values are: h2, postgres, sqlserver, oracle, mysql.
set DB_OPTS="-Dsysprop.bonita.db.vendor=h2"

rem Define the RDMBS vendor use by Bonita Engine to store Business Data. Valid values are: h2, postgres, sqlserver, oracle, mysql.
rem If you use different DB engines by tenants, please update directly bonita-tenant-community-custom.properties
set BDM_DB_OPTS="-Dsysprop.bonita.bdm.db.vendor=h2"

rem Arjuna (JTA service added to Tomcat and required by Bonita Engine for transaction management)
set ARJUNA_OPTS="-Dcom.arjuna.ats.arjuna.common.propertiesFile=%CATALINA_HOME%\conf\jbossts-properties.xml"

rem Optional JAAS configuration. Usually used when delegating authentication to LDAP / Active Directory server
rem set SECURITY_OPTS="-Djava.security.auth.login.config=%CATALINA_HOME%\conf\jaas-standard.cfg"

rem Optional JMX remote access Configuration. Used to enable remote JMX agent in tomcat to monitor Heap Memory, Threads, CPU Usage, Classes, and configure various MBeans.
if "%JMX_REMOTE_ACCESS%" == "true" (set JMX_REMOTE_ACCESS_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9000 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=true -Dcom.sun.management.jmxremote.password.file=%CATALINA_HOME%\conf\jmxremote.password -Dcom.sun.management.jmxremote.access.file=%CATALINA_HOME%\conf\jmxremote.access")

rem Pass the JVM system properties to Tomcat JVM using CATALINA_OPTS variable
set CATALINA_OPTS=%CATALINA_OPTS% %LOG_CONF_FILE_PATH% %PLATFORM_SETUP% %H2_DATABASE_DIR% %DB_OPTS% %BDM_DB_OPTS% %ARJUNA_OPTS% %INCIDENT_LOG_DIR% %JMX_REMOTE_ACCESS_OPTS% -Dfile.encoding=UTF-8 -Xshare:auto -Xms1024m -Xmx1024m -XX:+HeapDumpOnOutOfMemoryError -Dlog4j2.formatMsgNoLookups=true

set CATALINA_PID=%CATALINA_BASE%\catalina.pid

@rem extra lib at Tomcat startup (for instance bonita juli extensions)
set CLASSPATH="%CATALINA_HOME%\lib\ext\*"
