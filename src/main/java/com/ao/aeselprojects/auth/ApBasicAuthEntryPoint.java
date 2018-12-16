/*
Apache2 License Notice
Copyright 2018 Alex Barry
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.ao.aeselprojects.auth;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

public class ApBasicAuthEntryPoint extends BasicAuthenticationEntryPoint {

  @Override
  public void commence(final HttpServletRequest request,
      final HttpServletResponse response,
      final AuthenticationException authException) throws IOException, ServletException {
    //Authentication failed, send error response.
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.addHeader("WWW-Authenticate", "Basic realm=" + getRealmName() + "");

    PrintWriter writer = response.getWriter();
    writer.println("HTTP Status 401 : " + authException.getMessage());
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    setRealmName("PROJECTS_REALM");
    super.afterPropertiesSet();
  }
}
