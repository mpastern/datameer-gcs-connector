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

import datameer.com.google.common.base.Preconditions;
import datameer.dap.sdk.datastore.DataStoreType;

public class OAuth2Authentication extends OAuthAuthentication {
    public OAuth2Authentication() {
        super("OAuth2 Authentication");
    }

    @Override
    protected OAuth2Setup getOAuthSetup(DataStoreType dataStoreType) {
        Preconditions.checkArgument(
                dataStoreType instanceof OAuth2BasedDataStoreType, "%s can only be used by an implementation of %s!",
                new Object[] { getClass().getSimpleName(), OAuth2BasedDataStoreType.class.getName() });

        return ((OAuth2BasedDataStoreType) dataStoreType).getOAuthSetup();
    }
}
