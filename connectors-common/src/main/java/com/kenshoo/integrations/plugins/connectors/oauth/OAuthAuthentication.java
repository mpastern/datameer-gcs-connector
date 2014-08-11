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

import java.util.List;

import datameer.com.google.common.collect.Lists;
import datameer.dap.sdk.datastore.DataStoreType;
import datameer.dap.sdk.property.PropertyDefinition;
import datameer.dap.sdk.property.PropertyType;
import datameer.dap.sdk.property.WizardPageContext;

public abstract class OAuthAuthentication extends AuthenticationMethod {
    public static final String PROPERTY_KEY_SERVICE_OAUTH_TOKEN_INFO = "service.oauth.token-info";

    public OAuthAuthentication(String name) {
        super(name);
    }

    @Override
    public final List<PropertyDefinition> createPropertyDefinitions(DataStoreType dataStoreType,
            WizardPageContext pageContext) {
        List<PropertyDefinition> propertiesList = Lists.newArrayList();
        OAuthSetup oAuthSetup = getOAuthSetup(dataStoreType);

        OAuthButton oAuthButton = new OAuthButton(dataStoreType.getName(), oAuthSetup, pageContext.getDatameerServer());
        propertiesList.add(oAuthButton);

        PropertyDefinition tokenInfo =
                new PropertyDefinition(PROPERTY_KEY_SERVICE_OAUTH_TOKEN_INFO,
                        "Token Information",
                        PropertyType.INFORMATION,
                        "No Token");
        tokenInfo.alwaysShowInAppInstallWizard();
        propertiesList.add(tokenInfo);
        return propertiesList;
    }

    protected abstract OAuthSetup getOAuthSetup(DataStoreType dataStoreType);
}
