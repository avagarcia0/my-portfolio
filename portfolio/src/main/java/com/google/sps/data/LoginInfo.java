// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.data;

/** Object for holding login information that is sent to the client. */
public class LoginInfo {
  String loginUrl;
  String logoutUrl;
  String userEmail;

  boolean isAdmin;
  boolean isLoggedIn;

  /** Constructor for when the user is not logged in. */
  public LoginInfo(String loginUrl) {
    isLoggedIn = false;

    this.loginUrl = loginUrl;
  }

  /** Constructor for when the user is logged in. */
  public LoginInfo(String logoutUrl, String userEmail, boolean isAdmin) {
    isLoggedIn = true;

    this.logoutUrl = logoutUrl;
    this.userEmail = userEmail;
    this.isAdmin = isAdmin;
  }
}