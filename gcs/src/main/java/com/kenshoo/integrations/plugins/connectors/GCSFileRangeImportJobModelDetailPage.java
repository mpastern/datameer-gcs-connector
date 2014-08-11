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

import com.kenshoo.integrations.plugins.connectors.util.PathUtil;

import datameer.dap.sdk.common.GenericConfiguration;
import datameer.dap.sdk.importjob.FileRangeImportJobModel;
import datameer.dap.sdk.importjob.FileRangeImportJobModelDetailPage;
import datameer.dap.sdk.property.PropertyDefinition;
import datameer.dap.sdk.property.PropertyDefinitionValueValidator;
import datameer.dap.sdk.property.PropertyGroupDefinition;
import datameer.dap.sdk.property.PropertyType;
import datameer.dap.sdk.property.WizardPageDefinition;

public class GCSFileRangeImportJobModelDetailPage extends FileRangeImportJobModelDetailPage {

    public static final String DONE_FILE_FILTER = "donefile";
    public static final String DONE_FILE_PROPERTY_KEY = "gcs.done.file";
    public static final String BUCKET_PROPERTY_KEY = "gcs.bucket.name";

    public GCSFileRangeImportJobModelDetailPage(FileRangeImportJobModel<?> importJobModel) {
        super(importJobModel);
    }

    @Override
    public WizardPageDefinition getPageDefinition() {
        WizardPageDefinition page = super.getPageDefinition();
        PropertyGroupDefinition basicPropertyGroup = page.getPropertyGroup("Basic");
        PropertyDefinition fileProperty = page.getProperty("file");
        if (fileProperty == null) {
            return page;
        }
        List<PropertyDefinition> propertyDefinitions = basicPropertyGroup.getPropertyDefinitions();
        PropertyDefinition rootPathProperty = page.getProperty("ds.root.path");
        if (rootPathProperty != null) {
            propertyDefinitions.remove(rootPathProperty);
        }
        int filePropertyIndex = propertyDefinitions.indexOf(fileProperty);
        if (filePropertyIndex != -1) {
            fileProperty.setHelpText(getFileHelpText());
            fileProperty.setLabel("File Name");

            PropertyDefinition bucketNameProperty = createBucketProperty();
            propertyDefinitions.add(filePropertyIndex, bucketNameProperty);
        }
        return page;
    }

    public PropertyDefinition createBucketProperty() {
        PropertyDefinition bucketNameProperty =
                new PropertyDefinition(BUCKET_PROPERTY_KEY, "Bucket Name", PropertyType.STRING);
        bucketNameProperty.setRequired(true);
        bucketNameProperty.addValidator(new PropertyDefinitionValueValidator() {
            @Override
            public String validate(GenericConfiguration conf, int index, String value) {
                String result = null;
                if (!PathUtil.isBucketNameValid(value)) {
                    result = "The value is not a valid Google Cloud Storage bucket name.";
                }
                return result;
            }
        });
        return bucketNameProperty;
    }

    public String getFileHelpText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Enter file name.");
        sb.append("Wildcards are accepted. <br>");
        sb.append("Use patterns <b>%year%, %month%, %day%, %hour%</b>or <b>%min%</b> to be replaced with current date values.<br>");
        sb.append("<a href=\"#\" onclick=\"openDocumentation('/File+Path+and+File+Name+Patterns')\">Learn more</a>");
        return sb.toString();
    }
}
