/*
 * Copyright 2022 Google LLC
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

package com.google.firebase.appcheck;

import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponseInterceptor;
import com.google.api.client.json.JsonFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.firebase.IncomingHttpResponse;
import com.google.firebase.internal.ErrorHandlingHttpClient;
import com.google.firebase.internal.HttpRequestInfo;
import com.google.firebase.internal.SdkUtils;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A helper class for interacting with Firebase App Check service.
 */
final class FirebaseAppCheckClientImpl implements FirebaseAppCheckClient {

  private static final String APP_CHECK_URL = "https://firebaseremoteconfig.googleapis.com/v1/projects/%s/appCheck";

  private static final Map<String, String> COMMON_HEADERS =
          ImmutableMap.of(
                  "X-Firebase-Client", "fire-admin-java/" + SdkUtils.getVersion()
          );

  private final String appCheckUrl;
  private final HttpRequestFactory requestFactory;
  private final JsonFactory jsonFactory;
  private final ErrorHandlingHttpClient<FirebaseAppCheckException> httpClient;

  private FirebaseAppCheckClientImpl(FirebaseAppCheckClientImpl.Builder builder) {
    checkArgument(!Strings.isNullOrEmpty(builder.projectId));
    this.appCheckUrl = String.format(APP_CHECK_URL, builder.projectId);
    this.requestFactory = checkNotNull(builder.requestFactory);
    this.jsonFactory = checkNotNull(builder.jsonFactory);
    HttpResponseInterceptor responseInterceptor = builder.responseInterceptor;
    FirebaseAppCheckClientImpl.AppCheckErrorHandler errorHandler = new FirebaseAppCheckClientImpl.AppCheckErrorHandler(this.jsonFactory);
    this.httpClient = new ErrorHandlingHttpClient<>(requestFactory, jsonFactory, errorHandler)
            .setInterceptor(responseInterceptor);
  }

  @VisibleForTesting
  String getRemoteConfigUrl() {
    return appCheckUrl;
  }

  @VisibleForTesting
  HttpRequestFactory getRequestFactory() {
    return requestFactory;
  }

  @VisibleForTesting
  JsonFactory getJsonFactory() {
    return jsonFactory;
  }

  @Override
  public DecodedAppCheckToken verifyToken(String token) throws FirebaseAppCheckException {
    HttpRequestInfo request = HttpRequestInfo.buildGetRequest(appCheckUrl)
            .addAllHeaders(COMMON_HEADERS);
    IncomingHttpResponse response = httpClient.send(request);
    //TemplateResponse templateResponse = httpClient.parse(response, TemplateResponse.class);
    //Template template = new Template(templateResponse);
    return response;
  }
}
