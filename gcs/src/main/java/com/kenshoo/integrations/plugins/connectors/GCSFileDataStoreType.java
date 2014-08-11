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


package com.kenshoo.integrations.plugins.connectors;

import java.util.List;

import com.google.api.services.storage.StorageScopes;
import com.kenshoo.integrations.plugins.connectors.oauth.AuthenticationMethod;
import com.kenshoo.integrations.plugins.connectors.oauth.OAuth2Authentication;
import com.kenshoo.integrations.plugins.connectors.oauth.OAuth2BasedDataStoreType;
import com.kenshoo.integrations.plugins.connectors.oauth.OAuth2Setup;
import com.kenshoo.integrations.plugins.connectors.oauth.OAuthClientDataRepository;
import com.kenshoo.integrations.plugins.connectors.oauth.OAuthSetup;
import com.kenshoo.integrations.plugins.connectors.util.OAuthUtil;

import datameer.dap.sdk.datastore.FileDataStoreType;
import datameer.dap.sdk.entity.DataStore;
import datameer.dap.sdk.exportjob.ExportJobType;
import datameer.dap.sdk.importjob.ImportJobType;
import datameer.dap.sdk.property.PropertyDefinition;
import datameer.dap.sdk.property.PropertyGroupDefinition;
import datameer.dap.sdk.property.WizardPageContext;
import datameer.dap.sdk.property.WizardPageDefinition;

public class GCSFileDataStoreType extends FileDataStoreType implements OAuth2BasedDataStoreType {

    public static final String ID = "das.GCSFileDataStoreType";

    private final String providerName = "Google";

    private final ImportJobType<?> _importJobType = new GCSFileImportJobType();

    public GCSFileDataStoreType() {
        super(new GCSFileProtocol(), ID);
    }

    @Override
    public ExportJobType getExportJobType() {
        return null;
    }

    @Override
    public OAuth2Setup getOAuthSetup() {
        OAuth2Setup oAuthSetup =
                OAuth2Setup.create(this,
                        OAuthUtil.GOOGLE_AUTH_ENDPOINT,
                        OAuthUtil.GOOGLE_TOKEN_ENDPOINT,
                        OAuthSetup.TokenRefresh.SUPPORTED);
        oAuthSetup.setOAuthClientData(OAuthClientDataRepository.GOOGLE_CLIENT_DATA);
        oAuthSetup.setScope(StorageScopes.DEVSTORAGE_READ_ONLY);
        return oAuthSetup;
    }

    @Override
    public String getProviderName() {
        return providerName;
    }

    public AuthenticationMethod getAuthenticationMethod() {
        return new OAuth2Authentication();
    }

    @Override
    protected void populateWizardPage(WizardPageDefinition page, WizardPageContext pageContext) {
        page.setName("Details");
        super.populateWizardPage(page, pageContext);
        OAuth2Authentication authenticationMethod = (OAuth2Authentication) getAuthenticationMethod();
        List<PropertyDefinition> propertyDefinitions =
                authenticationMethod.createPropertyDefinitions(this, pageContext);
        PropertyGroupDefinition propertyGroup = new PropertyGroupDefinition("Authentication");
        page.addPropertyGroup(propertyGroup);
        for (PropertyDefinition propertyDefinition : propertyDefinitions) {
            propertyGroup.addPropertyDefinition(propertyDefinition);
        }
    }

    @Override
    public GCSFileDataStoreModel createModel(DataStore dataStore) {
        return new GCSFileDataStoreModel(getFileProtocol(), dataStore);
    }

    @Override
    public ImportJobType<?> getImportJobType() {
        return this._importJobType;
    }
}
