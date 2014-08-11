//
//   Copyright (c) 2014 Kenshoo.com
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//


package com.kenshoo.integrations.plugins.connectors.oauth;

import datameer.dap.sdk.common.GenericConfiguration;
import datameer.dap.sdk.property.CallbackRequest;
import datameer.dap.sdk.property.ExternalCallbackButtonPropertyDefinition;
import datameer.dap.sdk.util.DatameerServer;
import datameer.dap.sdk.util.StringUtil;

public class OAuthButton extends ExternalCallbackButtonPropertyDefinition {
    private static final String KEY = "requestOAuthAccessButton";
    private final String serviceName;
    private final OAuthSetup oAuthSetup;
    private final DatameerServer datameerServer;

    public OAuthButton(String serviceName, OAuthSetup oAuthSetup, DatameerServer datameerServer) {
        super(KEY, String.format("Authorize %s to retrieve data", new Object[] { "Kenshoo" }));
        this.serviceName = serviceName;
        this.oAuthSetup = oAuthSetup;
        this.datameerServer = datameerServer;
        setHelpText(String.format("Obtain OAuth access for %s. (An <a href=\"http://oauth.net/\">OAuth token</a> is used to allow a login to the service without revealing the user name and password to %s.)",
                new Object[] { serviceName, "Kenshoo" }));

        setSaveProperties(true);
        setPropertyPersister(new OAuthPropertyPersister(oAuthSetup));
        alwaysShowInAppInstallWizard();
    }

    public String getServiceName() {
        return serviceName;
    }

    @Override
    public String getExternalUrl(GenericConfiguration configuration) {
        return oAuthSetup.getAuthorizationUrl(configuration, this.datameerServer, this.serviceName);
    }

    @Override
    public String processCallback(GenericConfiguration configuration, CallbackRequest request) {
        return oAuthSetup.processCallback(configuration, this.datameerServer, request);
    }

    @Override
    public String validate(GenericConfiguration conf, int index, String value) {
        String code = conf.getStringProperty("authentication.oauth.accesstoken", null);
        if (StringUtil.isEmpty(code)) {
            return "No access granted! Please click " + getLabel();
        }
        if ((oAuthSetup.isTokenExpired(conf)) && (!oAuthSetup.isTokenRefreshSupported())) {
            return "Token Expired! Please click " + getLabel();
        }
        return super.validate(conf, index, value);
    }
}
