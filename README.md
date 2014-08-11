datameer-gcs-connector
======================

0. compile the project

1. copy gcs-1.0.zip to etc/custom-plugins

2. copy gcs-1.0-fat.jar to etc/custom-jars

3. kenshoo_servlet_connectors-common-1.0.jar to webapp/ROOT/WEB-INF/lib

4. inset the following deployment descriptor into webapp/ROOT/WEB-INF/web.xml:

	<!-- KENSHOO oAuth forward -->
	<servlet>
                <servlet-name>oauthforward</servlet-name>
                <servlet-class>com.kenshoo.integrations.plugins.connectors.oauth.servlet.OauthForwardServlet</servlet-class>
	</servlet>
        <servlet-mapping>
                <servlet-name>oauthforward</servlet-name>
                <url-pattern>/oauth/forward</url-pattern>
        </servlet-mapping>
	<!-- KENSHOO oAuth forward -->

5. insert the following into conf/default.properties (this section assumes that you have google app to be used in it)

# KENSHOO OAuth data
oauth.data.google.clientid=...
oauth.data.google.secret=...
oauth.data.google.redirecturi=http://integration.kenshoo.com:8080/oauth/forward

6. restart Datameer
