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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.common.exceptions.ConnectionFailedException;

import com.google.analytics.admin.v1alpha.AccessBinding;
import com.google.analytics.admin.v1alpha.AnalyticsAdminServiceClient;
import com.google.analytics.admin.v1alpha.AnalyticsAdminServiceClient.ListAccessBindingsPage;
import com.google.analytics.admin.v1alpha.AnalyticsAdminServiceClient.ListAccessBindingsPagedResponse;
import com.google.analytics.admin.v1alpha.AnalyticsAdminServiceSettings;
import com.google.analytics.admin.v1alpha.ListPropertiesRequest;
import com.google.analytics.admin.v1alpha.PropertyName;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.ServiceAccountCredentials;

import io.grpc.StatusRuntimeException;


/**
 * Google Analytics connection to manage a property.
 *
 */
public class GoogleAnalyticsConnection {

  /** logger */
  private static final Log LOG = Log.getLog(GoogleAnalyticsConnection.class);

  /** configuration */
  private GoogleAnalyticsConfiguration configuration;

  /** admin service client */
  private AnalyticsAdminServiceClient analyticsAdmin;

  /** if configuration is valid */
  private boolean valid = false;
  
  private String parent;

  /**
   * Constructor
   * @param configuration configuration
   */
  public GoogleAnalyticsConnection(GoogleAnalyticsConfiguration configuration) {
    this.configuration = configuration;
    Credentials creds;
    try {
      creds = ServiceAccountCredentials.fromStream(new FileInputStream(configuration.getJsonPath()));
      AnalyticsAdminServiceSettings settings = AnalyticsAdminServiceSettings.newBuilder().setCredentialsProvider(FixedCredentialsProvider.create(creds)).build();
      analyticsAdmin = AnalyticsAdminServiceClient.create(settings);
      parent = PropertyName.of(configuration.getProperty()).toString();
    } catch (FileNotFoundException e) {
      throw new ConfigurationException(e.getMessage(), e);
    } catch (IOException e) {
      throw new ConnectionFailedException(e.getMessage(), e);
    }
    
    valid = true;
  }
  
  /**
   * Check to see if this is ready to connect
   * @return true if ready, false otherwise
   */
  public boolean checkAvailable() {
    return !analyticsAdmin.isShutdown() && !analyticsAdmin.isTerminated() && valid;
  }

  /**
   * Test connection by getting list of properties that can be seen by this account
   */
  public void test() {
    if (!valid) {
      throw new ConfigurationException("Invalid configuration");
    }
    String filterString = "parent:" + parent;
    try {
      analyticsAdmin.listProperties(ListPropertiesRequest.newBuilder().setFilter(filterString).build());
    } catch (StatusRuntimeException e) {
      LOG.error("Failed to list properties with filter {0}", filterString);
      throw e;
    }
  }
  
  /**
   * Get all users
   * @return list of users
   */
  public List<AccessBinding> getUsers() {
    List<AccessBinding> users = new ArrayList<>();
    ListAccessBindingsPagedResponse response = analyticsAdmin.listAccessBindings(parent);
    for (ListAccessBindingsPage page : response.iteratePages()) {
      for (AccessBinding user : page.iterateAll()) {
        users.add(user);
      }
    }
    
    return users;
  }
  
  /**
   * Get specified user
   * @param uidValue uid value to lookup
   * @return value
   */
  public AccessBinding getUser(String uidValue) {
    return analyticsAdmin.getAccessBinding(uidValue);
  }
  
  /**
   * Create user in property
   * @param email email address of user
   * @return created user
   */
  public AccessBinding createUser(String email) {
    AccessBinding link = AccessBinding.newBuilder().setUser(email).addRoles(configuration.getDirectRole()).build();
    AccessBinding content = analyticsAdmin.createAccessBinding(parent, link);
    return content;
  }

  /**
   * Delete user from property
   * @param uidValue user id to remove
   */
  public void deleteUser(String uidValue) {
    analyticsAdmin.deleteAccessBinding(uidValue);
  }
  
  /**
   * Update user in property
   * @param uidValue uid value
   * @param email name value
   * @return user
   */
  public AccessBinding updateUser(String uidValue, String email) {
    AccessBinding binding = analyticsAdmin.getAccessBinding(uidValue);
    AccessBinding update = AccessBinding.newBuilder(binding).setUser(email).setName(uidValue).build();
    return analyticsAdmin.updateAccessBinding(update);
  }
  
  public void close() {
    analyticsAdmin.close();
  }
  
}
