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

import static com.kenshoo.integrations.plugins.connectors.GCSFileRangeImportJobModelDetailPage.BUCKET_PROPERTY_KEY;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.kenshoo.integrations.plugins.connectors.util.PathUtil;

import datameer.dap.sdk.entity.DataSourceConfiguration;
import datameer.dap.sdk.importjob.FileImportJobModel;
import datameer.dap.sdk.importjob.FileInputMode;
import datameer.dap.sdk.importjob.FileRangeImportJobModel;
import datameer.dap.sdk.importjob.PlaceholderFileInput;
import datameer.dap.sdk.importjob.placeholder.PlaceholderResolver;
import datameer.dap.sdk.property.WizardPageDefinition;
import datameer.dap.sdk.util.StringUtil;

public class GCSFileRangeImportJobModel<T> extends FileRangeImportJobModel<T> {

    private static final long serialVersionUID = 1L;

    public GCSFileRangeImportJobModel(DataSourceConfiguration conf) {
        super(conf);
    }

    @Override
    public WizardPageDefinition createDetailsWizardPage() {
        return new GCSFileRangeImportJobModelDetailPage(this).getPageDefinition();
    }

    @Override
    public String getFile() {
        String bucketName = getConfiguration().getStringProperty(BUCKET_PROPERTY_KEY, null);
        String fileName = super.getFile();
        fileName = resolvePlaceholders(fileName);
        return PathUtil.buildStorageObjectFileSystemQuery(bucketName, fileName);
    }

    public String getPlaceholderResolvedProperty(String propertyKey) {
        String value = getConfiguration().getStringProperty(propertyKey, null);
        String result = null;
        if (!StringUtil.isEmpty(value)) {
            result = resolvePlaceholders(value);
        }
        return result;
    }

    public String resolvePlaceholders(String string) {
        PlaceholderFileInput fileInput =
                _createPlaceHolderFileInput(string, new HashSet<URI>(), PathUtil.DEFAULT_ROOT, FileInputMode.PREVIEW);
        String resolvedFileExpression = string;
        for (PlaceholderResolver placeholderResolver : fileInput.getPlaceholderResolvers()) {
            resolvedFileExpression = placeholderResolver.getSampleStartPath(resolvedFileExpression);
        }
        return resolvedFileExpression;
    }

    protected PlaceholderFileInput _createPlaceHolderFileInput(String fileExpression,
            Set<URI> hostAddresses,
            String dataStoreRootPath,
            FileInputMode inputMode) {
        Method createPlaceHolderFileInputMethod = getCreatePlaceHolderFileInputMethod();
        if (createPlaceHolderFileInputMethod != null) {
            try {
                return (PlaceholderFileInput) createPlaceHolderFileInputMethod.invoke(this,
                        fileExpression,
                        hostAddresses,
                        dataStoreRootPath,
                        inputMode);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        PlaceholderFileInput fileInput =
                new PlaceholderFileInput(new ArrayList<URI>(hostAddresses), dataStoreRootPath, fileExpression);
        configureFileInput(fileInput, inputMode);
        return fileInput;
    }

    private Method getCreatePlaceHolderFileInputMethod() {
        Method[] methods = FileImportJobModel.class.getDeclaredMethods();
        Method result = null;
        for (Method m : methods) {
            if (m.getName().equals("createPlaceHolderFileInput")) {
                result = m;
                break;
            }
        }
        return result;
    }
}
