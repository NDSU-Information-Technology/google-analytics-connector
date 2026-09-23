/*
 * Copyright (c) 2022 North Dakota State University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.ndsu.eci.midpoint.google_analytics_connector;

import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.spi.ConfigurationProperty;

/**
 * Configuration class for the GA connector.
 *
 */
public class GoogleAnalyticsConfiguration extends AbstractConfiguration {

  /** logger */
  private static final Log LOG = Log.getLog(GoogleAnalyticsConfiguration.class);

  /** google anayltics property being managed */
  private String property;

  /** path to json file with creds */
  private String jsonPath;
  
  private String directRole;

  @Override
  public void validate() {
    //todo implement
  }

  @ConfigurationProperty(displayMessageKey = "googleanalytics.config.property",
      helpMessageKey = "googleanalytics.config.property.help")
  public String getProperty() {
    return property;
  }

  public void setProperty(String property) {
    this.property = property;
  }

  @ConfigurationProperty(displayMessageKey = "googleanalytics.config.jsonPath",
      helpMessageKey = "googleanalytics.config.jsonPath.help")
  public String getJsonPath() {
    return jsonPath;
  }

  public void setJsonPath(String jsonPath) {
    this.jsonPath = jsonPath;
  }

  @ConfigurationProperty(displayMessageKey = "googleanalytics.config.directRole",
      helpMessageKey = "googleanalytics.config.directRole.help")
  public String getDirectRole() {
    return directRole;
  }

  public void setDirectRole(String directRole) {
    this.directRole = directRole;
  }

}