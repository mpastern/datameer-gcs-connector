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

import datameer.dap.sdk.datastore.DataStoreType;
import datameer.dap.sdk.property.PropertyDefinition;
import datameer.dap.sdk.property.WizardPageContext;

public abstract class AuthenticationMethod {
    private final String name;

    public AuthenticationMethod(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract List<PropertyDefinition> createPropertyDefinitions(DataStoreType dataStoreType,
            WizardPageContext paramWizardPageContext);
}
