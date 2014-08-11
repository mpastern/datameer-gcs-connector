datameer-gcs-connector
======================

1. compile the project

2. copy gcs-1.0.zip to etc/custom-plugins

3. copy gcs-1.0-fat.jar to etc/custom-jars

4. copy kenshoo_servlet_connectors-common-1.0.jar to webapp/ROOT/WEB-INF/lib

5. inset the following deployment descriptor into webapp/ROOT/WEB-INF/web.xml:

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

6. insert the following into conf/default.properties
   (this section assumes that you have google app to be used in it)

	oauth.data.google.clientid=

	oauth.data.google.secret=

	oauth.data.google.redirecturi=http://integration.kenshoo.com:8080/oauth/forward

7. restart Datameer
