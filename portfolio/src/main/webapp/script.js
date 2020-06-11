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

/**
 * Adds a random voting method and a nice property it has to the page.
 */
function addRandomVotingMethod() {
  const votingMethods = [
    {
      name: 'STAR voting',
      property: 'Performs well in a variety of simulations making different ' +
          'assumptions',
    },
    {name: 'Score voting', property: 'Simple but expressive'},
    {name: '3-2-1 voting', property: 'Highly resistant to strategy'},
    {
      name: 'Approval voting',
      property: 'Incredibly simple, just vote for one or more',
    },
  ];

  // Pick a random voting method.
  const index = Math.floor(Math.random() * votingMethods.length);
  const votingMethod = votingMethods[index];

  // Add it to the page.
  const votingMethodContainer =
      document.getElementById('voting-method-container');
  votingMethodContainer.innerText =
      votingMethod.name + ': ' + votingMethod.property;
}

/**
 * Displays the comments that have been left so far.
 */
async function displayComments() {
  const numComments = document.getElementById('num-comments').value;
  const response = await fetch('/data?num-comments=' + numComments);
  const comments = await response.json();
  const commentsContainer = document.getElementById('comments-container');

  commentsContainer.innerHTML = '';

  for (const comment of comments) {
    const commentElement = document.createElement('div');
    commentElement.innerText = comment;
    commentsContainer.appendChild(commentElement);
  }
}

/**
 * Displays the login status of the user and a link for logging in or out.
 */
async function displayLoginInfo() {
  const response = await fetch('/login');
  const loginInfo = await response.json();
  const loginUrl = loginInfo[0];
  const logoutUrl = loginInfo[1];
  const userEmail = loginInfo[2];

  const loginContainer = document.getElementById('login-container');

  // Check if the user is logged in.
  if (userEmail === null) {
    loginContainer.innerHTML =
        '<a href="' + loginUrl + '">Click here to log in</a>';
  } else {
    loginContainer.innerHTML = 'Logged in as ' + userEmail + '<br /><a href="' +
        logoutUrl + '">Click here to log out</a>';
  }
}

/**
 * Displays comments and login info.
 */
function displayInfo() {
  displayComments();
  displayLoginInfo();
}

/**
 * Deletes all comments currently being stored.
 */
async function deleteAllComments() {
  await fetch('/delete-data', {method: 'POST'});
  displayComments();
}
