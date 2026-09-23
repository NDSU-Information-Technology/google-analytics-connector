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

import java.util.List;
import java.util.Set;

import org.identityconnectors.common.CollectionUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectionFailedException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.EqualsFilter;
import org.identityconnectors.framework.common.objects.filter.Filter;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.PoolableConnector;
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.framework.spi.operations.DeleteOp;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.identityconnectors.framework.spi.operations.UpdateOp;

import com.google.analytics.admin.v1alpha.AccessBinding;


/**
 * Connector to manage Google Analytics users on a single property.
 *
 */
@ConnectorClass(displayNameKey = "googleanalytics.connector.display", configurationClass = GoogleAnalyticsConfiguration.class)
public class GoogleAnalyticsConnector implements PoolableConnector, TestOp, SchemaOp, SearchOp<Filter>, CreateOp, DeleteOp, UpdateOp {

  /** logger */
  private static final Log LOG = Log.getLog(GoogleAnalyticsConnector.class);

  /** configuration */
  private GoogleAnalyticsConfiguration configuration;
  /** connection */
  private GoogleAnalyticsConnection connection;

  @Override
  public Configuration getConfiguration() {
    return configuration;
  }

  @Override
  public void init(Configuration configuration) {
    this.configuration = (GoogleAnalyticsConfiguration)configuration;
    this.connection = new GoogleAnalyticsConnection(this.configuration);
  }

  @Override
  public void dispose() {
    connection.close();
  }

  @Override
  public void test() {
    connection.test();
  }

  @Override
  public Schema schema() {
    ObjectClassInfoBuilder objectClassBuilder = new ObjectClassInfoBuilder();
    objectClassBuilder.setType("googleAnalytics");
    AttributeInfoBuilder uidAib = new AttributeInfoBuilder(Uid.NAME);
    uidAib.setNativeName("id");
    uidAib.setType(String.class);
    uidAib.setRequired(true);
    uidAib.setCreateable(false);
    uidAib.setUpdateable(false);
    uidAib.setReadable(true);
    objectClassBuilder.addAttributeInfo(uidAib.build());

    AttributeInfoBuilder nameAib = new AttributeInfoBuilder(Name.NAME);
    nameAib.setNativeName("email");
    nameAib.setType(String.class);
    nameAib.setRequired(true);
    objectClassBuilder.addAttributeInfo(nameAib.build());

    SchemaBuilder schemaBuilder = new SchemaBuilder(GoogleAnalyticsConnector.class);
    schemaBuilder.defineObjectClass(objectClassBuilder.build());
    return schemaBuilder.build();
  }

  @Override
  public FilterTranslator<Filter> createFilterTranslator(ObjectClass objectClass, OperationOptions options) {
    return new FilterTranslator<Filter>() {
      public List<Filter> translate(Filter filter) {
          return CollectionUtil.newList(filter);
      }
    };
  }

  @Override
  public void executeQuery(ObjectClass objectClass, Filter query, ResultsHandler handler, OperationOptions options) {
    if (query instanceof EqualsFilter && ((EqualsFilter) query).getAttribute() instanceof Uid) {
      Uid uid = (Uid) ((EqualsFilter) query).getAttribute();
      handler.handle(convertUserToConnectorObject(connection.getUser(uid.getUidValue())));
      return;
    }
    List<AccessBinding> users;
    users = connection.getUsers();

    for (AccessBinding user : users) {
      ConnectorObject obj = convertUserToConnectorObject(user);
      if (obj == null) {
        continue;
      }
      handler.handle(obj);
    }

  }

  /**
   * Convert user to what the search requires
   * @param user user from GA
   * @return what search requires back
   */
  private ConnectorObject convertUserToConnectorObject(AccessBinding user) {
    if (user.getUser() == null || user.getUser().isBlank() || !user.getUser().contains("@")) {
      return null;
    }
    
    ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
    builder.setUid(user.getName());
    builder.setName(user.getUser());
    return builder.build();
  }

  @Override
  public Uid create(ObjectClass objectClass, Set<Attribute> createAttributes, OperationOptions options) {
    String email = getEmail(createAttributes);

    AccessBinding link = connection.createUser(email);

    return new Uid(link.getName());
  }

  /**
   * Get email address from attribute list
   * @param attributes attribute list
   * @return found email if found, null if somehow not in list of attributes
   */
  private String getEmail(Set<Attribute> attributes) {
    String email = null;
    for (Attribute attr : attributes) {
      if (attr.getName().equals(Name.NAME)) {
        email = (String) attr.getValue().get(0);
      }
    }
    return email;
  }

  @Override
  public void delete(ObjectClass objectClass, Uid uid, OperationOptions options) {
    connection.deleteUser(uid.getUidValue());

  }

  @Override
  public Uid update(ObjectClass objectClass, Uid uid, Set<Attribute> replaceAttributes, OperationOptions options) {
    connection.deleteUser(uid.getUidValue());

    String email = getEmail(replaceAttributes);

    AccessBinding link = connection.createUser(email);

    return new Uid(link.getName());
  }

  @Override
  public void checkAlive() {
    if (!connection.checkAvailable()) {
      throw new ConnectionFailedException("Connection check failed");
    }
  }

}
