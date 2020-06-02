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
  const votingMethods =
      ['STAR voting', 'Score voting', '3-2-1 voting', 'Approval voting'];
  const properties =
      ['Performs well in a variety of simulations making different assumptions',
       'Simple but expressive',
       'Highly resistant to strategy',
       'Incredibly simple, just vote for one or more'];

  // Pick a random voting method.
  const index = Math.floor(Math.random() * votingMethods.length);
  const votingMethod = votingMethods[index];
  const property = properties[index];

  // Add it to the page.
  const votingMethodContainer = document.getElementById('voting-method-container');
  votingMethodContainer.innerText = votingMethod + ": " + property;
}

/**
 * Adds a message from the servlet to the page.
 */
async function getMessage() {
  const response = await fetch('/data');
  const message = await response.text();
  document.getElementById('message-container').innerText = message;
}
