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
import datameer.dap.sdk.property.PropertyPersister;
import java.util.Collections;
import java.util.List;

class OAuthPropertyPersister implements PropertyPersister {
    private final OAuthSetup oAuthSetup;

    public OAuthPropertyPersister(OAuthSetup oAuthSetup) {
        this.oAuthSetup = oAuthSetup;
    }

    @Override
    public void clear(GenericConfiguration conf) {
        for (String propertyKey : this.oAuthSetup.getPropertiesWhichShouldntBePartOfApp())
            conf.removeProperty(propertyKey);
    }

    @Override
    public void save(GenericConfiguration conf, List<String> values) {
    }

    @Override
    public List<String> load(GenericConfiguration conf) {
        return Collections.emptyList();
    }
}
